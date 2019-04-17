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

package com.voxlearning.utopia.agent.service.apply;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.apply.DictSchoolEditParams;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentModifyDictSchoolApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentModifyDictSchoolApplyServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song.wang
 * @date 2017/6/20
 */
@Named
public class AgentDictSchoolApplyService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentDictSchoolService agentDictSchoolService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentModifyDictSchoolApplyLoaderClient agentModifyDictSchoolApplyLoaderClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject
    private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject
    private AgentModifyDictSchoolApplyServiceClient agentModifyDictSchoolApplyServiceClient;
    @Inject
    private WorkFlowServiceClient workFlowServiceClient;
    @Inject
    private AgentRegionService agentRegionService;
    @Inject
    private SchoolResourceService schoolResourceService;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject
    private BaseDictService baseDictService;
    @Inject private AgentGroupSupport agentGroupSupport;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;

    public String generateRegionName(Integer regionCode) {
        String regionName = "";
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion != null) {
            if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                regionName = exRegion.getCityName() + "/" + exRegion.getCountyName();
            } else if (exRegion.fetchRegionType() == RegionType.CITY) {
                regionName = exRegion.getCityName();
            }
        }
        return regionName;
    }

    public List<Map<String, Object>> getDictSchoolApplyList(Long schoolId) {
        List<AgentModifyDictSchoolApply> applyList = agentModifyDictSchoolApplyLoaderClient.findBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(applyList)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> itemMap;
        for (AgentModifyDictSchoolApply applyItem : applyList) {
            itemMap = BeanMapUtils.tansBean2Map(applyItem);
            itemMap.put("status", applyItem.getStatus().getDesc());
            itemMap.put("createDatetime", DateUtils.dateToString(applyItem.getCreateDatetime(), "yyyy-MM-dd"));
            List<WorkFlowProcessHistory> processHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(applyItem.getWorkflowId());
            StringBuilder stringBuilder = new StringBuilder();
            if (CollectionUtils.isNotEmpty(processHistoryList)) {
                processHistoryList.forEach(p -> {
                    stringBuilder.append("审核人：").append(p.getProcessorName()).append("  审核结果：").append(p.getResult().getDesc()).append("  审核意见：").append(p.getProcessNotes()).append("<br/>");
                });
            }
            itemMap.put("processFlow", stringBuilder.toString());
            retList.add(itemMap);
        }
        return retList;
    }

    public MapMessage checkModifyDictSchool(Long userId, School school, Integer modifyType, AgentSchoolPopularityType schoolPopularity) {

        Long schoolId = school.getId();
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        if (modifyType == 1) { // 添加学校
            // 判断学校是否在字典表中
            if (baseDictService.isDictSchool(schoolId)) {
                return MapMessage.errorMessage("该学校已经在字典表中，请勿重复申请");
            }

            // 学校如果不在用户所属部门负责区域内，则提示“该学校不在负责区域内，无法提交申请”
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
            Set<Integer> groupRegionCodes = new HashSet<>();
            groupIdList.forEach(p -> groupRegionCodes.addAll(agentRegionService.getCountyCodes(baseOrgService.getGroupRegionCodeList(p))));
            if (!groupRegionCodes.contains(school.getRegionCode())) {
                return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
            }
            if ((Objects.equals(schoolLevel, SchoolLevel.MIDDLE) || Objects.equals(schoolLevel, SchoolLevel.HIGH)) && null == schoolPopularity) {
                return MapMessage.errorMessage("学校等级不能为空！");
            }
        } else if (modifyType == 2) { // 删除学校
            // 判断学校是否在字典表中
            if (!baseDictService.isDictSchool(schoolId)) {
                return MapMessage.errorMessage("该学校不在字典表中，无法提交申请");
            }

            //学校如果不在自己负责区域范围内（专员看学校，市经理看地区，审核团队看地区），则提示“该学校不在负责区域内，无法提交申请”
            List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
            if (CollectionUtils.isEmpty(managedSchoolList)) {
                return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
            }
            boolean flag = managedSchoolList.stream().anyMatch(p -> Objects.equals(schoolId, p));
            if (!flag) {
                return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
            }
        } else if (modifyType == 3) { // 业务变更
            // 判断学校是否在字典表中
            if (!baseDictService.isDictSchool(schoolId)) {
                return MapMessage.errorMessage("该学校不在字典表中，无法提交申请");
            }
            if ((Objects.equals(schoolLevel, SchoolLevel.MIDDLE) || Objects.equals(schoolLevel, SchoolLevel.HIGH)) && null == schoolPopularity) {
                return MapMessage.errorMessage("学校等级不能为空！");
            }
        } else {
            return MapMessage.errorMessage("调整类别无效！");
        }

        List<AgentModifyDictSchoolApply> applyList = agentModifyDictSchoolApplyLoaderClient.findBySchoolId(schoolId);
        if (CollectionUtils.isNotEmpty(applyList)) {
            // 学校如果已经提交申请并且是【审核中】的状态，则提示“该学校已提交申请，无需重复提交申请”
            List<AgentModifyDictSchoolApply> pendingList = applyList.stream().filter(p -> ApplyStatus.PENDING == p.getStatus()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pendingList)) {
                return MapMessage.errorMessage("该学校已提交申请，无需重复提交申请");
            }
            // 学校如果已经提交申请并且是【已通过】的状态且未进行批量操作，则提示“该学校已提交申请，无需重复提交申请”
            boolean hadApprovedButNotResolvedList = applyList.stream().anyMatch(p -> ApplyStatus.APPROVED == p.getStatus() && Boolean.FALSE.equals(p.getResolved()));
            if (hadApprovedButNotResolvedList) {
                return MapMessage.errorMessage("该学校已提交申请，无需重复提交申请");
            }


//            // 学校日过已经提交申请并且是【已通过】或【被驳回】的状态，且申请日期在30天内，则提示“该学校在30天内不可重复提交申请”
//            // 取最近 30 天【已通过】或【被驳回】的申请记录
//            List<AgentModifyDictSchoolApply> list = applyList.stream().filter(p -> (ApplyStatus.APPROVED == p.getStatus() || ApplyStatus.REJECTED == p.getStatus()) && p.getCreateDatetime().after(DateUtils.addDays(new Date(), -90))).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(list)) {
//                return MapMessage.errorMessage("该学校在90天内不可重复提交申请");
//            }
        }

        if (modifyType == 1) {
            //TODO ⑤学校信息如果是【未完善】或【被驳回】的状态，则提示“学校信息未完善，请先在天玑中完善学校信息后再提交申请”
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            if (schoolExtInfo == null) {
                return MapMessage.errorMessage("学校信息未完善，请先在天玑中完善学校信息后再提交申请");
            }
            if (!schoolResourceService.checkGradeBasicDataIsComplete(schoolId)) {
                return MapMessage.errorMessage("学校信息未完善，请先在天玑中完善学校信息后再提交申请");
            }
        }
        return MapMessage.successMessage();
    }

    public MapMessage checkMobileEditDictSchool(Long userId, School school, AgentSchoolPopularityType schoolPopularity) {
        MapMessage basMsg = baseCheck(school.getId());
        if (!basMsg.isSuccess()) {
            return basMsg;
        }
        Long schoolId = school.getId();
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        if (baseDictService.isDictSchool(schoolId)) {
            return MapMessage.errorMessage("该学校已经在字典表中，请勿重复申请");
        }

        // 学校如果不在用户所属部门负责区域内，则提示“该学校不在负责区域内，无法提交申请”
        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
        Set<Integer> groupRegionCodes = new HashSet<>();
        groupIdList.forEach(p -> groupRegionCodes.addAll(agentRegionService.getCountyCodes(baseOrgService.getGroupRegionCodeList(p))));
        if (!groupRegionCodes.contains(school.getRegionCode())) {
            return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
        }
        if ((Objects.equals(schoolLevel, SchoolLevel.MIDDLE) || Objects.equals(schoolLevel, SchoolLevel.HIGH)) && null == schoolPopularity) {
            return MapMessage.errorMessage("学校等级不能为空！");
        }

        return MapMessage.successMessage();
    }

    public MapMessage checkMobileRemoveDictSchool(Long userId, Long schoolId) {
        MapMessage basMsg = baseCheck(schoolId);
        if (!basMsg.isSuccess()) {
            return basMsg;
        }
        // 判断学校是否在字典表中
        if (!baseDictService.isDictSchool(schoolId)) {
            return MapMessage.errorMessage("该学校不在字典表中，无法提交申请");
        }

        //学校如果不在自己负责区域范围内（专员看学校，市经理看地区，审核团队看地区），则提示“该学校不在负责区域内，无法提交申请”
        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        if (CollectionUtils.isEmpty(managedSchoolList)) {
            return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
        }
        boolean flag = managedSchoolList.stream().anyMatch(p -> Objects.equals(schoolId, p));
        if (!flag) {
            return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
        }

        return MapMessage.successMessage();
    }

    public MapMessage checkMobileEditDictSchoolPopularity(School school, AgentSchoolPopularityType schoolPopularity) {
        MapMessage basMsg = baseCheck(school.getId());
        if (!basMsg.isSuccess()) {
            return basMsg;
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());

        if (!baseDictService.isDictSchool(school.getId())) {
            return MapMessage.errorMessage("该学校不在字典表中，无法提交申请");
        }

        if (!Objects.equals(schoolLevel, SchoolLevel.MIDDLE) && !Objects.equals(schoolLevel, SchoolLevel.HIGH)) {
            return MapMessage.errorMessage("非初中、高中，不能变更学校等级！");
        }

        if (Objects.isNull(schoolPopularity)) {
            return MapMessage.errorMessage("学校等级不能为空！");
        }
        return MapMessage.successMessage();
    }

    public MapMessage checkMobileEditDictSchoolResponsible(Long userId, School school, Long responsibleId) {
        if (!baseDictService.isDictSchool(school.getId())) {
            return MapMessage.errorMessage("该学校不在字典表中，无法提交申请");
        }

        //学校如果不在自己负责区域范围内（专员看学校，市经理看地区，审核团队看地区），则提示“该学校不在负责区域内，无法提交申请”
        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        if (CollectionUtils.isEmpty(managedSchoolList)) {
            return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
        }
        boolean flag = managedSchoolList.stream().anyMatch(p -> Objects.equals(school.getId(), p));
        if (!flag) {
            return MapMessage.errorMessage("该学校不在负责区域内，无法提交申请");
        }

        //如果负责人为空，不校验区域
        if (Objects.isNull(responsibleId) || responsibleId == 0L) {
            return MapMessage.successMessage();
        }

        List<Long> schoolGroupIds = agentGroupSupport.getGroupIdsBySchool(school.getId());
        List<Long> groupIdList = baseOrgService.getManagedGroupIdListByUserId(userId);
        List<Long> subGroupIdList = new ArrayList<>();
        subGroupIdList.addAll(groupIdList);
        groupIdList.forEach(groupId -> {
            List<AgentGroup> agentGroups = baseOrgService.getSubGroupList(groupId);
            List<Long> temp = Optional.ofNullable(agentGroups).orElse(new ArrayList<>())
                    .stream()
                    .map(AgentGroup::getId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(temp)) {
                subGroupIdList.addAll(temp);
            }
        });
        subGroupIdList.retainAll(schoolGroupIds);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return MapMessage.errorMessage("该学校不在您管理范围，您无权创建该类申请！");
        }
        List<Long> userIdList = new ArrayList<>();
        groupIdList.forEach(groupId -> {
            List<Long> groupUserList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
            if (CollectionUtils.isNotEmpty(groupUserList)) {
                userIdList.addAll(groupUserList);
            }
        });
        List<AgentUser> userList = userIdList.stream().map(agentUserLoaderClient::load).filter(p -> p != null && p.isValidUser()).collect(Collectors.toList());

        AgentUser agentUser = Optional.ofNullable(userList).orElse(Collections.emptyList())
                .stream()
                .filter(user -> Objects.equals(user.getId(), responsibleId))
                .findAny()
                .orElse(null);
        Boolean hasAuthrity = Objects.nonNull(agentUser);
        if (!hasAuthrity) {
            return MapMessage.errorMessage("该责任人不在您负责区域，无法提交申请！");
        }

        return MapMessage.successMessage();
    }

    private MapMessage baseCheck(Long schoolId) {
        List<AgentModifyDictSchoolApply> applyList = agentModifyDictSchoolApplyLoaderClient.findBySchoolId(schoolId);
        if (CollectionUtils.isNotEmpty(applyList)) {
            // 学校如果已经提交申请并且是【审核中】的状态，则提示“该学校已提交申请，无需重复提交申请”
            List<AgentModifyDictSchoolApply> pendingList = applyList.stream().filter(p -> ApplyStatus.PENDING == p.getStatus()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pendingList)) {
                return MapMessage.errorMessage("该学校已提交申请，无需重复提交申请");
            }
            // 学校如果已经提交申请并且是【已通过】的状态且未进行批量操作，则提示“该学校已提交申请，无需重复提交申请”
            boolean hadApprovedButNotResolvedList = applyList.stream().anyMatch(p -> ApplyStatus.APPROVED == p.getStatus() && Boolean.FALSE.equals(p.getResolved()));
            if (hadApprovedButNotResolvedList) {
                return MapMessage.errorMessage("该学校已提交申请，无需重复提交申请");
            }
        }
        return MapMessage.successMessage();
    }

    // targetUser 要将该字典表学校分配给的专员Id
    public MapMessage saveModifyDicSchoolApply(Integer modifyType, School school, String comment, AgentSchoolPopularityType schoolPopularity, Long targetUserId) {

        MapMessage resultMsg = MapMessage.successMessage();
        StringBuilder modifyDesc = new StringBuilder("");
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());

        if (modifyType == AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType()) {
            if (Objects.equals(schoolLevel, SchoolLevel.MIDDLE) || Objects.equals(schoolLevel, SchoolLevel.HIGH)) {
                modifyDesc.append("学校等级：").append(schoolPopularity == null ? "" : schoolPopularity.getLevel()).append("\r\n");
            }
        } else {
            AgentDictSchool dictSchool = agentDictSchoolLoaderClient.findBySchoolId(school.getId());
            if (dictSchool == null) {
                return MapMessage.errorMessage("该学校不在字典表中，无法提交申请");
            }

            AgentSchoolPopularityType dictSchoolPopularity = dictSchool.getSchoolPopularity();
            if (modifyType == AgentModifyDictSchoolApply.ModifyType.DELETE_SCHOOL.getType()) {
                if (Objects.equals(schoolLevel, SchoolLevel.MIDDLE) || Objects.equals(schoolLevel, SchoolLevel.HIGH)) {
                    modifyDesc.append("学校等级：").append(dictSchoolPopularity == null ? "" : dictSchoolPopularity.getLevel());
                }
            } else if (modifyType == AgentModifyDictSchoolApply.ModifyType.UPDATE_POPULARITY.getType()) {
                if (Objects.equals(schoolLevel, SchoolLevel.MIDDLE) || Objects.equals(schoolLevel, SchoolLevel.HIGH)) {
                    modifyDesc.append("学校等级：").append(dictSchoolPopularity == null ? "" : dictSchoolPopularity.getLevel()).append(" --> ").append(schoolPopularity == null ? "" : schoolPopularity.getLevel());
                }
            }
        }

        AuthCurrentUser currentUser = getCurrentUser();
        // 创建字典表调整申请
        AgentModifyDictSchoolApply apply = new AgentModifyDictSchoolApply();
        apply.setApplyType(ApplyType.AGENT_MODIFY_DICT_SCHOOL);
        apply.setModifyType(modifyType);
        apply.setSchoolId(school.getId());
        apply.setSchoolName(school.getCname());
        apply.setUserPlatform(SystemPlatformType.AGENT);
        apply.setSchoolLevel(school.getLevel());
        apply.setSchoolPopularity(schoolPopularity == null ? null : schoolPopularity.getLevel());
        apply.setModifyDesc(modifyDesc.toString());
        apply.setRegionName(this.generateRegionName(school.getRegionCode()));
        apply.setAccount(String.valueOf(currentUser.getUserId()));
        apply.setAccountName(currentUser.getRealName());
        apply.setStatus(ApplyStatus.PENDING);
        apply.setComment(comment);
        if ((modifyType == AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType() || modifyType == AgentModifyDictSchoolApply.ModifyType.UPDATE_RESPONSIBLE.getType())
                && targetUserId != null
                && targetUserId > 0) {
            apply.setTargetUserId(targetUserId);
        }
        Long applyId = agentModifyDictSchoolApplyServiceClient.addApply(apply);

        // 创建工作流
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setStatus("init");
        workFlowRecord.setSourceApp("agent");
        workFlowRecord.setTaskName("字典表调整申请");
        workFlowRecord.setTaskContent(apply.generateSummary());
        workFlowRecord.setLatestProcessorName(currentUser.getRealName());
        workFlowRecord.setCreatorName(currentUser.getRealName());
        workFlowRecord.setCreatorAccount(String.valueOf(currentUser.getUserId()));
        workFlowRecord.setWorkFlowType(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL);

        WorkFlowProcessUser processUser = null;
        resultMsg.setInfo("已为您创建申请，请您及时关注审批流程");
        if (currentUser.isBusinessDeveloper()) { // 当前用户是专员
            // 获取管理者
            AgentUser userManager = baseOrgService.getUserRealManager(currentUser.getUserId());
            if (null != userManager) {
                processUser = new WorkFlowProcessUser();
                processUser.setUserPlatform("agent");
                processUser.setAccount(String.valueOf(userManager.getId()));
                processUser.setAccountName(userManager.getRealName());
            }
        } else if (isManagent(currentUser.getRoleList())) { // 当前用户是市经理以上
            workFlowRecord.setStatus("lv1");
            // 如果不是修改学校等级，如果是学前学校不进行风控审核， 系统自动通过
            if (modifyType != AgentModifyDictSchoolApply.ModifyType.UPDATE_POPULARITY.getType()) {
                processUser = new WorkFlowProcessUser();
                processUser.setUserPlatform("agent");
                processUser.setAccount("system");
                processUser.setAccountName("系统");
            }
            if (modifyType == AgentModifyDictSchoolApply.ModifyType.DELETE_SCHOOL.getType()) {
                resultMsg.setInfo("该学校已成功移出字典表");
            } else if (modifyType == AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType()
                    && !Objects.equals(schoolLevel, SchoolLevel.MIDDLE)
                    && !Objects.equals(schoolLevel, SchoolLevel.HIGH)) {
                resultMsg.setInfo("该学校已成功加入字典表");
            } else if (modifyType == AgentModifyDictSchoolApply.ModifyType.UPDATE_POPULARITY.getType()) {
                resultMsg.setInfo("该学校已成功变更等级");
            } else if (modifyType == AgentModifyDictSchoolApply.ModifyType.UPDATE_RESPONSIBLE.getType()) {
                resultMsg.setInfo("该学校已成功变更负责人");
            }
        }

        MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord, processUser);
        Long workflowId = fetchWorkflowId(mapMessage);
        if (workflowId != null) {
            agentModifyDictSchoolApplyServiceClient.updateWorkflowId(applyId, workflowId);
        }

        if (isManagent(currentUser.getRoleList())) { // 当前用户是市经理以上
            if ((modifyType == AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType()
                    && !Objects.equals(schoolLevel, SchoolLevel.MIDDLE)
                    && !Objects.equals(schoolLevel, SchoolLevel.HIGH))
                    || modifyType == AgentModifyDictSchoolApply.ModifyType.DELETE_SCHOOL.getType()
                    || modifyType == AgentModifyDictSchoolApply.ModifyType.UPDATE_RESPONSIBLE.getType()
                    || school.getLevel() == SchoolLevel.INFANT.getLevel()) { // 非变更学校等级或者是学前学校，系统自动通过
                workFlowServiceClient.processWorkflow("agent", "system", "系统", workflowId, WorkFlowProcessResult.agree, "系统自动通过", null);
            }
        }
        return resultMsg;
    }

    private Long fetchWorkflowId(MapMessage mapMessage) {
        if (!mapMessage.isSuccess()) {
            return null;
        }
        WorkFlowRecord workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
        if (workFlowRecord == null) {
            return null;
        }
        return workFlowRecord.getId();
    }

    public MapMessage checkAndUpdateSchoolExtInfo(Long schoolId, DictSchoolEditParams params) {
        Boolean updateEnglishStartGradeFlag = Boolean.FALSE;
        Boolean updateGradeDataFlag = Boolean.FALSE;
        SchoolExtInfo schoolExtInfo = Optional.ofNullable(schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly()).orElse(new SchoolExtInfo());

        if (Objects.isNull(schoolExtInfo.getEnglishStartGrade()) || schoolExtInfo.getEnglishStartGrade() == 0) {
            updateEnglishStartGradeFlag = Boolean.TRUE;
            if (Objects.equals(params.getEnglishStartGrade(), ClazzLevel.FIRST_GRADE.getLevel()) && Objects.equals(params.getEnglishStartGrade(), ClazzLevel.THIRD_GRADE.getLevel())) {
                MapMessage.errorMessage("英语起始年级有误，请重新选择！");
            }
        }

        Boolean unfinishedGradeClazzData = MapUtils.isEmpty(schoolExtInfo.getGradeClazzCount());
        if (!unfinishedGradeClazzData) {
            for (Map.Entry<String, Integer> entry : schoolExtInfo.getGradeClazzCount().entrySet()) {
                if (Objects.isNull(entry.getValue())) {
                    unfinishedGradeClazzData = true;
                    break;
                }
            }
        }

        Boolean unfinishedGradeStudentData = Objects.isNull(schoolExtInfo.getGradeStudentCount());
        if (!unfinishedGradeStudentData) {
            for (Map.Entry<String, Integer> entry : schoolExtInfo.getGradeStudentCount().entrySet()) {
                if (Objects.isNull(entry.getValue())) {
                    unfinishedGradeStudentData = true;
                    break;
                }
            }
        }

        if (unfinishedGradeClazzData || unfinishedGradeStudentData) {
            updateGradeDataFlag = Boolean.TRUE;
            if (params.getGradeDataList().stream().anyMatch(grade ->
                    Objects.isNull(grade.getClazzNum())
                            || grade.getClazzNum() == 0
                            || Objects.isNull(grade.getStudentNum())
                            || grade.getStudentNum() == 0)) {
                MapMessage.errorMessage("年级信息不全，请重新填写！");
            }
        }

        if (updateEnglishStartGradeFlag) {
            schoolExtInfo.setEnglishStartGrade(params.getEnglishStartGrade());
        }

        if (updateGradeDataFlag) {
            params.getGradeDataList().forEach(grade -> {
                schoolExtInfo.setGradeClazzNum(ClazzLevel.parse(grade.getGrade()), grade.getClazzNum());
                schoolExtInfo.setGradeStudentNum(ClazzLevel.parse(grade.getGrade()), grade.getStudentNum());
            });
        }
        if (updateEnglishStartGradeFlag || updateGradeDataFlag) {
            if (Objects.isNull(schoolExtInfo.getId())) {
                schoolExtInfo.setId(params.getSchoolId());
            }
            schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(schoolExtInfo).getUninterruptibly();
        }
        return MapMessage.successMessage();
    }

    /**
     * 市经理级以上角色
     *
     * @param roles
     * @return
     */
    public Boolean isManagent(List<Integer> roles) {
        return roles.contains(AgentRoleType.Admin.getId())
                || roles.contains(AgentRoleType.Country.getId())
                || roles.contains(AgentRoleType.Region.getId())
                || roles.contains(AgentRoleType.ProvinceManager.getId())
                || roles.contains(AgentRoleType.AreaManager.getId())
                || roles.contains(AgentRoleType.CityManager.getId());
    }
}
