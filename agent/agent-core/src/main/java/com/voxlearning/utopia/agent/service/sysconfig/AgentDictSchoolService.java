/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.ListUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.AgentSchoolDictData;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.agent.listener.handler.AgentModifyDictSchoolApplyMessageHandler;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.support.AgentSchoolSupport;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.*;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentDictSchoolServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentModifyDictSchoolApplyServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserSchoolServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentDictSchoolService
 * Created by Administrator on 2016/6/28.
 */
@Named
public class AgentDictSchoolService extends AbstractAgentService {
    public static final String INT_TIME = "yyyyMMdd";

    @Inject private RaikouSystem raikouSystem;

    @Inject private AgentDictSchoolServiceClient agentDictSchoolServiceClient;
    @Inject private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;


    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentUserSchoolServiceClient agentUserSchoolServiceClient;
    @Inject private AgentModifyDictSchoolApplyServiceClient agentModifyDictSchoolApplyServiceClient;
    @Inject private OrgConfigService orgConfigService;
    @Inject private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;
    @Inject private BaseUserService baseUserService;
    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private AgentModifyDictSchoolApplyMessageHandler agentModifyDictSchoolApplyMessageHandler;
    @Inject private BaseDictService baseDictService;
    @Inject private AgentSchoolSupport agentSchoolSupport;
    @Inject private AgentGroupSupport agentGroupSupport;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;


    public MapMessage disposeApply(List<AgentModifyDictSchoolApply> applyList) {
        if (CollectionUtils.isEmpty(applyList)) {
            return MapMessage.errorMessage("没有符合规定的审核");
        }
        Map<Integer, List<AgentModifyDictSchoolApply>> applyType = applyList.stream().collect(Collectors.groupingBy(AgentModifyDictSchoolApply::getModifyType));
        StringBuffer failed = new StringBuffer();
        StringBuffer success = new StringBuffer();

        MapMessage addInfo = addSchoolDict(applyType.get(AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType()));
        if (!addInfo.isSuccess()) {
            return addInfo;
        }
        MapMessage removeInfo = removeSchoolDict(applyType.get(AgentModifyDictSchoolApply.ModifyType.DELETE_SCHOOL.getType()));
        if (!removeInfo.isSuccess()) {
            return removeInfo;
        }
        MapMessage updatePopularityTypeInfo = updateSchoolDictPopularityType(applyType.get(AgentModifyDictSchoolApply.ModifyType.UPDATE_POPULARITY.getType()));
        if (!updatePopularityTypeInfo.isSuccess()) {
            return updatePopularityTypeInfo;
        }
        MapMessage updateInfo = updateSchoolDictTargetUser(applyType.get(AgentModifyDictSchoolApply.ModifyType.UPDATE_RESPONSIBLE.getType()));
        if (!updateInfo.isSuccess()) {
            return updateInfo;
        }
        failed.append(addInfo.get("failed"));
        failed.append(removeInfo.get("failed"));
        failed.append(updateInfo.get("failed"));
        failed.append(updatePopularityTypeInfo.get("failed"));
        success.append(addInfo.get("successItem"));
        success.append(removeInfo.get("successItem"));
        success.append(updateInfo.get("successItem"));
        success.append(updatePopularityTypeInfo.get("successItem"));
        Set<Long> applyIds = applyList.stream().map(AgentModifyDictSchoolApply::getId).collect(Collectors.toSet());
        List<Long> resultApplyIds = agentModifyDictSchoolApplyServiceClient.updateApplyResolvedByIds(applyIds);
        MapMessage msg = MapMessage.successMessage();
        msg.add("failed", failed);
        msg.add("successItem", success);
        msg.add("summary", StringUtils.formatMessage("共{}条记录被处理", resultApplyIds.size()));
        return msg;
    }

    private MapMessage addSchoolDict(List<AgentModifyDictSchoolApply> addApplyList) {
        if (CollectionUtils.isEmpty(addApplyList)) {
            return MapMessage.successMessage().add("failed", "").add("successItem", "");
        }
        Map<Long, Set<Long>> targetUserSchoolMap = new HashMap<>();  // 保存要分配给专员的学校列表
        Map<Long, Set<Long>> groupSchoolMap = new HashMap<>();
        StringBuffer failed = new StringBuffer();
        StringBuffer success = new StringBuffer();
        Map<Long, AgentDictSchool> existsData = new HashMap<>();
        Map<Long, AgentDictSchool> schoolMap = getSchoolMap();
        existsData.putAll(schoolMap);
        Set<Long> schoolIds = addApplyList.stream().map(AgentModifyDictSchoolApply::getSchoolId).collect(Collectors.toSet());
        Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = batchLoadCrmSchoolSummaryAndSchool(schoolIds);

        addApplyList.forEach(p -> {
            if (existsData.keySet().contains(p.getSchoolId())) {
                failed.append(StringUtils.formatMessage("学校ID:{}已经在字典表中,\r\n", p.getSchoolId()));
                return;
            }
            CrmSchoolSummary crmSchoolSummary = crmSchoolSummaryMap.get(p.getSchoolId());
            if (crmSchoolSummary == null) {
                failed.append(StringUtils.formatMessage("学校ID:{}学校未找到,\r\n", p.getSchoolId()));
                return;
            }
            AgentDictSchool agentDictSchool = new AgentDictSchool();
            if (null != crmSchoolSummary.getSchoolLevel()) {
                agentDictSchool.setSchoolLevel(crmSchoolSummary.getSchoolLevel().getLevel());
            }
            agentDictSchool.setSchoolId(p.getSchoolId());
            agentDictSchool.setCountyCode(crmSchoolSummary.getCountyCode());
            agentDictSchool.setCountyName(p.getRegionName());
            agentDictSchool.setDisabled(false);
            agentDictSchool.setCalPerformance(true);
            if (StringUtils.isNotEmpty(p.getSchoolPopularity())) {
                agentDictSchool.setSchoolPopularity(AgentSchoolPopularityType.of(p.getSchoolPopularity()));
            }
            MapMessage msg = addAgentDictSchool(agentDictSchool);

            if (msg.isSuccess()) {
                if (p.getTargetUserId() != null && p.getTargetUserId() > 0) {
                    Set<Long> schoolSet = targetUserSchoolMap.computeIfAbsent(p.getTargetUserId(), k -> new HashSet<>());
                    schoolSet.add(p.getSchoolId());
                }

                if (StringUtils.isNotEmpty(p.getAccount())) {
                    long account = SafeConverter.toLong(p.getAccount());
                    if (account > 0) {
                        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(account);
                        if (CollectionUtils.isNotEmpty(groupUserList)) {
                            Long groupId = groupUserList.get(0).getGroupId();
                            Set<Long> schoolSet = groupSchoolMap.computeIfAbsent(groupId, k -> new HashSet<>());
                            schoolSet.add(p.getSchoolId());
                        }
                    }
                }

                existsData.put(p.getSchoolId(), agentDictSchool);
                success.append(StringUtils.formatMessage("学校ID:{}添加成功\r\n", p.getSchoolId()));
                agentModifyDictSchoolApplyMessageHandler.sendApproveMessage(p.getModifyType(), p.getSchoolName(), p.getSchoolId(), SafeConverter.toLong(p.getAccount()));
            } else {
                failed.append(StringUtils.formatMessage("学校ID:{}{},\r\n", p.getSchoolId(), msg.getInfo()));
            }
        });

        if (MapUtils.isNotEmpty(groupSchoolMap)) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Thread.sleep(60000); // 等待两分钟
                } catch (InterruptedException e) {
                }
                List<String> messageList = new ArrayList<>();
//                groupSchoolMap.forEach((k, v) -> {
//                    if (CollectionUtils.isNotEmpty(v)) {
//                        v.forEach(s -> {
//                            MapMessage msg = orgConfigService.setSchoolForGroup(k, s);
//                            if (!msg.isSuccess()) {
//                                messageList.add(msg.getInfo());
//                            }
//                        });
//                    }
//                });
                targetUserSchoolMap.forEach((k, v) -> {
                    if (CollectionUtils.isEmpty(v)) {
                        return;
                    }
                    List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(k);
                    groupUserList = groupUserList.stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(groupUserList)) {
                        Long groupId = groupUserList.get(0).getGroupId();
                        MapMessage message = orgConfigService.setSchoolsForUser(groupId, k, v);
                        if (!message.isSuccess()) {
                            messageList.add(message.getInfo());
                        } else {
                            List<String> messageInfoList = (List<String>) message.get("messageInfoList");
                            if (CollectionUtils.isNotEmpty(messageInfoList)) {
                                messageList.addAll(messageInfoList);
                            }
                        }
                    }
                });

                if (CollectionUtils.isNotEmpty(messageList)) {
                    String content = StringUtils.join(messageList, "<br/>");
                    emailServiceClient.createPlainEmail()
                            .body(content)
                            .subject(RuntimeMode.current().getStageMode() +
                                    "环境字典表自动分配失败")
                            .to("song.wang@17zuoye.com;jinling.zhu@17zuoye.com;dongwei.xiao@17zuoye.com;xiaoqing.chang@17zuoye.com;yaguang.wang@17zuoye.com;chunlin.yu@17zuoye.com")
                            .send();
                }
            });
        }

        // 将字典表学校分配给专员， 采用异步操作， 此处不关注分配结果， 分配结果可到组织结构中确认
       /* if (MapUtils.isNotEmpty(targetUserSchoolMap)) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Thread.sleep(2 * 60000); // 等待两分钟
                } catch (InterruptedException e) {
                }
            });
        }*/
        return MapMessage.successMessage().add("failed", failed).add("successItem", success);
    }


    private MapMessage removeSchoolDict(List<AgentModifyDictSchoolApply> removeApplyList) {
        if (CollectionUtils.isEmpty(removeApplyList)) {
            return MapMessage.successMessage().add("failed", "").add("successItem", "");
        }
        StringBuffer failed = new StringBuffer();
        StringBuffer success = new StringBuffer();
        Map<Long, AgentDictSchool> existsData = new HashMap<>();
        Map<Long, AgentDictSchool> schoolMap = getSchoolMap();
        existsData.putAll(schoolMap);
        removeApplyList.forEach(p -> {
            AgentDictSchool dictSchool = existsData.get(p.getSchoolId());
            if (dictSchool == null) {
                failed.append(StringUtils.formatMessage("学校ID:{}不在字典表中,\r\n", p.getSchoolId()));
                return;
            }
            int updRecords = agentDictSchoolServiceClient.deleteDictDimSchool(dictSchool.getId());
            if (updRecords > 0) {
                success.append(StringUtils.formatMessage("学校ID:{}已经删除成功\r\n", p.getSchoolId()));
                existsData.remove(p.getSchoolId());
                flushOrgService(p.getSchoolId());
                agentModifyDictSchoolApplyMessageHandler.sendApproveMessage(p.getModifyType(), p.getSchoolName(), p.getSchoolId(), SafeConverter.toLong(p.getAccount()));
            }
        });
        return MapMessage.successMessage().add("failed", failed).add("successItem", success);
    }

    private MapMessage updateSchoolDictPopularityType(List<AgentModifyDictSchoolApply> updateApplyList) {
        if (CollectionUtils.isEmpty(updateApplyList)) {
            return MapMessage.successMessage().add("failed", "").add("successItem", "");
        }
        StringBuffer failed = new StringBuffer();
        StringBuffer success = new StringBuffer();
        Map<Long, AgentDictSchool> existsData = new HashMap<>();
        Map<Long, AgentDictSchool> schoolMap = getSchoolMap();
        existsData.putAll(schoolMap);
        updateApplyList.forEach(p -> {
            AgentDictSchool dictSchool = existsData.get(p.getSchoolId());
            if (dictSchool == null) {
                failed.append(StringUtils.formatMessage("学校ID:{}不在字典表中,\r\n", p.getSchoolId()));
                return;
            }
            boolean updFlg = false;
            if (StringUtils.isNotEmpty(p.getSchoolPopularity()) && null != AgentSchoolPopularityType.of(p.getSchoolPopularity())) {
                dictSchool.setSchoolPopularity(AgentSchoolPopularityType.of(p.getSchoolPopularity()));
                updFlg = true;
            }
            if (updFlg) {
                dictSchool = agentDictSchoolServiceClient.replace(dictSchool);
                success.append(StringUtils.formatMessage("学校ID:{}已经更新成功\r\n", p.getSchoolId()));
            } else {
                success.append(StringUtils.formatMessage("学校ID:{}已经无需更新\r\n", p.getSchoolId()));
            }
            agentModifyDictSchoolApplyMessageHandler.sendApproveMessage(p.getModifyType(), p.getSchoolName(), p.getSchoolId(), SafeConverter.toLong(p.getAccount()));
        });
        return MapMessage.successMessage().add("failed", failed).add("successItem", success);
    }

    private MapMessage updateSchoolDictTargetUser(List<AgentModifyDictSchoolApply> updateApplyList) {
        if (CollectionUtils.isEmpty(updateApplyList)) {
            return MapMessage.successMessage().add("failed", "").add("successItem", "");
        }
        StringBuffer failed = new StringBuffer();
        StringBuffer success = new StringBuffer();
        Map<Long, AgentDictSchool> existsData = new HashMap<>();
        Map<Long, AgentDictSchool> schoolMap = getSchoolMap();
        existsData.putAll(schoolMap);
        updateApplyList.forEach(p -> {
            School school = raikouSystem.loadSchool(p.getSchoolId());
            if (school == null) {
                failed.append(StringUtils.formatMessage("学校ID:{}不存在,\r\n", p.getSchoolId()));
                return;
            }

            AgentDictSchool dictSchool = existsData.get(p.getSchoolId());
            if (dictSchool == null) {
                failed.append(StringUtils.formatMessage("学校ID:{}不在字典表中,\r\n", p.getSchoolId()));
                return;
            }

            if (Objects.nonNull(p.getTargetUserId())) {
                AgentUserSchool agentUserSchool = baseOrgService.getUserSchool(p.getTargetUserId(), p.getSchoolId());
                if (agentUserSchool == null) {
                    flushOrgService(p.getSchoolId());
                    AgentUserSchool userSchool = new AgentUserSchool();
                    userSchool.setUserId(p.getTargetUserId());
                    userSchool.setSchoolId(p.getSchoolId());
                    userSchool.setRegionCode(school.getRegionCode());
                    userSchool.setSchoolLevel(school.getLevel());
                    agentUserSchoolServiceClient.persist(userSchool);
                }
            } else {//未指定负责人
                flushOrgService(p.getSchoolId());
            }

            success.append(StringUtils.formatMessage("学校ID:{}已经更新成功\r\n", p.getSchoolId()));
            agentModifyDictSchoolApplyMessageHandler.sendApproveMessage(p.getModifyType(), p.getSchoolName(), p.getSchoolId(), SafeConverter.toLong(p.getAccount()));
        });
        return MapMessage.successMessage().add("failed", failed).add("successItem", success);
    }


    private Map<Long, AgentDictSchool> getSchoolMap() {
        return baseDictService.loadAllSchoolDictData().stream().collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity(), (key1, key2) -> key2));
    }

//    public List<AgentSchoolDictData> getWrappedSchoolDictData() {
//        List<AgentDictSchool> allDataTemp = baseDictService.loadAllSchoolDictData();
//        Set<Long> schoolIds = new HashSet<>();
//        Set<Integer> countyCodes = new HashSet<>();
//        allDataTemp.forEach(item -> {
//            if (null != item){
//                schoolIds.add(item.getSchoolId());
//                countyCodes.add(item.getCountyCode());
//            }
//        });
//
//        Map<Long, CrmSchoolSummary> schoolMapTemp = batchLoadCrmSchoolSummaryAndSchool(schoolIds);
//        if (MapUtils.isEmpty(schoolMapTemp)) {
//            return Collections.emptyList();
//        }
//
////        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIds).getUninterruptibly();
//        Map<Integer, ExRegion> regionsFromBuffer = regionServiceClient.getExRegionBuffer().loadRegions(countyCodes);
//
//        //获取学校用户关系
//        List<AgentUserSchool> agentUserSchools = agentUserSchoolLoaderClient.findAll();
//        if (CollectionUtils.isEmpty(agentUserSchools)){
//            agentUserSchools = Collections.emptyList();
//        }
//        Map<Long,List<AgentUserSchool>> agentUserSchoolMap = agentUserSchools.stream().collect(Collectors.groupingBy(AgentUserSchool::getSchoolId));
//
//        //获取用户信息
//        Map<String, AgentUser> allUsersMap = baseUserService.getAllAgentUsers();
//
//        //获取部门用户关系信息
//        List<AgentGroupUser> allGroupUsers = agentGroupUserLoaderClient.findAll();
//
//        // 获取市经理信息
//        List<AgentGroupUser> cityManageUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.CityManager)).collect(Collectors.toList());
//        Map<Long, Long> cityManageUsersMap = cityManageUsers.stream().collect(Collectors.toMap(AgentGroupUser::getGroupId, AgentGroupUser::getUserId, (o1, o2) -> o1));
//
//        // 获取所有专员信息
//        List<AgentGroupUser> businessDeveloperUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.BusinessDeveloper)).collect(Collectors.toList());
//        Map<Long, AgentGroupUser> businessDeveloperUserMap = businessDeveloperUsers.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1));
//
//        // 获取部门数据
//        Map<Long, AgentGroup> allGroupMap = agentGroupLoaderClient.findAllGroups().stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
//
//        //拼装大区、区域、分区、市经理对应关系信息
//        List<AgentDictSchoolExportData> schoolDictExportDataList = new ArrayList<>();
//        //过滤出所有分区信息
//        List<AgentGroup> cityGroupList = allGroupMap.values().stream().filter(item -> null != item && item.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
//        cityGroupList.forEach(group -> {
//            AgentDictSchoolExportData schoolDictExportData = new AgentDictSchoolExportData();
//            //设置分区
//            schoolDictExportData.setCityGroupId(group.getId());
//            schoolDictExportData.setCityGroupName(group.getGroupName());
//            // 设置大区和区域信息
//            AgentGroup regionGroup = allGroupMap.get(group.getParentId());
//            if (regionGroup != null) {
//                //如果父级部门级别是“区域”
//                if (regionGroup.fetchGroupRoleType() == AgentGroupRoleType.Area){
//                    //设置“区域”
//                    schoolDictExportData.setAreaGroupName(regionGroup.getGroupName());
//                    //再次获取父级部门
//                    AgentGroup parentGroup = allGroupMap.get(regionGroup.getParentId());
//                    if(null != parentGroup){
//                        //设置“大区”
//                        schoolDictExportData.setRegionGroupName(parentGroup.getGroupName());
//                    }
//                    //如果父级部门级别是“大区”
//                }else if (regionGroup.fetchGroupRoleType() == AgentGroupRoleType.Region){
//                    //设置“区域”为空
//                    schoolDictExportData.setAreaGroupName("");
//                    //设置“大区”
//                    schoolDictExportData.setRegionGroupName(regionGroup.getGroupName());
//                }
//            }
//            //设置部门对应的市经理信息
//            Long cityManageUserId = cityManageUsersMap.get(group.getId());
//            if (null != cityManageUserId){
//                AgentUser user = allUsersMap.get(String.valueOf(cityManageUserId));
//                if (null != user){
//                    schoolDictExportData.setCityManagerId(user.getId());
//                    schoolDictExportData.setCityManagerName(user.getRealName());
//                }
//            }
//            schoolDictExportDataList.add(schoolDictExportData);
//        });
//        Map<Long, AgentDictSchoolExportData> cityGroupIdExportDataMap = schoolDictExportDataList.stream().collect(Collectors.toMap(AgentDictSchoolExportData::getCityGroupId, Function.identity(), (o1, o2) -> o1));
//
//        List<AgentSchoolDictData> res = new ArrayList<>();
//        allDataTemp.forEach(p -> {
//            if (p == null) {
//                return;
//            }
//            Long schoolId = p.getSchoolId();
//            if (schoolId == null || !schoolMapTemp.containsKey(schoolId) || null == schoolMapTemp.get(schoolId)) {
//                return;
//            }
//            AgentSchoolDictData data = new AgentSchoolDictData();
//            SchoolDepartmentInfo departmentInfo = new SchoolDepartmentInfo();
////            data.setId(p.getId());
//            CrmSchoolSummary school = schoolMapTemp.get(schoolId);
//            data.setSchoolId(schoolId);
//            data.setAgentSchoolPermeabilityType(null == p.getPermeabilityType()?null:p.getPermeabilityType().getDesc());
//            data.setSchoolName(school.getSchoolName() + (school.getDisabled() ? "(已失效)" :  ""));
//            data.setRegionCode(p.getCountyCode());
////            data.setRegionName(p.getCountyName());
//            ExRegion region = regionsFromBuffer.get(p.getCountyCode());
//            if (region != null) {
////                data.setCityCode(region.getCityCode());
//                data.setCityName(region.getCityName());
//                data.setCountyName(region.getCountyName());
////                data.setCountyCode(region.getCountyCode());
////                data.setProvinceCode(region.getProvinceCode());
////                data.setProvinceName(region.getProvinceName());
////                departmentInfo.setRegionName(StringUtils.formatMessage("{}-{}", region.getCityName(), region.getCountyName()));
//            }
//            SchoolLevel schoolLevel = school.getSchoolLevel();
//            if (schoolLevel != null) {
//                if (schoolLevel == SchoolLevel.MIDDLE) {
//                    data.setSchoolLevel("初中");
//                } else {
//                    data.setSchoolLevel(schoolLevel.getDescription());
//                }
////                data.setSchoolLevelEnum(schoolLevel);
//            }
//            data.setCalPerformance(ConversionUtils.toBool(p.getCalPerformance()));
//            if (null != p.getSchoolDifficulty()){
//                data.setSchoolDifficulty(p.getSchoolDifficulty().getLevel());
//            }
//            if (null != p.getSchoolPopularity()) {
//                data.setSchoolPopularity(p.getSchoolPopularity().getLevel());
//            }
//            if (null != p.getPermeabilityType()){
//                data.setAgentSchoolPermeabilityType(p.getPermeabilityType().getDesc());
//            }
//
//            List<Long> schoolGroupIds = baseOrgService.getGroupIdsBySchool(schoolId, Collections.singletonList(AgentGroupRoleType.City));
//            if (CollectionUtils.isNotEmpty(schoolGroupIds)){
//               //  设置大区、区域、分区、市经理信息
//                Long groupId = schoolGroupIds.get(0);
//                AgentDictSchoolExportData schoolDictExportData = cityGroupIdExportDataMap.get(groupId);
//                if (null != schoolDictExportData){
//                    departmentInfo.setGroupName(schoolDictExportData.getCityGroupName());
//                    departmentInfo.setAreaGroupName(schoolDictExportData.getAreaGroupName());
//                    departmentInfo.setRegionGroupName(schoolDictExportData.getRegionGroupName());
//                    departmentInfo.setCityManagerName(schoolDictExportData.getCityManagerName());
//                }
//                // 设置专员信息
//                List<AgentUserSchool> agentUserSchoolsTemp = agentUserSchoolMap.get(schoolId);
//                if (CollectionUtils.isNotEmpty(agentUserSchoolsTemp)){
//                    agentUserSchoolsTemp.forEach(item -> {
//                        Long userId = item.getUserId();
//                        AgentGroupUser businessDeveloperUser = businessDeveloperUserMap.get(userId);
//                        AgentUser user = allUsersMap.get(String.valueOf(userId));
//                        if (null != businessDeveloperUser && user != null){
//                            departmentInfo.setBusinessDeveloperName(user.getRealName());
//                            departmentInfo.setBusinessDeveloperNumber(user.getAccountNumber());
//                            return;
//                        }
//                    });
//                }
//            }
//            data.setSchoolDepartmentInfo(departmentInfo);
//
////            SchoolExtInfo schoolExtInfo = schoolExtInfoMap.get(p.getSchoolId());
////            if (schoolExtInfo != null) {
////                data.setSchoolSize(schoolExtInfo.getSchoolSize());
////            }
//            res.add(data);
//        });
//        return res;
//    }


    public List<AgentSchoolDictData> getWrappedSchoolDictData() {
        List<AgentDictSchool> allDictList = baseDictService.loadAllSchoolDictData();
        Set<Long> schoolIds = new HashSet<>();
        allDictList.forEach(item -> {
            if (null != item) {
                schoolIds.add(item.getSchoolId());
            }
        });
//        Map<Long, SchoolEsInfo> schoolInfoMap = agentSchoolSupport.loadSchoolEsInfo(schoolIds);

        Map<Long, CrmSchoolSummary> schoolSummaryMap = agentSchoolSupport.batchLoadCrmSchoolSummaryAndSchool(schoolIds);

        List<AgentGroup> cityGroupList = agentGroupLoaderClient.findByRoleId(AgentGroupRoleType.City.getId());

        Map<Long, GroupWithParent> groupWithParentMap = agentGroupSupport.generateGroupWithParent(cityGroupList);

        List<SchoolLevel> schoolLevelList = new ArrayList<>();
        schoolLevelList.add(SchoolLevel.JUNIOR);
        schoolLevelList.add(SchoolLevel.MIDDLE);
        schoolLevelList.add(SchoolLevel.HIGH);
        List<Future<List<AgentSchoolDictData>>> futureList = new ArrayList<>();
        for (SchoolLevel schoolLevel : schoolLevelList) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> buildSchoolDictData(allDictList, schoolLevel, groupWithParentMap, schoolSummaryMap)));
        }

        List<AgentSchoolDictData> dataList = new ArrayList<>();
        for (Future<List<AgentSchoolDictData>> future : futureList) {
            try {
                List<AgentSchoolDictData> result = future.get();
                if (CollectionUtils.isNotEmpty(result)) {
                    dataList.addAll(result);
                }
            } catch (Exception e) {
                logger.error("getWrappedSchoolDictData error ", e);
            }
        }

        return dataList;
    }

    private List<AgentSchoolDictData> buildSchoolDictData(List<AgentDictSchool> dictList, SchoolLevel schoolLevel, Map<Long, GroupWithParent> groupWithParentMap, Map<Long, CrmSchoolSummary> schoolSummaryMap) {
        if (CollectionUtils.isEmpty(dictList) || schoolLevel == null) {
            return Collections.emptyList();
        }
        Map<Integer, List<AgentDictSchool>> countyDictMap = dictList.stream().filter(p -> p.getSchoolLevel() == schoolLevel.getLevel()).collect(Collectors.groupingBy(AgentDictSchool::getCountyCode, Collectors.toList()));
        List<Future<List<AgentSchoolDictData>>> futureList = new ArrayList<>();
        for (Integer countyCode : countyDictMap.keySet()) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> {
                // 获取该区域所在的部门
                List<Long> groupIds = agentGroupSupport.getGroupIdsByRegionCode(countyCode);
                // 过滤出负责该地区，该学段的分区
                Long targetGroupId = groupIds.stream().filter(p -> {
                    GroupWithParent groupWithParent = groupWithParentMap.get(p);
                    if (groupWithParent != null) {
                        List<SchoolLevel> managedSchoolLevels = groupWithParent.fetchServiceTypeList().stream().map(AgentServiceType::toSchoolLevel).collect(Collectors.toList());
                        if (managedSchoolLevels.contains(schoolLevel)) {
                            return true;
                        }
                        return false;
                    }
                    return false;
                }).findFirst().orElse(null);

                // 有部门负责该区域，该学段，  获取市经理信息
                AgentUser cityManager = null;
                Map<Long, AgentUser> userMap = new HashMap<>();
                Map<Long, Long> schoolUserMap = new HashMap<>();
                if (targetGroupId != null) {
                    List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroup(targetGroupId);
                    Long managerId = groupUserList.stream().filter(p -> p.getUserRoleType() == AgentRoleType.CityManager).map(AgentGroupUser::getUserId).findFirst().orElse(null);

                    List<Long> groupUserIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
                    Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolByUsers(groupUserIds);
                    schoolUserMap.putAll(userSchoolMap.values().stream().flatMap(List::stream).collect(Collectors.toMap(AgentUserSchool::getSchoolId, AgentUserSchool::getUserId, (o1, o2) -> o1)));
                    userMap.putAll(agentUserLoaderClient.findByIds(groupUserIds));
                    if (managerId != null) {
                        cityManager = userMap.get(managerId);
                    }
                }

                List<AgentSchoolDictData> resultList = new ArrayList<>();
                List<AgentDictSchool> countyDictList = countyDictMap.get(countyCode);
                for (AgentDictSchool p : countyDictList) {
                    AgentSchoolDictData data = new AgentSchoolDictData();
                    data.setSchoolId(p.getSchoolId());
                    data.setAgentSchoolPermeabilityType(null == p.getPermeabilityType() ? null : p.getPermeabilityType().getDesc());

                    CrmSchoolSummary schoolSummary = schoolSummaryMap.get(p.getSchoolId());
                    if (schoolSummary != null) {
                        data.setSchoolName(schoolSummary.getSchoolName());

                        data.setCityName(schoolSummary.getCityName());
                        data.setCountyCode(schoolSummary.getCountyCode());
                        data.setCountyName(schoolSummary.getCountyName());
                    } else {
                        data.setCountyCode(p.getCountyCode());
                    }
                    data.setSchoolLevel(schoolLevel.getDescription());
                    data.setCalPerformance(ConversionUtils.toBool(p.getCalPerformance()));
                    if (null != p.getSchoolDifficulty()) {
                        data.setSchoolDifficulty(p.getSchoolDifficulty().getLevel());
                    }
                    if (null != p.getSchoolPopularity()) {
                        data.setSchoolPopularity(p.getSchoolPopularity().getLevel());
                    }
                    if (null != p.getPermeabilityType()) {
                        data.setAgentSchoolPermeabilityType(p.getPermeabilityType().getDesc());
                    }


                    // 有分区负责该地区的该学段， 学校没有相应的部门信息
                    if (targetGroupId != null) {
                        // 设置学校所在的部门信息
                        data.setGroupWithParent(groupWithParentMap.get(targetGroupId));
                        if (cityManager != null) {
                            data.setCityManagerName(cityManager.getRealName());
                        }
                        Long userId = schoolUserMap.get(p.getSchoolId());
                        if (userId != null) {
                            AgentUser user = userMap.get(userId);
                            if (user != null) {
                                data.setBusinessDeveloperName(user.getRealName());
                                data.setBusinessDeveloperNumber(user.getAccountNumber());
                            }
                        }
                    }
                    resultList.add(data);
                }
                return resultList;
            }));
        }

        List<AgentSchoolDictData> dataList = new ArrayList<>();
        for (Future<List<AgentSchoolDictData>> future : futureList) {
            try {
                List<AgentSchoolDictData> result = future.get();
                if (CollectionUtils.isNotEmpty(result)) {
                    dataList.addAll(result);
                }
            } catch (Exception e) {
                logger.error("getWrappedSchoolDictData error ", e);
            }
        }
        return dataList;
    }


    /**
     * 分批查询，优先从SchoolSummary中查询，如果SchoolSummary查不到，则从VoxSchool查询，但结果都封装成CrmSchoolSummary对象
     * 需要注意的是，只填充了部分字段
     *
     * @param schoolIds
     * @return
     */
    public Map<Long, CrmSchoolSummary> batchLoadCrmSchoolSummaryAndSchool(Collection<Long> schoolIds) {
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<Long> copySchoolIds = new ArrayList<>(schoolIds);
            Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = batchLoadCrmSchoolSummary(copySchoolIds);
            //移除summary数据存在的
            copySchoolIds.removeAll(crmSchoolSummaryMap.keySet());
            Map<Long, School> schoolMap = batchLoadSchool(copySchoolIds);
            if (MapUtils.isNotEmpty(schoolMap)) {
                schoolMap.forEach((k, v) -> crmSchoolSummaryMap.put(k, toCrmSchoolSummary(v)));
            }
            return crmSchoolSummaryMap;
        }
        return new HashMap<>();
    }

    /**
     * 只封装了部分字段
     *
     * @param school
     * @return
     */
    private CrmSchoolSummary toCrmSchoolSummary(School school) {
        if (null != school) {
            CrmSchoolSummary crmSchoolSummary = new CrmSchoolSummary();
            crmSchoolSummary.setSchoolId(school.getId());
            crmSchoolSummary.setSchoolName(school.getCname());
            if (null != school.getLevel()) {
                crmSchoolSummary.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            }
            crmSchoolSummary.setCountyCode(school.getRegionCode());
            crmSchoolSummary.setDisabled(school.getDisabled());
            return crmSchoolSummary;
        }
        return null;
    }

    /**
     * 分批查询，如果一次查询会把缓存查死
     *
     * @param schoolIds
     * @return
     */
    public Map<Long, CrmSchoolSummary> batchLoadCrmSchoolSummary(Collection<Long> schoolIds) {
        Map<Long, CrmSchoolSummary> schoolMapTemp = new HashMap<>();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            AgentResourceService.batchIds(schoolIds, 2000).forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    Map<Long, CrmSchoolSummary> longCrmSchoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(v);
                    schoolMapTemp.putAll(longCrmSchoolSummaryMap);
                }
            });
        }
        return schoolMapTemp;
    }

    private Map<Long, School> batchLoadSchool(Collection<Long> schoolIds) {
        Map<Long, School> schoolMapTemp = new HashMap<>();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            AgentResourceService.batchIds(schoolIds, 2000).forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    Map<Long, School> schoolMap = raikouSystem.loadSchools(v);
                    schoolMapTemp.putAll(schoolMap);
                }
            });
        }
        return schoolMapTemp;
    }


    public List<AgentSchoolDictData> getWrappedSchoolDictDataBySchool(Long schoolId) {
        AgentDictSchool dictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (dictSchool == null) {
            return Collections.emptyList();
        }
        return buildAgentSchoolDictData(Collections.singletonList(dictSchool));
    }

    public List<AgentDictSchool> loadSchoolDictDataBySchool(Collection<Long> schoolIds) {
        List<AgentDictSchool> retList = new ArrayList<>();
        Map<Long, AgentDictSchool> schoolMap = agentDictSchoolLoaderClient.findBySchoolIds(schoolIds);
        schoolIds.forEach(p -> {
            if (schoolMap.containsKey(p) && !retList.contains(schoolMap.get(p))) {
                retList.add(schoolMap.get(p));
            }
        });

        return retList;
    }

    public List<AgentDictSchool> loadSchoolDictDataByRegion(Collection<Integer> countyCodes) {
        List<AgentDictSchool> retList = new ArrayList<>();
        Map<Integer, List<AgentDictSchool>> countyMap = agentDictSchoolLoaderClient.findByCountyCodes(countyCodes);
        countyCodes.forEach(p -> {
            if (countyMap.containsKey(p)) {
                retList.addAll(countyMap.get(p));
            }
        });

        return retList;
    }

    public List<AgentSchoolDictData> getWrappedSchoolDictDataByRegion(Collection<Integer> countyCodes) {
        return buildAgentSchoolDictData(loadSchoolDictDataByRegion(countyCodes));
    }

    public AgentSchoolDictData getWrappedSchoolDictData(Long dictId) {
        AgentDictSchool dictSchool = agentDictSchoolLoaderClient.load(dictId);
        if (dictSchool != null) {
            List<AgentSchoolDictData> schoolDictDatas = buildAgentSchoolDictData(Collections.singletonList(dictSchool));
            if (schoolDictDatas != null && schoolDictDatas.size() >= 1) {
                AgentSchoolDictData agentSchoolDictData = schoolDictDatas.get(0);
                List<Long> groupIds = agentGroupSupport.getMarketGroupIdsBySchool(agentSchoolDictData.getSchoolId());
                if (CollectionUtils.isNotEmpty(groupIds)) {
                    agentSchoolDictData.setGroupId(groupIds.get(0));
                }
                return agentSchoolDictData;
            }
        }

        return null;
    }

    public int removeSchoolDictData(Long dictId) {
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.load(dictId);
        int updRecords = agentDictSchoolServiceClient.deleteDictDimSchool(dictId);
        if (updRecords > 0) {
            // 删除字典表学校时，删除对应 AgentUserSchool
            flushOrgService(agentDictSchool.getSchoolId());
        }
        return updRecords;
    }

    private void flushOrgService(Long schoolId) {
        List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolBySchool(schoolId);
        if (CollectionUtils.isNotEmpty(userSchoolList)) {
            userSchoolList.forEach(p -> {
                p.setDisabled(true);
                agentUserSchoolServiceClient.update(p.getId(), p);
            });
        }
    }

    private List<AgentSchoolDictData> buildAgentSchoolDictData(List<AgentDictSchool> allDictSchool) {
        if (CollectionUtils.isEmpty(allDictSchool)) {
            return Collections.emptyList();
        }
/*        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(allDictSchool.stream().map(AgentDictSchool::getSchoolId).collect(Collectors.toSet()))
                .getUninterruptibly();*/
        Map<Long, CrmSchoolSummary> crmSchoolMap = batchLoadCrmSchoolSummaryAndSchool(allDictSchool.stream().map(AgentDictSchool::getSchoolId).collect(Collectors.toSet()));
        if (MapUtils.isEmpty(crmSchoolMap)) {
            return Collections.emptyList();
        }
        Set<Integer> countyCodes = allDictSchool.stream().map(AgentDictSchool::getCountyCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> regionsFromBuffer = raikouSystem.getRegionBuffer().loadRegions(countyCodes);
        List<AgentSchoolDictData> res = new ArrayList<>();
        allDictSchool.forEach((AgentDictSchool p) -> {
            if (p == null) {
                return;
            }
            if (p.getSchoolId() == null || !crmSchoolMap.containsKey(p.getSchoolId())) {
                return;
            }
            AgentSchoolDictData data = new AgentSchoolDictData();
            data.setId(p.getId());
            Long schoolId = p.getSchoolId();
            if (schoolId != null) {
                CrmSchoolSummary schoolSummary = crmSchoolMap.get(p.getSchoolId());
                data.setSchoolId(schoolId);
                if (schoolSummary != null) {
                    data.setSchoolName(schoolSummary.getSchoolName() + (schoolSummary.getDisabled() ? "(已失效)" : ""));
                    data.setRegionCode(p.getCountyCode());
                    data.setRegionName(p.getCountyName());
                    ExRegion region = regionsFromBuffer.get(p.getCountyCode());
                    if (region != null) {
                        data.setCityCode(region.getCityCode());
                        data.setCityName(region.getCityName());
                        data.setCountyName(region.getCountyName());
                        data.setCountyCode(region.getCountyCode());
                        data.setProvinceCode(region.getProvinceCode());
                        data.setProvinceName(region.getProvinceName());
                    }
                    if (schoolSummary.getSchoolLevel() != null) {
                        if (schoolSummary.getSchoolLevel() == SchoolLevel.MIDDLE) {
                            data.setSchoolLevel("初中");
                        } else {
                            data.setSchoolLevel(schoolSummary.getSchoolLevel().getDescription());
                        }
                        data.setSchoolLevelEnum(schoolSummary.getSchoolLevel());
                    }
                }
            }

            data.setCalPerformance(ConversionUtils.toBool(p.getCalPerformance()));
            if (null != p.getSchoolDifficulty()) {
                data.setSchoolDifficulty(p.getSchoolDifficulty().getLevel());
            }
            if (null != p.getSchoolPopularity()) {
                data.setSchoolPopularity(p.getSchoolPopularity().getLevel());
            }
            if (null != p.getPermeabilityType()) {
                data.setAgentSchoolPermeabilityType(p.getPermeabilityType().getDesc());
            }
            res.add(data);
        });
        return res;
    }

    public MapMessage addAgentDictSchool(AgentDictSchool agentDictSchool) {
        try {
            agentDictSchoolServiceClient.insert(agentDictSchool);
            return MapMessage.successMessage();
        } catch (DuplicateKeyException dx) {
            logger.error("The school has been in existence", dx);
            return MapMessage.errorMessage("学校已存在");
        } catch (Exception ex) {
            logger.error("add agent dict school is failed ", ex);
            return MapMessage.errorMessage("添加学校字典表失败");
        }
    }

    public MapMessage updateAgentDictSchool(AgentDictSchool agentDictSchool) {
        try {
            Map<Long, AgentDictSchool> schoolMap = getSchoolMap();
            AgentDictSchool temp = schoolMap.get(agentDictSchool.getSchoolId());
            if (null == temp || !Objects.equals(agentDictSchool.getCalPerformance(), temp.getCalPerformance())
                    || !Objects.equals(agentDictSchool.getSchoolDifficulty(), temp.getSchoolDifficulty())
                    || !Objects.equals(agentDictSchool.getSchoolPopularity(), temp.getSchoolPopularity()) || !Objects.equals(agentDictSchool.getPermeabilityType(), temp.getPermeabilityType())) {
                agentDictSchoolServiceClient.replace(agentDictSchool);
                if (!Objects.equals(agentDictSchool.getSchoolDifficulty(), temp.getSchoolDifficulty()) && null == agentDictSchool.getSchoolDifficulty()) {
                    agentDictSchoolServiceClient.unsetField("SCHOOL_DIFFICULTY", agentDictSchool.getId());
                }
                if (!Objects.equals(agentDictSchool.getPermeabilityType(), temp.getPermeabilityType()) && null == agentDictSchool.getPermeabilityType()) {
                    agentDictSchoolServiceClient.unsetField("PERMEABILITY_TYPE", agentDictSchool.getId());
                }
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("update agent dict school is failed ", ex);
            return MapMessage.errorMessage("更新学校字典表失败");
        }
    }

    private List<AgentSchoolDictData> convert2AgentDictSchool(XSSFWorkbook workbook) {
        List<AgentSchoolDictData> list = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows = 1;
        if (null != sheet) {
            while (true) {
                try {
                    AgentSchoolDictData agentSchoolDictData = new AgentSchoolDictData();
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    Long schoolId = XssfUtils.getLongCellValue(row.getCell(0));
                    String schoolName = getStringCellValue(row.getCell(1));
                    String agentSchoolPermeabilityType = getStringCellValue(row.getCell(2));
                    String schoolPopularity = getStringCellValue(row.getCell(3));
                    String schoolDifficulty = getStringCellValue(row.getCell(4));
                    String calPerformanceStr = getStringCellValue(row.getCell(5));
                    String groupName = getStringCellValue(row.getCell(6));
                    agentSchoolDictData.setSchoolId(schoolId);
                    agentSchoolDictData.setSchoolName(schoolName);
                    agentSchoolDictData.setAgentSchoolPermeabilityType(agentSchoolPermeabilityType);
                    if (StringUtils.isNotEmpty(schoolPopularity)) {
                        agentSchoolDictData.setSchoolPopularity(schoolPopularity);
                    }
                    if (StringUtils.isNotEmpty(schoolDifficulty)) {
                        agentSchoolDictData.setSchoolDifficulty(schoolDifficulty);
                    }
                    agentSchoolDictData.setCalPerformance(!Objects.equals(calPerformanceStr, "否"));
                    agentSchoolDictData.setGroupName(groupName);
                    list.add(agentSchoolDictData);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        return list;
    }

    private MapMessage validateImportSchoolDictInfo(List<AgentSchoolDictData> agentSchoolDictDataList) {
        MapMessage resultMessage = MapMessage.errorMessage();
        if (CollectionUtils.isNotEmpty(agentSchoolDictDataList)) {
            List<String> errorList = new ArrayList<>();
            Set<Long> schoolIds = agentSchoolDictDataList.stream().filter(item -> item.getSchoolId() != null).map(AgentSchoolDictData::getSchoolId).collect(Collectors.toSet());
            Map<Long, CrmSchoolSummary> schoolSummaryMap = batchLoadCrmSchoolSummaryAndSchool(schoolIds);
            //FIXME 为了获取学校认证状态  CrmSchoolSummary 中没有  先从vox_school全部查  如果效率不高 看看是不是能让大数据的同事直接在CrmSchoolSummary 中加上 authenticationState
            Map<Long, School> schoolMap = batchLoadSchool(schoolIds);
            List<CrmSchoolSummary> allSchools = new ArrayList<>(schoolSummaryMap.values());
            Set<Integer> regionCodes = allSchools.stream().map(CrmSchoolSummary::getCountyCode).collect(Collectors.toSet());
            Map<Integer, ExRegion> regions = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
//            List<AgentGroup> allAgentGroupList = baseOrgService.findAllGroups();
//            Map<String, AgentGroup> agentGroupMap = allAgentGroupList.stream().filter(item -> null != item.getGroupName()).collect(Collectors.toMap(AgentGroup::getGroupName, Function.identity(), (key1, key2) -> key2));
//            Map<Long,List<Integer>> groupRegionCodeCache = new HashMap<>();
            List<Long> tempIdList = new ArrayList<>();
            for (int i = 0; i < agentSchoolDictDataList.size(); i++) {
                int rows = i + 2;
                AgentSchoolDictData item = agentSchoolDictDataList.get(i);

                if (item.getSchoolId() == null) {
                    errorList.add(rows + "行学校ID错误。");
                    continue;
                }
                if (tempIdList.contains(item.getSchoolId())) {
                    errorList.add(rows + "行学校ID重复，ID为：" + item.getSchoolId());
                    continue;
                } else {
                    tempIdList.add(item.getSchoolId());
                }
                if (!schoolSummaryMap.containsKey(item.getSchoolId()) || schoolSummaryMap.get(item.getSchoolId()) == null) {
                    errorList.add(rows + "行学校ID错误。");
                    logger.warn("Null school for schoolId = {}", item.getSchoolId());
                    continue;
                }
                School school1 = schoolMap.get(item.getSchoolId());

                if (null != school1 && school1.getAuthenticationState().equals(3)) {
                    errorList.add(rows + "行， 学校为假学校（鉴定状态未通过）,");
                    continue;
                }

                CrmSchoolSummary school = schoolSummaryMap.get(item.getSchoolId());
                if (null != school.getSchoolLevel()) {
                    item.setLevel(school.getSchoolLevel().getLevel());
                }
                item.setRegionCode(school.getCountyCode());
                item.setCountyCode(school.getCountyCode());
                ExRegion exRegion = regions.get(school.getCountyCode());
                item.setCountyName(ConversionUtils.toString(exRegion.getCityName()) + ConversionUtils.toString(exRegion.getCountyName()));

                if (StringUtils.isNotEmpty(item.getAgentSchoolPermeabilityType()) && null == AgentSchoolPermeabilityType.of(item.getAgentSchoolPermeabilityType())) {
                    errorList.add(rows + "行渗透情况填写错误,");
                    continue;
                }

                if (null != item.getSchoolDifficulty() && null == AgentDictSchoolDifficultyType.of(item.getSchoolDifficulty())) {
                    errorList.add(rows + "行任务难度填写错误,");
                    continue;
                }

                if (null != item.getSchoolPopularity() && null == AgentSchoolPopularityType.of(item.getSchoolPopularity())) {
                    errorList.add(rows + "行学校等级填写错误,");
                    continue;
                }

//                AgentGroup agentGroup = agentGroupMap.get(item.getGroupName());
//                if (null == agentGroup){
//                    errorList.add(rows + "行学校所属部门错误或不存在");
//                    continue;
//                }
//                if (!groupRegionCodeCache.containsKey(agentGroup.getId())){
//                    List<Integer> tempCodeList = baseOrgService.getGroupRegionCodeList(agentGroup.getId());
//                    groupRegionCodeCache.put(agentGroup.getId(),agentRegionService.getCountyCodes(tempCodeList));
//                }
//                List<Integer> groupRegionCodeList = groupRegionCodeCache.get(agentGroup.getId());
//                if(CollectionUtils.isEmpty(groupRegionCodeList) || !groupRegionCodeList.contains(school.getCountyCode())){
//                    errorList.add(rows + "行学校不在部分负责的区域范围内");
//                    continue;
//                }
//
//                item.setGroupId(agentGroup.getId());
            }
            if (CollectionUtils.isNotEmpty(errorList)) {
                resultMessage.put("errorList", errorList);
            } else {
                resultMessage = MapMessage.successMessage();
            }
        }
        return resultMessage;
    }

    public MapMessage importSchoolDictInfo(XSSFWorkbook workbook, AuthCurrentUser authCurrentUser) {
        List<AgentSchoolDictData> agentSchoolDictDataList = convert2AgentDictSchool(workbook);
        MapMessage mapMessage = validateImportSchoolDictInfo(agentSchoolDictDataList);
        if (mapMessage.isSuccess()) {
            Map<Long, AgentDictSchool> existsData = new HashMap<>();
            Map<Long, AgentDictSchool> schoolMap = getSchoolMap();
            existsData.putAll(schoolMap);
            agentSchoolDictDataList = agentSchoolDictDataList.stream().collect(Collectors.toMap(AgentSchoolDictData::getSchoolId, Function.identity(), (o1, o2) -> o1)).values().stream().collect(Collectors.toList());
            int allDealSchoolCount = agentSchoolDictDataList.size();
            int addCount = 0;
            int updateCount = 0;
            Set<Long> groupChangeIds = new HashSet<>();

//            Map<Long, AgentSchoolDictData> agentSchoolDictDataMap = agentSchoolDictDataList.stream().collect(Collectors.toMap(AgentSchoolDictData::getSchoolId, Function.identity(), (o1, o2) -> o2));
//            Map<Long, AgentGroupSchool> agentGroupSchoolMap = new HashMap<>();
//            AgentResourceService.batchIds(agentSchoolDictDataMap.keySet(),2000).forEach((k, v) -> {
//                Map<Long, AgentGroupSchool> tempMap = agentGroupSchoolLoaderClient.findBySchoolIds(v);
//                if (MapUtils.isNotEmpty(tempMap)){
//                    agentGroupSchoolMap.putAll(tempMap);
//                }
//            });

            //需要执行更新操作的字典表学校
            List<AgentDictSchool> needUpdateSchools = new ArrayList<>();
            //需要执行添加操作的字典表学校
            List<AgentDictSchool> needAddSchools = new ArrayList<>();
//            //需要执行更新学校部门的学校，Key：schoolId，Value：groupId
//            Map<Long,Long> needUpdateSchoolGroupMap = new HashMap<>();
            for (int i = 0; i < agentSchoolDictDataList.size(); i++) {
                AgentSchoolDictData agentSchoolDictData = agentSchoolDictDataList.get(i);
                AgentDictSchool agentDictSchool = new AgentDictSchool();
                agentDictSchool.setSchoolLevel(agentSchoolDictData.getLevel());
                agentDictSchool.setSchoolId(agentSchoolDictData.getSchoolId());

                agentDictSchool.setCountyCode(agentSchoolDictData.getCountyCode());
                agentDictSchool.setCountyName(agentSchoolDictData.getCountyName());
                agentDictSchool.setPermeabilityType(AgentSchoolPermeabilityType.of(agentSchoolDictData.getAgentSchoolPermeabilityType()));
                agentDictSchool.setCalPerformance(agentSchoolDictData.getCalPerformance());
                if (null != agentSchoolDictData.getSchoolDifficulty()) {
                    agentDictSchool.setSchoolDifficulty(AgentDictSchoolDifficultyType.of(agentSchoolDictData.getSchoolDifficulty()));
                }
                if (null != agentSchoolDictData.getSchoolPopularity()) {
                    agentDictSchool.setSchoolPopularity(AgentSchoolPopularityType.of(agentSchoolDictData.getSchoolPopularity()));
                }

                if (existsData.containsKey(agentDictSchool.getSchoolId())) {
                    AgentDictSchool dictSchool = existsData.get(agentDictSchool.getSchoolId());
                    agentDictSchool.setId(dictSchool.getId());
                    needUpdateSchools.add(agentDictSchool);
                    updateCount++;
                } else {
                    needAddSchools.add(agentDictSchool);
                    addCount++;
                }
//                //更新学校部门流程
//                AgentGroupSchool groupSchool = agentGroupSchoolMap.get(agentSchoolDictData.getSchoolId());
//                if (null == groupSchool || !Objects.equals(agentSchoolDictData.getGroupId(),groupSchool.getGroupId())){
//                    needUpdateSchoolGroupMap.put(agentSchoolDictData.getSchoolId(),agentSchoolDictData.getGroupId());
//                    groupChangeIds.add(agentSchoolDictData.getSchoolId());
//                }
                existsData.put(agentDictSchool.getSchoolId(), agentDictSchool);
            }
            //单独开个线程处理操作
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    if (CollectionUtils.isNotEmpty(needUpdateSchools)) {
                        needUpdateSchools.forEach(p -> {
                            agentDictSchoolServiceClient.replace(p);
                        });
                    }
                    if (CollectionUtils.isNotEmpty(needAddSchools)) {
                        needAddSchools.forEach(p -> {
                            agentDictSchoolServiceClient.insert(p);
                        });
                    }
//                    if (MapUtils.isNotEmpty(needUpdateSchoolGroupMap)){
//                        needUpdateSchoolGroupMap.forEach((k,v) -> {
//                            orgConfigService.setSchoolForGroup(v, k);
//                        });
//                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("字典表导入成功，操作人：" + authCurrentUser.getRealName() + "\r\n");
                    sb.append("本次操作共计" + allDealSchoolCount + "所学校,\r\n");
                    sb.append("其中新添加" + needAddSchools.size() + "所,\r\n");
                    sb.append("更新" + needUpdateSchools.size() + "所,\r\n");
//                    if (MapUtils.isNotEmpty(needUpdateSchoolGroupMap)){
//                        sb.append("以下" + needUpdateSchoolGroupMap.keySet().size() + "所学校所属部门被变更：\r\n");
//                        sb.append(StringUtils.join(needUpdateSchoolGroupMap.keySet(),","));
//                    }
                    emailServiceClient.createPlainEmail()
                            .body(sb.toString())
                            .subject("【" + RuntimeMode.current().getStageMode() + "】" +
                                    "环境字典表导入成功")
                            .to("song.wang@17zuoye.com;dongwei.xiao@17zuoye.com;xiaoqing.chang@17zuoye.com;chunlin.yu@17zuoye.com;dongshuang.zhao@17zuoye.com")
                            .send();
                } catch (Exception e) {
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    StringBuilder sb = new StringBuilder();
                    sb.append("字典表导入失败，操作人：" + authCurrentUser.getRealName() + "\r\n");
                    sb.append("Exception--------------");
                    for (StackTraceElement traceElement : stackTrace) {
                        sb.append("\r\n\tat " + traceElement);
                    }
                    emailServiceClient.createPlainEmail()
                            .body(sb.toString())
                            .subject("【" + RuntimeMode.current().getStageMode() + "】" +
                                    "环境字典表导入失败")
                            .to("song.wang@17zuoye.com;dongwei.xiao@17zuoye.com;xiaoqing.chang@17zuoye.com;chunlin.yu@17zuoye.com;dongshuang.zhao@17zuoye.com")
                            .send();
                } finally {
                }
            });
            mapMessage.put("allDealSchoolCount", allDealSchoolCount);
            mapMessage.put("addCount", addCount);
            mapMessage.put("updateCount", updateCount);
            mapMessage.put("groupChangeIds", groupChangeIds);
        }
        return mapMessage;
    }

    public static Boolean isAgentDictSchoolDifficultyType(String schoolDifficulty) {
        return StringUtils.isBlank(schoolDifficulty) || AgentDictSchoolDifficultyType.of(schoolDifficulty) != null;
    }

    public static Boolean isAgentSchoolPopularityType(String schoolPopularity) {
        return StringUtils.isBlank(schoolPopularity) || AgentSchoolPopularityType.of(schoolPopularity) != null;
    }


    public static Boolean isAgentSchoolPermeabilityType(String permeability) {
        return StringUtils.isBlank(permeability) || AgentSchoolPermeabilityType.of(permeability) != null;
    }

    private static String getStringCellValue(XSSFCell cell) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(new BigDecimal(cell.getNumericCellValue()));
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return StringUtils.deleteWhitespace(cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return "";
        }

        return null;
    }

    public void exportSchoolDictData(SXSSFWorkbook workbook, List<AgentSchoolDictData> dataList) {


        List<List<AgentSchoolDictData>> partitionList = ListUtils.partition(dataList, 40000);
        List<Future<Boolean>> futureList = new ArrayList<>();
        for (int t = 0; t < partitionList.size(); t++) {
            List<AgentSchoolDictData> partition = partitionList.get(t);
            int sheetIndex = t;
            futureList.add(AlpsThreadPool.getInstance().submit(() -> {
                try {
                    Sheet sheet = workbook.createSheet("学校字典表-" + sheetIndex);
                    sheet.createFreezePane(0, 1, 0, 1);
                    Font font = workbook.createFont();
                    font.setFontName("宋体");
                    font.setFontHeightInPoints((short) 10);
                    CellStyle firstRowStyle = workbook.createCellStyle();
                    firstRowStyle.setFont(font);
                    firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
                    firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                    firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
                    Row firstRow = sheet.createRow(0);
                    HssfUtils.setCellValue(firstRow, 0, firstRowStyle, "城市");
                    HssfUtils.setCellValue(firstRow, 1, firstRowStyle, "地区");
                    HssfUtils.setCellValue(firstRow, 2, firstRowStyle, "地区编码");
                    HssfUtils.setCellValue(firstRow, 3, firstRowStyle, "学校ID");
                    HssfUtils.setCellValue(firstRow, 4, firstRowStyle, "学校名称");
                    HssfUtils.setCellValue(firstRow, 5, firstRowStyle, "学段");
                    HssfUtils.setCellValue(firstRow, 6, firstRowStyle, "等级");
                    HssfUtils.setCellValue(firstRow, 7, firstRowStyle, "学校规模");
                    HssfUtils.setCellValue(firstRow, 8, firstRowStyle, "难度");
                    HssfUtils.setCellValue(firstRow, 9, firstRowStyle, "是否结算");
                    HssfUtils.setCellValue(firstRow, 10, firstRowStyle, "渗透情况");
                    HssfUtils.setCellValue(firstRow, 11, firstRowStyle, "大区");
                    HssfUtils.setCellValue(firstRow, 12, firstRowStyle, "区域");
                    HssfUtils.setCellValue(firstRow, 13, firstRowStyle, "分区");
                    HssfUtils.setCellValue(firstRow, 14, firstRowStyle, "市经理");
                    HssfUtils.setCellValue(firstRow, 15, firstRowStyle, "专员");
                    HssfUtils.setCellValue(firstRow, 16, firstRowStyle, "工号");

                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setFont(font);
                    cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

                    if (CollectionUtils.isNotEmpty(partition)) {
                        int rowIndex = 1;
                        for (AgentSchoolDictData data : partition) {
                            Row row = sheet.createRow(rowIndex++);
                            List<Object> exportAbleData = data.getExportAbleData();
                            if (CollectionUtils.isNotEmpty(exportAbleData)) {
                                for (int i = 0; i < exportAbleData.size(); i++) {
                                    Object object = exportAbleData.get(i);
                                    if (null != object) {
                                        if (SafeConverter.toLong(object) != 0L || Objects.equals(object, 0)) {
                                            HssfUtils.setCellValue(row, i, cellStyle, SafeConverter.toLong(object));
                                        } else {
                                            HssfUtils.setCellValue(row, i, cellStyle, ConversionUtils.toString(object).replaceAll("\\s*", "").replace("  ", "").replace("　", ""));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("error info: ", ex);
                    emailServiceClient.createPlainEmail()
                            .body("error info: " + ex)
                            .subject("导出学校字典表异常【" + RuntimeMode.current().getStageMode() + "】")
                            .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                            .send();
                }
                return true;
            }));
        }
        // 阻塞，等待完成后在返回
        for (Future<Boolean> future : futureList) {
            try {
                future.get();
            } catch (Exception e) {
            }
        }

    }
}
