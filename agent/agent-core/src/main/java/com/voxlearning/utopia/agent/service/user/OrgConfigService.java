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

package com.voxlearning.utopia.agent.service.user;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.random.RandomGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.bean.PartnerData;
import com.voxlearning.utopia.agent.bean.SchoolDepartmentInfo;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConfigSchoolLogType;
import com.voxlearning.utopia.agent.constants.AgentUserType;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserCashRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserMaterialBudgetRecordDao;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.persist.entity.AgentSchoolConfigLog;
import com.voxlearning.utopia.agent.persist.entity.AgentUserCashRecord;
import com.voxlearning.utopia.agent.persist.entity.AgentUserMaterialBudgetRecord;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.budget.AgentKpiBudgetService;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombUserService;
import com.voxlearning.utopia.agent.service.log.AgentSchoolConfigLogService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDateConfigService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.*;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupRegionServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupUserServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserSchoolServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OrgConfigService
 *
 * @author song.wang
 * @date 2016/6/20
 */
@Named
public class OrgConfigService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private BaseOrgService baseOrgService;
    @Inject private BaseUserService baseUserService;
    @Inject private AgentRegionService agentRegionService;
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private AgentGroupServiceClient agentGroupServiceClient;
    @Inject private AgentGroupRegionLoaderClient agentGroupRegionLoaderClient;
    @Inject private AgentGroupRegionServiceClient agentGroupRegionServiceClient;

    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private AgentGroupUserServiceClient agentGroupUserServiceClient;
    @Inject private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;
    @Inject private AgentUserSchoolServiceClient agentUserSchoolServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private AgentUserMaterialBudgetRecordDao agentUserMaterialBudgetRecordDao;
    @Inject private AgentUserCashRecordDao agentUserCashRecordDao;
    @Inject private BaseGroupService baseGroupService;
    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject private AgentDateConfigService agentDateConfigService;
    @Inject private AgentSchoolConfigLogService agentSchoolConfigLogService;
    //    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
//    @Inject private AgentGroupSchoolServiceClient agentGroupSchoolServiceClient;
//    @Inject private AgentGroupSchoolLoaderClient agentGroupSchoolLoaderClient;
    @Inject private AgentKpiBudgetService agentKpiBudgetService;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private AgentMaterialBudgetService agentMaterialBudgetService;
    @Inject
    private BaseDictService baseDictService;
    @Inject
    private AgentGroupSupport agentGroupSupport;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private HoneycombUserService honeycombUserService;
    @Inject
    private AgentPartnerService agentPartnerService;

    public MapMessage updateAgentUserBud(XSSFWorkbook workbook) {
        MapMessage msg = MapMessage.successMessage();
        List<String> errorList = new ArrayList<>();
        List<String> rightList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows = 1;
        if (sheet != null) {
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    String accountName = getStringCellValue(row.getCell(0));
                    if (StringUtils.isBlank(accountName)) {
                        errorList.add(rows + "行用户名为空,");
                        continue;
                    }
                    AgentUser user = baseUserService.getByAccountName(accountName);
                    if (user == null) {
                        errorList.add(rows + "行账户对应的用户不存在,");
                        continue;
                    }
                    Float materielBudget = SafeConverter.toFloat(getStringCellValue(row.getCell(1)));
                    if (!isPositiveNum(materielBudget)) {
                        errorList.add(rows + "行预算不能为空或者0,");
                        continue;
                    }
                    Float usableCashAmount = SafeConverter.toFloat(getStringCellValue(row.getCell(2)));
                    if (!isPositiveNum(usableCashAmount)) {
                        errorList.add(rows + "行余额不能为空或者0,");
                        continue;
                    }
                    if (materielBudget < usableCashAmount) {
                        errorList.add(rows + "行余额不能大于预算,");
                        continue;
                    }
                    user.setCashAmount(usableCashAmount);
                    user.setUsableCashAmount(usableCashAmount);
                    user.setMaterielBudget(materielBudget);
                    baseUserService.updateAgentUser(user);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        if (com.voxlearning.alps.core.util.CollectionUtils.isEmpty(errorList)) {
            rightList.add("全部用户预算更新成功");
            msg.add("right", rightList);
            msg.setSuccess(true);
        } else {
            rightList.add("预算更新有错误");
            msg.add("right", rightList);
            msg.add("error", errorList);
            msg.setSuccess(false);
        }
        return msg;
    }

    private static boolean isPositiveNum(Float... budget) {
        for (Float num : budget) {
            if (num == null || num < 0L) {
                return false;
            }
        }
        return true;
    }

    private static String getStringCellValue(XSSFCell cell) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(new BigDecimal(cell.getNumericCellValue()));
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return com.voxlearning.alps.core.util.StringUtils.deleteWhitespace(cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return "";
        }

        return null;
    }

    /**
     * 创建部门
     *
     * @param groupName       部门名称
     * @param desc            描述
     * @param parentId        上级部门ID
     * @param groupRoleType   部门级别
     * @param serviceTypeList 业务类型
     * @return mapMessage
     */
    public MapMessage addAgentGroup(String groupName, String desc, Long parentId, AgentGroupRoleType groupRoleType, String logo, List<AgentServiceType> serviceTypeList) {
        if (StringUtils.isEmpty(groupName)) {
            return MapMessage.errorMessage("部门名称为空");
        }
//        if (groupRoleType == null) {
//            return MapMessage.errorMessage("部门级别为空");
//        }
        if (baseOrgService.getGroupByName(groupName) != null) {
            return MapMessage.errorMessage("名称为" + groupName + "的部门已经存在");
        }
        if (baseOrgService.getGroupById(parentId) == null) {
            return MapMessage.errorMessage("上级部门为空");
        }

        AgentGroup agentGroup = new AgentGroup();
        agentGroup.setGroupName(StringUtils.trim(groupName));
        if (!StringUtils.isEmpty(desc)) {
            agentGroup.setDescription(desc);
        }
        agentGroup.setParentId(parentId);
        if (groupRoleType != null) {
            agentGroup.setRoleId(groupRoleType.getId());
        }
        agentGroup.setLogo(logo);
        //如果是业务部
        if (CollectionUtils.isNotEmpty(serviceTypeList)) {
            List<String> serviceTypeNameList = serviceTypeList.stream().map(AgentServiceType::name).collect(Collectors.toList());
            agentGroup.setServiceType(StringUtils.join(serviceTypeNameList, ","));
        }
        Long groupId = agentGroupServiceClient.persist(agentGroup);
        return MapMessage.successMessage().add("groupId", groupId);
    }

    public MapMessage updateGroupRegion(Long groupId, Set<Integer> regionSet) {
        MapMessage message = MapMessage.successMessage();
        Map<Integer, Boolean> addResultMap = new HashMap<>();

        // 获取部门原来负责的区域
        List<AgentGroupRegion> groupRegionList = baseOrgService.getGroupRegionByGroup(groupId);
        Set<Integer> existCoveredRegions = new HashSet<>();
        existCoveredRegions.addAll(agentRegionService.getCountyCodes(groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet())));

        // 用户选择的所有区县
        Set<Integer> selectCoveredRegions = new HashSet<>();
        selectCoveredRegions.addAll(agentRegionService.getCountyCodes(regionSet));

        // 合并区域并保存
        Set<Integer> targetRegions = mergeRegions(selectCoveredRegions);
        targetRegions.forEach(p -> {
            MapMessage message1 = addGroupRegion(groupId, p);
            addResultMap.put(p, message1.isSuccess());
        });

        boolean result = true;
        if (MapUtils.isNotEmpty(addResultMap)) {
            result = addResultMap.values().stream().reduce((x, y) -> x && y).get();
        }

        // 更新成时
        if (result) {
            // 删除原来的区域
            groupRegionList.forEach(p -> {
                if (!targetRegions.contains(p.getRegionCode())) {
                    p.setDisabled(true);
                    agentGroupRegionServiceClient.update(p.getId(), p);
                }
            });

            // 获取保存后最新的区域
            List<AgentGroupRegion> newGroupRegionList = baseOrgService.getGroupRegionByGroup(groupId);
            Set<Integer> coveredRegions = new HashSet<>();
            coveredRegions.addAll(agentRegionService.getCountyCodes(newGroupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet())));

            // 部门不再负责的区县（有权限删除的情况下，调整子部门负责区域，及相应的学校）
            Set<Integer> needRemoveRegions = existCoveredRegions.stream().filter(p -> !coveredRegions.contains(p)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(needRemoveRegions)) {
                deleteGroupSchoolAndUserSchoolByRegionCode(groupId, needRemoveRegions);
                adjustSubGroupRegions(groupId);
            }


        }


        String errorMessageStr = "区域 ";
        if (!result && !addResultMap.isEmpty()) {
            for (Integer key : addResultMap.keySet()) {
                Boolean v = addResultMap.get(key) != null ? addResultMap.get(key) : false;
                if (!v) {
                    errorMessageStr += key + "，";
                }
            }
            errorMessageStr += "  保存失败！";
        }

        message.setSuccess(result);
        message.setInfo(errorMessageStr);
        return message;
    }

    // 调整子部门负责区域
    private void adjustSubGroupRegions(Long groupId) {
        List<Integer> regionCodes = baseOrgService.getGroupRegionCodeList(groupId);
        Set<Integer> parentGroupCoveredRegions = new HashSet<>(); // 部门负责的所有区县
        parentGroupCoveredRegions.addAll(agentRegionService.getCountyCodes(regionCodes));

        List<AgentGroup> subGroups = baseOrgService.getGroupListByParentId(groupId);
        if (CollectionUtils.isNotEmpty(subGroups)) {
            subGroups.forEach(childGroup -> {
                List<AgentGroupRegion> groupRegionList = baseOrgService.getGroupRegionByGroup(childGroup.getId());
                Set<Integer> childGroupCoveredRegions = new HashSet<>(); // 部门负责的所有区县
                childGroupCoveredRegions.addAll(agentRegionService.getCountyCodes(groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet())));


                // 子部门的权限不在父部门的权限范围内的情况， 调整子部门的区域范围
                if (childGroupCoveredRegions.stream().anyMatch(p -> !parentGroupCoveredRegions.contains(p))) {
                    Set<Integer> coveredRegions = childGroupCoveredRegions.stream().filter(parentGroupCoveredRegions::contains).collect(Collectors.toSet());

                    Set<Integer> newRegions = mergeRegions(coveredRegions);
                    newRegions.forEach(n -> addGroupRegion(childGroup.getId(), n));
                    groupRegionList.forEach(g -> {
                        if (!newRegions.contains(g.getRegionCode())) {
                            g.setDisabled(true);
                            agentGroupRegionServiceClient.update(g.getId(), g);
                        }
                    });

                    adjustSubGroupRegions(childGroup.getId());
                }

            });
        }
    }

    private void deleteGroupSchoolAndUserSchoolByRegionCode(Long groupId, Collection<Integer> regionCodes) {

        if (CollectionUtils.isEmpty(regionCodes)) {
            return;
        }
        List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
        List<Long> groupIds = new ArrayList<>();
        groupIds.addAll(subGroupList.stream().map(AgentGroup::getId).collect(Collectors.toSet()));
        groupIds.add(groupId);
//        groupIds.forEach(g -> agentGroupSchoolServiceClient.deleteByGroupAndRegions(g, regionCodes));

        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByGroups(groupIds);
        if (CollectionUtils.isNotEmpty(groupUsers)) {
            List<AgentUserSchool> targetUserSchools = new ArrayList<>();
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            for (Long userId : userIds) {
                List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolByUser(userId);
                if (CollectionUtils.isEmpty(userSchoolList)) {
                    continue;
                }
                targetUserSchools.addAll(userSchoolList.stream().filter(p -> regionCodes.contains(p.getRegionCode())).collect(Collectors.toList()));
            }
            deleteUserSchools(targetUserSchools);
        }
    }


    // 将区县向上合并  regionCodes中的所有区域必须同一级别的区域（即全部为区县，或全部为城市）
    private Set<Integer> mergeRegions(Collection<Integer> regionCodes) {
        Set<Integer> result = new HashSet<>();
        if (CollectionUtils.isEmpty(regionCodes)) {
            return result;
        }

        Set<Integer> targetRegions = new HashSet<>();
        Set<Integer> resolvedRegions = new HashSet<>();
        for (Integer regionCode : regionCodes) {
            if (resolvedRegions.contains(regionCode)) {
                continue;
            }
            Set<Integer> siblingRegions = getSiblingRegions(regionCode);
            if (CollectionUtils.isEmpty(siblingRegions)) {
                result.add(regionCode);
                resolvedRegions.add(regionCode);
            } else {
                if (!regionCodes.containsAll(siblingRegions)) {
                    Set<Integer> tmpRegions = regionCodes.stream().filter(siblingRegions::contains).collect(Collectors.toSet());
                    result.addAll(tmpRegions);
                    resolvedRegions.addAll(tmpRegions);
                } else {
                    Integer parentRegionCode = getParentRegionCode(regionCode);
                    if (parentRegionCode == null || parentRegionCode == 0) {
                        Set<Integer> tmpRegions = regionCodes.stream().filter(siblingRegions::contains).collect(Collectors.toSet());
                        result.addAll(tmpRegions);
                        resolvedRegions.addAll(tmpRegions);
                    } else {
                        targetRegions.add(parentRegionCode);
                        resolvedRegions.addAll(siblingRegions);
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(targetRegions)) {
            result.addAll(mergeRegions(targetRegions));
        }

        return result;

    }

    private Integer getParentRegionCode(Integer regionCode) {
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return null;
        }
        return exRegion.getParentRegionCode();
    }

    private Set<Integer> getSiblingRegions(Integer regionCode) {
        Set<Integer> result = new HashSet<>();
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return result;
        }
        Integer parentRegionCode = exRegion.getParentRegionCode();
        ExRegion parentExRegion = raikouSystem.loadRegion(parentRegionCode);
        if (parentExRegion == null || CollectionUtils.isEmpty(parentExRegion.getChildren())) {
            return result;
        }
        return parentExRegion.getChildren().stream().map(ExRegion::getId).collect(Collectors.toSet());
    }


    // 获取区域code中的最上层节点，
    public Set<Integer> getTopRegions(Collection<Integer> regions) {
        Set<Integer> result = new HashSet<>();
        if (CollectionUtils.isEmpty(regions)) {
            return result;
        }
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regions);
        exRegionMap.values().forEach(p -> {
            if (p.fetchRegionType() == RegionType.PROVINCE) {
                result.add(p.getId());
            } else if (p.fetchRegionType() == RegionType.CITY) {
                if (regions.contains(p.getProvinceCode())) {
                    result.add(p.getProvinceCode());
                } else {
                    result.add(p.getId());
                }
            } else if (p.fetchRegionType() == RegionType.COUNTY) {
                if (regions.contains(p.getProvinceCode())) {
                    result.add(p.getProvinceCode());
                } else if (regions.contains(p.getCityCode())) {
                    result.add(p.getCityCode());
                } else {
                    result.add(p.getId());
                }
            }
        });
        return result;
    }

    // 为部门配置区域权限
    public MapMessage addGroupRegion(Long groupId, Integer regionCode) {
        AgentGroupRegion agentGroupRegion = baseOrgService.getGroupRegion(groupId, regionCode);
        // 该部门已负责该区域
        if (agentGroupRegion != null) {
            return MapMessage.successMessage();
        }

        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        AgentGroupRegion parentRegion = baseOrgService.getParentGroupRegion(groupId, regionCode);
        //判断当前部门和上级部门在该地区的权限是否匹配
        if (agentGroup.getParentId() != 0) { // 有上级部门
            AgentGroup parentGroup = baseOrgService.getGroupById(agentGroup.getParentId());
            if (parentGroup == null || (parentGroup.getParentId() != 0 && parentRegion == null)) {
                return MapMessage.errorMessage("当前部门的上级部门并不负责该区域");
            }
        }

        AgentGroupRegion groupRegion = new AgentGroupRegion();
        groupRegion.setGroupId(groupId);
        groupRegion.setRegionCode(regionCode);
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        if (exRegion == null) {
            return MapMessage.errorMessage("选择的区域不存在");
        }
        groupRegion.setRegionName(exRegion.getName());
        agentGroupRegionServiceClient.persist(groupRegion);

        return MapMessage.successMessage();
    }


    /**
     * 区域树形结构
     *
     * @param groupId 部门ID
     * @return tree List
     */
    public List<Map<String, Object>> loadMarkedGroupRegionTreeByGroupId(Long groupId) {
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (agentGroup == null) {
            return Collections.emptyList();
        }
        return loadMarkedGroupRegionTreeByGroup(agentGroup);
    }

    public List<Map<String, Object>> loadMarkedGroupRegionTreeByGroup(AgentGroup agentGroup) {
        List<Map<String, Object>> groupRegionTreeList = baseOrgService.loadGroupRegionTreeByGroupId(agentGroup.getParentId());

        List<String> regionList = new ArrayList<>();
        List<AgentGroupRegion> groupRegionList = baseOrgService.getGroupRegionByGroup(agentGroup.getId());
        if (CollectionUtils.isNotEmpty(groupRegionList)) {
            groupRegionList.forEach(p -> regionList.add(String.valueOf(p.getRegionCode())));
        }

        if (CollectionUtils.isNotEmpty(regionList)) {
            agentRegionService.markSelectedRegion(groupRegionTreeList, regionList);
        }
        return groupRegionTreeList;
    }

    /**
     * 获取部门负责的区域数据
     *
     * @param groupId 部门ID
     * @return list
     */
    public List<Map<String, Object>> getGroupRegionData(Long groupId) {
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (agentGroup == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retList = new ArrayList<>();
        List<Integer> regionCodeList = baseOrgService.getGroupRegionCodeList(groupId);
        if (CollectionUtils.isNotEmpty(regionCodeList)) {
            Set<Integer> topRegions = getTopRegions(regionCodeList);
            retList.addAll(topRegions.stream().map(p -> getRegionDataForGroup(groupId, p)).collect(Collectors.toList()));
        }
        return retList;
    }

    /**
     * 为部门配置区域时，返回该区域的具体可设置情况
     *
     * @param groupId    部门ID
     * @param regionCode 区域Code
     * @return map
     */
    public Map<String, Object> getRegionDataForGroup(Long groupId, Integer regionCode) {
        Map<String, Object> retMap = new HashMap<>();
        ExRegion exRegion = raikouSystem.loadRegion(regionCode);
        retMap.put("regionCode", regionCode);
        retMap.put("regionType", exRegion == null ? RegionType.UNKNOWN : exRegion.fetchRegionType());
        retMap.put("provinceName", exRegion == null ? "" : exRegion.getProvinceName());
        retMap.put("cityName", exRegion == null ? "" : exRegion.getCityName());
        retMap.put("countyName", exRegion == null ? "" : exRegion.getCountyName());

        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        AgentGroupRegion parentRegion = baseOrgService.getParentGroupRegion(groupId, regionCode);
        if (exRegion == null || (agentGroup.getParentId() != 0 && parentRegion == null)) { // 区域不存在 或者 有上级部门, 上级部门不负责该区域
            retMap.put("enabled", false);
            retMap.put("groupName", "");
            retMap.put("select", false);
            return retMap;
        }
        // parentRegion == null 表示没有上级部门，是全国级别的部门
        // 2018/02/06  部门负责的区域权限调整： 多个部门可以同时负责同一个区域，只要不超过上级部门负责的区域范围即可
        setGroupRegionMap(retMap, parentRegion, agentGroup, regionCode);
        return retMap;
    }


    private void setGroupRegionMap(Map<String, Object> map, AgentGroupRegion parentRegion, AgentGroup group, Integer regionCode) {
        // 获取部门负责的区域列表
        List<Integer> groupRegionCodeList = baseOrgService.getGroupRegionCodeList(group.getId());
        if (group.getParentId() == 0) { // 无上级部门（市场部）
            if (groupRegionCodeList.contains(regionCode)) { // 已经负责该区域
                map.put("enabled", true);
                map.put("groupName", "");
                map.put("select", true);
            } else {
                map.put("enabled", true);
                map.put("groupName", "");
                map.put("select", false);
            }
        } else {  // 有上级部门
            if (groupRegionCodeList.contains(regionCode)) { // 已经负责该区域
                map.put("enabled", true);
                map.put("groupName", "");
                map.put("select", true);
            } else if (parentRegion != null) {   // 本部门不负责该区域，但是上级部门负责该区域
                map.put("enabled", true);
                map.put("groupName", "");
                map.put("select", false);
            } else {  // 本部门不负责该区域，上级部门也不负责该区域
                map.put("enabled", false);
                map.put("groupName", "");
                map.put("select", false);
            }
        }
    }


    /**
     * 用户名是否存在
     *
     * @param accountName 用户名
     * @return 是否存在
     */
    public boolean judgeUserExistByAccount(String accountName) {
        return baseUserService.getByAccountName(accountName) != null;
    }

    public boolean judgeUserExistByMobile(String mobile) {
        return baseUserService.getByMobile(mobile) != null;
    }

    /**
     * 添加系统用户
     *
     * @param realName          用户名
     * @param accountName       账号名
     * @param password          密码
     * @param tel               手机号
     * @param cashDeposit       保证金金额
     * @param bankName          开户行名称
     * @param bankHostname      开户人姓名
     * @param bankAccount       银行帐号
     * @param contractStartDate 合同开始时间
     * @param contractEndDate   合同结束时间
     * @param contractNumber    合同编号
     * @param address           地址
     * @param userComment       备注
     * @param avatar            用户头像
     * @return MapMessage
     */
    public MapMessage addAgentUser(String realName, String accountName, String password, String tel,
                                   Integer cashDeposit, String bankName, String bankHostname, String bankAccount,
                                   Date contractStartDate, Date contractEndDate, String contractNumber, String address,
                                   String userComment, String avatar, Float materielBudget, String accountNumber) {

        if (StringUtils.isEmpty(password)) {
            return MapMessage.errorMessage("密码不能为空！");
        }
        if (agentUserLoaderClient.findByName(StringUtils.trim(accountName)) != null) {
            return MapMessage.errorMessage("已存在相同账号名的用户！");
        }
        if (StringUtils.isEmpty(realName)) {
            return MapMessage.errorMessage("姓名不能为空！");
        }
//        List<AgentUser> exsitAgentUserList = baseOrgService.getUserByRealName(realName);
//        if (CollectionUtils.isNotEmpty(exsitAgentUserList)){
//            return MapMessage.errorMessage("已存在相同姓名的用户！");
//        }


        AgentUser agentUser = new AgentUser();
        agentUser.setAccountName(StringUtils.trim(accountName));
        agentUser.setRealName(realName);

        Password passWd = encryptPassword(password);
        agentUser.setPasswd(passWd.getPassword());
        agentUser.setPasswdSalt(passWd.getSalt());

        agentUser.setTel(tel);
        agentUser.setCashDeposit(cashDeposit);
        agentUser.setBankName(bankName);
        agentUser.setBankHostName(bankHostname);
        agentUser.setBankAccount(bankAccount);

        agentUser.setContractStartDate(contractStartDate);
        agentUser.setContractEndDate(contractEndDate);
        agentUser.setContractNumber(contractNumber);
        agentUser.setAddress(address);

        agentUser.setStatus(AgentUserType.INITIAL.getStatus());
        agentUser.setMaterielBudget(materielBudget);
        agentUser.setCashAmount(materielBudget);
        agentUser.setPointAmount(0f);
        agentUser.setUsableCashAmount(materielBudget);
        agentUser.setUsablePointAmount(0f);

        if (!StringUtils.isEmpty(userComment)) {
            agentUser.setUserComment(userComment);
        }
        if (!StringUtils.isEmpty(avatar)) {
            agentUser.setAvatar(avatar);
        }
        if (StringUtils.isNotBlank(accountNumber)) {
            agentUser.setAccountNumber(String.format("%04d", SafeConverter.toLong(accountNumber)));
        }
        Long userId = baseUserService.createAgentUser(agentUser);
        MapMessage message = MapMessage.successMessage();
        message.put("userId", userId);
        agentUser.setId(userId);
        agentUser.setMaterielBudget(null);
        agentUser.setCashAmount(null);
        message.put("user", agentUser);

        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setMobile(tel);
        smsMessage.setType(SmsType.MARKET_ADD_AGENT_USER.name());
        smsMessage.setSmsContent("「天权」亲，您的市场账号已创建完成，用户名：" + accountName + "，密码：" + password + "，请及时登录天玑或天权修改；PC端天权登录网址：http://marketing.oaloft.com；APP端天玑下载地址：http://t.cn/RGnZayC");
        smsServiceClient.getSmsService().sendSms(smsMessage);

        return message;
    }

    public MapMessage bindHoneycombAccount(Long userId, String mobile) {

        AgentUser user = baseOrgService.getUser(userId);
        if (user == null) {
            return MapMessage.errorMessage("该天玑用户不存在");
        }
        List<Long> honeycombUserIds = honeycombUserService.getHoneycombUserIds(userId);
        if (CollectionUtils.isNotEmpty(honeycombUserIds)) {
            return MapMessage.errorMessage("您已绑定蜂巢用户！");
        }

        return honeycombUserService.bindAgentUser(mobile, userId);
    }

    public MapMessage addAgentUserRecord(AgentUser agentUser, Long userId, Integer budgetOpt, Double materielBudget, String adjust_cause) {
        if (budgetOpt != 0 && materielBudget != 0) {
            if (StringUtils.isBlank(adjust_cause)) {
                return MapMessage.errorMessage("物料预算有调整，需填写调整原因。");
            }
            float changedData = MathUtils.floatMultiply(budgetOpt, materielBudget.floatValue());

            float preBudget = agentUser.getMaterielBudget() == null ? 0 : agentUser.getMaterielBudget();
            float f1 = MathUtils.floatAdd(preBudget, changedData);
            agentUser.setMaterielBudget(f1 >= 0 ? f1 : 0);

            float preCashAmount = agentUser.getCashAmount() == null ? 0 : agentUser.getCashAmount();
            float f2 = MathUtils.floatAdd(preCashAmount, changedData);
            agentUser.setCashAmount(f2 >= 0 ? f2 : 0);

            float preUsableCashAmount = agentUser.getUsableCashAmount() == null ? 0 : agentUser.getUsableCashAmount();
            float f3 = MathUtils.floatAdd(preUsableCashAmount, changedData);
            agentUser.setUsableCashAmount(f3 >= 0 ? f3 : 0);

            // 添加用户物料预算变化记录
            baseUserService.addAgentUserCashDataRecord(1, agentUser.getId(), userId, preBudget, agentUser.getMaterielBudget(), changedData, StringUtils.formatMessage("{}", adjust_cause));
            // 添加用户余额变动记录
            baseUserService.addAgentUserCashDataRecord(2, agentUser.getId(), userId, preCashAmount, agentUser.getCashAmount(), changedData, StringUtils.formatMessage("【预算调整】{}", adjust_cause));
        }
        return MapMessage.successMessage();
    }

    /**
     * 生成密码
     *
     * @param password 密码串
     * @return 密码实体
     */
    public Password encryptPassword(String password) {
        try {
            return RandomGenerator.generatePassword(password);
        } catch (Exception ex) {
            logger.error("Payment password is invalid: {}", password);
        }
        return null;
    }


    /**
     * 为部门添加用户
     *
     * @param groupId      部门ID
     * @param userId       用户ID
     * @param userRoleType 用户角色
     * @return MapMessage
     */
    public MapMessage addGroupUser(Long groupId, Long userId, AgentRoleType userRoleType) {
        if (baseOrgService.getGroupManager(groupId) != null && (userRoleType == AgentRoleType.BUManager || userRoleType == AgentRoleType.Region || userRoleType == AgentRoleType.AreaManager || userRoleType == AgentRoleType.CityManager)) {
            return MapMessage.errorMessage("该部门已经有" + userRoleType.getRoleName() + "，不可重复设置");
        }
        AgentGroupUser groupUser = baseOrgService.getGroupUser(groupId, userId);
        if (groupUser == null) { //用户不在该部门里面
            AgentUser agentUser = baseUserService.getUser(userId);
            if (agentUser != null && agentUser.isValidUser()) {
                AgentGroupUser agentGroupUser = new AgentGroupUser();
                agentGroupUser.setGroupId(groupId);
                agentGroupUser.setUserId(userId);
                agentGroupUser.setUserRoleId(userRoleType.getId());
                agentGroupUserServiceClient.persist(agentGroupUser);
            }
        }
        return MapMessage.successMessage();
    }


    /**
     * 调整用户在部门中的角色
     *
     * @param groupId  部门ID
     * @param userId   用户ID
     * @param roleType 用户角色
     * @return
     */
    public MapMessage updateGroupUserRole(Long groupId, Long userId, AgentRoleType roleType) {
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group == null) {
            return MapMessage.errorMessage("部门不存在");
        }

        List<AgentRoleType> roleTypeList = baseOrgService.getAgentRoleTypeList(group.fetchGroupRoleType());
        if (!roleTypeList.contains(roleType)) {
            return MapMessage.errorMessage("您选择的角色不在该部门的权限范围内");
        }


        if (roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager || roleType == AgentRoleType.CityManager) {
            if (baseOrgService.getGroupManager(groupId) != null) {
                return MapMessage.errorMessage("该部门已经存在" + roleType.getRoleName() + "，不能有多个该角色的用户！");
            }
        }

        AgentGroupUser groupUser = baseOrgService.getGroupUser(groupId, userId);
        if (groupUser == null) {
            return MapMessage.errorMessage("用户不在指定的部门");
        }

        //删除该用户名下的学校
        List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolByUser(groupUser.getUserId());
        if (CollectionUtils.isNotEmpty(userSchoolList)) {
            userSchoolList.forEach(p -> {
                p.setDisabled(true);
                agentUserSchoolServiceClient.update(p.getId(), p);
            });
        }
        //修改用户角色
        groupUser.setUserRoleId(roleType.getId());
        agentGroupUserServiceClient.update(groupUser.getId(), groupUser);
        if (roleType == AgentRoleType.CityManager) {
            // 删除用户本月和下月的预算
            List<Integer> monthList = new ArrayList<>();
            monthList.add(SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM")));
            monthList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.addMonth(new Date(), 1), "yyyyMM")));
            monthList.forEach(t -> agentKpiBudgetService.disableUserBudget(groupUser.getUserId(), groupUser.getGroupId(), t));
        }
        return MapMessage.successMessage();

    }

    /**
     * 调整用户所属部门
     *
     * @param oldGroupId 原来的部门ID
     * @param newGroupId 新部门的ID
     * @param userId     用户ID
     * @param roleType   用户角色
     * @return
     */
    public MapMessage changeGroupForUser(Long oldGroupId, Long newGroupId, Long userId, AgentRoleType roleType) {
        AgentGroup group = baseOrgService.getGroupById(newGroupId);
        if (group == null) {
            return MapMessage.errorMessage("部门不存在");
        }

        List<AgentRoleType> roleTypeList = baseOrgService.getAgentRoleTypeList(group.fetchGroupRoleType());
        if (!roleTypeList.contains(roleType)) {
            return MapMessage.errorMessage("您选择的角色不在该部门的权限范围内");
        }

        if (baseOrgService.getGroupManager(newGroupId) != null && (roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager || roleType == AgentRoleType.CityManager)) {
            return MapMessage.errorMessage("该部门已经存在" + roleType.getRoleName() + "，不能有多个该角色的用户！");
        }

        AgentGroupUser groupUser = baseOrgService.getGroupUser(oldGroupId, userId);
        if (groupUser == null) {
            return MapMessage.errorMessage("用户不在指定的部门");
        }

        //部门中的人员账号变更，不在此部门时，将该人员名下剩余的物料费用划归到部门的“未分配”费用中
        AgentUser agentUser = baseOrgService.getUser(userId);
        String userName = "";
        if (agentUser != null) {
            userName = agentUser.getAccountName();
        }
        String modifyReason = StringUtils.formatMessage("调整部门，账号:{}，人员余额划归到部门“未分配”费用", userName);
        agentMaterialBudgetService.changeUserBalanceToGroup(oldGroupId, userId, modifyReason);

        //删除该用户名下的学校
        List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolByUser(groupUser.getUserId());
        if (CollectionUtils.isNotEmpty(userSchoolList)) {
            userSchoolList.forEach(p -> {
                p.setDisabled(true);
                agentUserSchoolServiceClient.update(p.getId(), p);
            });
        }

        //将用户从旧的部门中删除
        groupUser.setDisabled(true);
        agentGroupUserServiceClient.update(groupUser.getId(), groupUser);
        // 删除用户本月和下月的预算
        List<Integer> monthList = new ArrayList<>();
        monthList.add(SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM")));
        monthList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.addMonth(new Date(), 1), "yyyyMM")));
        monthList.forEach(t -> agentKpiBudgetService.disableUserBudget(groupUser.getUserId(), groupUser.getGroupId(), t));

        //如果调整了部门
        if (!Objects.equals(oldGroupId, newGroupId)) {
            //删除该用户对应的权限内的SchoolLevel缓存
            deleteUserSchoolLevelCacheKey(oldGroupId, userId);
        }
        return addGroupUser(newGroupId, userId, roleType);
    }


    /**
     * 获取专员或代理负责的学校列表
     *
     * @param groupId 部门ID
     * @param userId  用户ID
     * @return list
     */
    public List<School> getUserSchoolDataList(Long groupId, Long userId) {
        AgentGroupUser groupUser = baseOrgService.getGroupUser(groupId, userId);
        if (groupUser == null || (!Objects.equals(groupUser.getUserRoleId(), AgentRoleType.CityAgent.getId()) && !Objects.equals(groupUser.getUserRoleId(), AgentRoleType.BusinessDeveloper.getId()) && !Objects.equals(groupUser.getUserRoleId(), AgentRoleType.CityAgentLimited.getId()))) {
            return Collections.emptyList();
        }

        List<Long> schoolIdList = baseOrgService.getUserSchools(userId);
        if (CollectionUtils.isEmpty(schoolIdList)) {
            return Collections.emptyList();
        }
        List<School> retList = new ArrayList<>();
        for (Long schoolId : schoolIdList) {
            School school = raikouSystem.loadSchool(schoolId);
            if (school != null) {
                retList.add(school);
            }
        }
        return retList;
    }


    /**
     * 给指定用户配置学校时，根据schoolId查找学校列表
     *
     * @param groupId      部门ID
     * @param userId       用户ID
     * @param schoolIdList 学校ID列表
     * @return map
     */
    public Map<String, Object> searchSchoolsBySchoolIdList(Long groupId, Long userId, Set<Long> schoolIdList) {
        if (CollectionUtils.isEmpty(schoolIdList)) {
            return Collections.emptyMap();
        }
        Map<String, Object> retMap = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();

        List<Integer> groupRegionCodeList = baseOrgService.getGroupRegionCountyCodeList(groupId);
        List<AgentDictSchool> agentDictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(schoolIdList);
        List<Long> groupSchoolList = baseOrgService.getManagedSchoolListByGroupId(groupId);

        Set<Long> copySchoolIdList = new HashSet<>(schoolIdList);//不在字典表里的学校列表
        Set<Long> invaildSchoolIdList = new HashSet<>();//不在本部门的学校列表

        Map<String, Object> item = null;
        for (AgentDictSchool agentDictSchool : agentDictSchoolList) {
            copySchoolIdList.remove(agentDictSchool.getSchoolId());
            // 不在当前部门负责的区域
            if (!groupRegionCodeList.contains(agentDictSchool.getCountyCode())) {
                invaildSchoolIdList.add(agentDictSchool.getSchoolId());
                continue;
            }

            // 不是当前部门负责的学校
            if (!groupSchoolList.contains(agentDictSchool.getSchoolId())) {
                invaildSchoolIdList.add(agentDictSchool.getSchoolId());
                continue;
            }

            School school = raikouSystem.loadSchool(agentDictSchool.getSchoolId());
            if (school == null) {
                invaildSchoolIdList.add(agentDictSchool.getSchoolId());
                continue;
            }

            // 获取负责该学校的用户列表
            List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolBySchool(school.getId());
            List<Long> userIdList = userSchools.stream().map(AgentUserSchool::getUserId).collect(Collectors.toList());

            // 获取本部门中负责该学校， 并且和指定用户角色相同的用户列表
            List<Long> sameRoleUserList = new ArrayList<>();
            List<Long> groupUserIdList = baseOrgService.getGroupUserIds(groupId); // 获取本部门的人员列表
            Map<Long, Integer> userRoleMap = baseOrgService.getGroupUserRoleMapByGroupId(groupId);
            Integer userRole = userRoleMap.get(userId);
            for (Long id : userIdList) {
                if (!groupUserIdList.contains(id)) { // 本部门不包括该人员，其他部门的人负责该学校
                    invaildSchoolIdList.add(agentDictSchool.getSchoolId());
                    continue;
                }
                Integer otherUserRole = userRoleMap.get(id);
                if (otherUserRole != null && Objects.equals(otherUserRole, userRole) && !sameRoleUserList.contains(id) && !Objects.equals(id, userId)) {
                    sameRoleUserList.add(id);
                }
            }
            if (invaildSchoolIdList.contains(agentDictSchool.getSchoolId())) {
                continue;
            }

            item = new HashMap<>();
            item.put("schoolId", school.getId());
            item.put("schoolName", school.getCname());
            item.put("schoolLevel", school.getLevel());

            if (CollectionUtils.isNotEmpty(userIdList)) { // 该学校已经有负责人了
                if (userIdList.contains(userId)) { //负责人是指定的用户
                    item.put("enabled", true);
                    item.put("userName", "");
                    item.put("selected", true);
                } else {
                    if (CollectionUtils.isEmpty(sameRoleUserList)) { // 负责该学校的人和指定的用户不是同样的角色，无法将该用户下的学校更换到自己名下
                        AgentUser agentUser = baseUserService.getUser(userIdList.get(0));
                        item.put("enabled", false);
                        item.put("userName", agentUser != null ? agentUser.getRealName() : "");
                        item.put("selected", false);
                    } else {
                        AgentUser agentUser = baseUserService.getUser(sameRoleUserList.get(0));
                        item.put("enabled", true);
                        item.put("userName", agentUser != null ? agentUser.getRealName() : "");
                        item.put("selected", false);
                    }
                }
            } else {
                item.put("enabled", true);
                item.put("userName", "");
                item.put("selected", false);
            }
            dataList.add(item);
        }
        invaildSchoolIdList.addAll(copySchoolIdList);
        retMap.put("dataList", dataList);
        retMap.put("invaildSchoolIdList", invaildSchoolIdList);
        return retMap;
    }


    /**
     * 为用户设置学校
     *
     * @param groupId      部门ID
     * @param userId       用户ID
     * @param schoolIdList 学校ID列表
     * @return int
     */
    public MapMessage setSchoolsForUser(Long groupId, Long userId, Set<Long> schoolIdList) {
        MapMessage retMessage = MapMessage.successMessage();
        List<String> messageInfoList = new ArrayList<>();
        for (Long schoolId : schoolIdList) {
            MapMessage message = addUserSchool(groupId, userId, schoolId);
            if (!message.isSuccess()) {
                messageInfoList.add(message.getInfo());
            }
        }
        retMessage.put("messageInfoList", messageInfoList);
        return retMessage;
    }

    /**
     * 为用户设置学校
     *
     * @param groupId  部门ID
     * @param userId   用户ID
     * @param schoolId 学校ID
     * @return MapMessage
     */
    private MapMessage addUserSchool(Long groupId, Long userId, Long schoolId) {

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("schoolId=" + schoolId + "的学校不存在！");
        }

        List<Long> groupSchools = baseOrgService.getManagedSchoolListByGroupId(groupId).stream().filter(p -> Objects.equals(p, schoolId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupSchools)) {
            return MapMessage.errorMessage("schoolId=" + schoolId + "的学校添加失败，不在该部门负责范围内");
        }
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        if (context != null && null != context.getCurrentUser() && baseOrgService.getUserRole(context.getCurrentUser().getUserId()) == AgentRoleType.CityManager && agentSchoolConfigLogService.checkCityManagerAddSchool(schoolId)) {
            AgentSchoolConfigLog log = agentSchoolConfigLogService.filterCityManagerLog(schoolId);
            return MapMessage.errorMessage("【{}({})】由{}在{}调整过负责人，60天内不可再次调整。", school.getCmainName(), schoolId, log.getHandlers(), DateUtils.dateToString(log.getOperatingTime(), DateUtils.FORMAT_SQL_DATE));
        }
        // 获取负责该学校的用户列表
        List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolBySchool(schoolId);
        List<Long> userIdList = userSchools.stream().map(AgentUserSchool::getUserId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIdList)) { //有人负责该学校了
            if (userIdList.contains(userId)) { // 该学校已经在该用户名下了
                return MapMessage.successMessage();
            }

            //获取和指定用户角色相同的同部门人员列表
            Set<Long> sameRoleUserList = new HashSet<>();
            List<Long> groupUserIdList = baseOrgService.getGroupUserIds(groupId); // 获取本部门的人员列表
            Map<Long, Integer> userRoleMap = baseOrgService.getGroupUserRoleMapByGroupId(groupId);
            Integer userRole = userRoleMap.get(userId);
            for (Long id : userIdList) {
                if (!groupUserIdList.contains(id)) { // 本部门不包括该人员，其他部门的人负责该学校
                    return MapMessage.errorMessage("schoolId=" + schoolId + "的学校添加失败，其它部门的人负责该学校");
                }
                Integer otherUserRole = userRoleMap.get(id);
                if (otherUserRole != null && Objects.equals(otherUserRole, userRole)) {
                    sameRoleUserList.add(id);
                }
            }
            // 删除原有的负责该学校的记录
            for (Long id : sameRoleUserList) {
                deleteUserSchool(id, schoolId);
            }
        }

        AgentUserSchool agentUserSchool = baseOrgService.getUserSchool(userId, schoolId);
        if (agentUserSchool == null) {
            AgentUserSchool userSchool = new AgentUserSchool();
            userSchool.setUserId(userId);
            userSchool.setSchoolId(schoolId);
            userSchool.setRegionCode(school.getRegionCode());
            userSchool.setSchoolLevel(school.getLevel());
            agentUserSchoolServiceClient.persist(userSchool);
            if (null != context && null != context.getCurrentUser()) {
                MapMessage msg = agentSchoolConfigLogService.initSchoolConfigLog(new Date(), context.getCurrentUser().getUserId(), AgentConfigSchoolLogType.ADD, schoolId, null, userId);
                if (msg.isSuccess()) {
                    agentSchoolConfigLogService.insertConfigSchoolLog((AgentSchoolConfigLog) msg.get("log"));
                }
            }
        }
        return MapMessage.successMessage();
    }


    /**
     * 删除用户负责的多个学校
     *
     * @param userId       用户ID
     * @param schoolIdList 学校ID列表
     */
    public void deleteUserSchoolList(Long userId, Set<Long> schoolIdList) {
        if (CollectionUtils.isNotEmpty(schoolIdList)) {
            List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolByUser(userId);
            List<AgentUserSchool> targetUserSchoolList = userSchoolList.stream().filter(p -> schoolIdList.contains(p.getSchoolId())).collect(Collectors.toList());
            deleteUserSchools(targetUserSchoolList);
        }
    }

    /**
     * 删除用户负责的单个学校
     *
     * @param userId   用户ID
     * @param schoolId 学校ID
     */
    public void deleteUserSchool(Long userId, Long schoolId) {
        AgentUserSchool agentUserSchool = baseOrgService.getUserSchool(userId, schoolId);
        if (agentUserSchool != null) {
            agentUserSchool.setDisabled(true);
            agentUserSchoolServiceClient.update(agentUserSchool.getId(), agentUserSchool);
        }
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        MapMessage msg = agentSchoolConfigLogService.initSchoolConfigLog(new Date(), context.getCurrentUser().getUserId(), AgentConfigSchoolLogType.DELETE, schoolId, userId, null);
        if (msg.isSuccess()) {
            agentSchoolConfigLogService.insertConfigSchoolLog((AgentSchoolConfigLog) msg.get("log"));
        }
    }

    public void deleteUserSchools(Collection<AgentUserSchool> userSchools) {
        if (CollectionUtils.isEmpty(userSchools)) {
            return;
        }
        userSchools.forEach(p -> {
            p.setDisabled(true);
            agentUserSchoolServiceClient.update(p.getId(), p);
        });
        Long userId = getCurrentUser().getUserId();
        AlpsThreadPool.getInstance().submit(() -> agentSchoolConfigLogService.addSchoolConfigLogs(userId, AgentConfigSchoolLogType.DELETE, userSchools));

    }


    /**
     * 删除部门
     *
     * @param groupId 部门ID
     */
    public void deleteGroupData(Long groupId) {
        List<AgentGroup> allSubGroupList = new ArrayList<>();
        //删除子部门
        baseOrgService.getAllSubGroupList(allSubGroupList, groupId);
        if (CollectionUtils.isNotEmpty(allSubGroupList)) {
            for (AgentGroup subGroup : allSubGroupList) {
                deleteSingleGroupData(subGroup.getId());
            }
        }
        deleteSingleGroupData(groupId);
    }

    private void deleteSingleGroupData(Long groupId) {
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group == null) {
            return;
        }
        //删除groupRegion
        List<AgentGroupRegion> groupRegionList = baseOrgService.getGroupRegionByGroup(groupId);
        if (CollectionUtils.isNotEmpty(groupRegionList)) {
            groupRegionList.stream().forEach(p -> {
                p.setDisabled(true);
                agentGroupRegionServiceClient.update(p.getId(), p);
            });
        }

        //删除groupUser 和 userSchool
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroup(groupId);
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            for (AgentGroupUser groupUser : groupUserList) {
                List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolByUser(groupUser.getUserId());
                if (CollectionUtils.isNotEmpty(userSchoolList)) {
                    userSchoolList.forEach(p -> {
                        p.setDisabled(true);
                        agentUserSchoolServiceClient.update(p.getId(), p);
                    });
                }
                groupUser.setDisabled(true);
                agentGroupUserServiceClient.update(groupUser.getId(), groupUser);
            }
        }

//        // 删除groupSchool
//        agentGroupSchoolServiceClient.deleteByGroupId(groupId);

        //删除Group
        group.setDisabled(true);
        baseOrgService.updateAgentGroup(group);

        // 删除部门及部门下人员本月和下月的预算
        List<Integer> monthList = new ArrayList<>();
        monthList.add(SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM")));
        monthList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.addMonth(new Date(), 1), "yyyyMM")));
        monthList.forEach(p -> agentKpiBudgetService.disableGroupBudget(groupId, p));
    }


    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return MapMessage
     */
    public MapMessage resetPassword(Long userId) {
        MapMessage mapMessage = new MapMessage();
        AgentUser agentUser = baseUserService.getById(userId);
        if (agentUser != null) {
            String newPassword = RandomUtils.randomString(6);
            Password passwd = encryptPassword(newPassword);
            agentUser.setPasswd(passwd.getPassword());
            agentUser.setPasswdSalt(passwd.getSalt());
            agentUser.setStatus(1);
            baseUserService.updateAgentUser(agentUser);
            mapMessage.setSuccess(true);
            mapMessage.setInfo("重设密码成功!");

            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(agentUser.getTel());
            smsMessage.setType(SmsType.MARKET_RESET_AGENT_USER_PASSWORD.name());
            smsMessage.setSmsContent("密码已重置成功，新密码：" + newPassword + "，请妥善保管。如需修改密码，可在「天玑」中自行设置。");
            smsServiceClient.getSmsService().sendSms(smsMessage);
        }
        return mapMessage;
    }

    public void closeUserAccount(Long userId) {
        // 删除用户负责的学校
        List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolByUser(userId);
        if (CollectionUtils.isNotEmpty(userSchoolList)) {
            userSchoolList.forEach(p -> p.setDisabled(true));
            userSchoolList.forEach(p -> agentUserSchoolServiceClient.update(p.getId(), p));
        }

        // 从部门中删除该用户
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(userId);
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            groupUserList.forEach(p -> p.setDisabled(true));
            groupUserList.forEach(p -> agentGroupUserServiceClient.update(p.getId(), p));

            // 删除用户本月和下月的预算
            List<Integer> monthList = new ArrayList<>();
            monthList.add(SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM")));
            monthList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.addMonth(new Date(), 1), "yyyyMM")));
            groupUserList.forEach(p -> monthList.forEach(t -> agentKpiBudgetService.disableUserBudget(p.getUserId(), p.getGroupId(), t)));
        }
        AgentUser agentUser = baseUserService.getById(userId);
        if (agentUser != null) {
            agentUser.setStatus(9);
            baseUserService.updateAgentUser(agentUser);

            honeycombUserService.unbindAgentUser(agentUser.getId());

            agentPartnerService.removePartnerRefByUserId(agentUser.getId());
        }
    }

    public MapMessage checkGroupData() {
        MapMessage retMap = MapMessage.successMessage();
        List<AgentUser> emptyUserList = checkEmptyUser();
        retMap.put("emptyUserList", emptyUserList);
        List<Map<String, Object>> invalidUserSchoolList = checkInvalidUserSchools();
        retMap.put("invalidUserSchoolList", invalidUserSchoolList);
        List<String> list = (List<String>) checkMarketGroup().get("checkGroupResultList");
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        retMap.put("checkGroupResultList", list);


        List<Long> noUserManagedSchools = getNoUserManagedSchools();
        if (CollectionUtils.isNotEmpty(noUserManagedSchools)) {
//            List<School> schoolList = noUserManagedSchools.stream().map(schoolLoaderClient::loadSchool).filter(p -> p != null).collect(Collectors.toList());
            List<String> list2 = (List<String>) retMap.get("checkGroupResultList");
            noUserManagedSchools.forEach(p -> list2.add("学校(ID:" + p + ")无人负责"));
        }
        return retMap;
    }

    /**
     * 获取不在任何部门里的用户
     *
     * @return
     */
    private List<AgentUser> checkEmptyUser() {

        Map<String, AgentUser> allUserMap = baseUserService.getAllAgentUsers();
        Set<Long> allGroupUserIds = baseOrgService.findAllGroupUserIds();
        if (CollectionUtils.isNotEmpty(allGroupUserIds)) {
            allGroupUserIds.forEach(p -> allUserMap.remove(String.valueOf(p)));
        }
        return new ArrayList<>(allUserMap.values());
    }

    // 获取用户名下负责的但字典表里面不存在的学校
    private List<Map<String, Object>> checkInvalidUserSchools() {
        List<AgentDictSchool> allDictSchool = baseDictService.loadAllSchoolDictData();
        Map<Long, AgentDictSchool> dictSchoolMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allDictSchool)) {
            Map<Long, AgentDictSchool> map = allDictSchool.stream().filter(p -> !p.isDisabledTrue()).collect(Collectors.toMap(AgentDictSchool::getSchoolId, (p) -> p, (k1, k2) -> {
                if (k1.fetchUpdateTimestamp() > k2.fetchUpdateTimestamp()) {
                    return k1;
                }
                return k2;
            }));
            if (MapUtils.isNotEmpty(map)) {
                dictSchoolMap.putAll(map);
            }
        }

        //获取所有userSchool
        List<AgentUserSchool> allUserSchoolList = agentUserSchoolLoaderClient.findAll();
        if (CollectionUtils.isEmpty(allUserSchoolList)) {
            return Collections.emptyList();
        }

        // 过滤出字典表中不存在的userSchool
        List<AgentUserSchool> targetUserSchoolList = allUserSchoolList.stream().filter(p -> !dictSchoolMap.containsKey(p.getSchoolId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(targetUserSchoolList)) {
            return Collections.emptyList();
        }
        Set<Long> schoolIdSet = targetUserSchoolList.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIdSet);
        Map<String, AgentUser> allUserMap = baseUserService.getAllAgentUsers();

        List<Map<String, Object>> invalidUserSchoolList = targetUserSchoolList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList());
        invalidUserSchoolList.forEach(p -> {
            Long userId = (Long) p.get("userId");
            AgentUser user = allUserMap.get(String.valueOf(userId));
            p.put("userName", user == null ? "" : user.getRealName());
            Long schoolId = (Long) p.get("schoolId");
            School school = schoolMap.get(schoolId);
            p.put("schoolName", school == null ? "" : school.getCname());
            p.put("comment", "用户负责的学校在字典表里不存在！");
        });
        return invalidUserSchoolList;
    }

    public List<AgentDictSchool> getAgentGroupSchoolBySchoolIds(Collection<Long> schoolId) {
        if (CollectionUtils.isEmpty(schoolId)) {
            return Collections.emptyList();
        }
        return agentDictSchoolService.loadSchoolDictDataBySchool(new HashSet<>(schoolId));
    }


    public MapMessage checkMarketGroup() {
        List<AgentGroup> rootGroupList = baseOrgService.getRootAgentGroups();
        if (CollectionUtils.isEmpty(rootGroupList)) {
            return MapMessage.successMessage();
        }
        List<AgentGroup> marketGroupList = rootGroupList.stream().filter(p -> Objects.equals(p.getRoleId(), AgentGroupRoleType.Country.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(marketGroupList)) {
            return MapMessage.successMessage();
        }
        MapMessage message = MapMessage.successMessage();
        List<String> resultList = new ArrayList<>();
        message.put("checkGroupResultList", resultList);
        for (AgentGroup group : marketGroupList) {
            MapMessage groupMessage = checkAgentGroup(group.getId());
            if (!groupMessage.isSuccess()) {
                List<String> groupResultList = (List<String>) groupMessage.get("checkGroupResultList");
                if (CollectionUtils.isNotEmpty(groupResultList)) {
                    resultList.addAll(groupResultList);
                }
            }
        }
        return message;
    }


    private MapMessage checkAgentGroup(Long groupId) {

        MapMessage message = MapMessage.successMessage();
        List<String> checkGroupResultList = new ArrayList<>();
        message.put("checkGroupResultList", checkGroupResultList);


        List<AgentGroup> agentGroupList = new ArrayList<>();
        baseOrgService.getAllSubGroupList(agentGroupList, groupId);

        List<AgentGroupRegion> agentGroupRegionList = agentGroupRegionLoaderClient.findAll();
        List<AgentGroupUser> agentGroupUserList = agentGroupUserLoaderClient.findAll();
        List<AgentUserSchool> agentUserSchoolList = agentUserSchoolLoaderClient.findAll();

        Map<Long, AgentGroup> groupMap = agentGroupList.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        Map<Long, List<AgentGroupRegion>> groupRegionMap = agentGroupRegionList.stream().collect(Collectors.groupingBy(AgentGroupRegion::getGroupId));

        Map<Long, List<AgentGroupUser>> groupUserMap = agentGroupUserList.stream().collect(Collectors.groupingBy(AgentGroupUser::getGroupId));
        Map<Long, List<AgentGroupUser>> userGroupMap = agentGroupUserList.stream().collect(Collectors.groupingBy(AgentGroupUser::getUserId));
        Map<Long, List<AgentUserSchool>> userSchoolMap = agentUserSchoolList.stream().collect(Collectors.groupingBy(AgentUserSchool::getUserId));

        Map<Integer, ExRegion> allRegionMap = raikouSystem.getRegionBuffer().loadAllRegions();
        Map<String, AgentUser> allUserMap = baseUserService.getAllAgentUsers();

        //// 检查部门区域
        // 判断没有负责任何区域的部门
        List<String> noRegionGoupMessages = agentGroupList.stream().filter(p -> !groupRegionMap.containsKey(p.getId())).map(p -> StringUtils.join("部门 【", p.getGroupName(), "(ID:", p.getId(), ")】没有负责任何区域")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noRegionGoupMessages)) {
            checkGroupResultList.addAll(noRegionGoupMessages);
        }

        // 部门负责的区域不在上级部门负责范围内或者相应区域的学校权限不一致
        List<AgentGroup> havRegionGroupList = agentGroupList.stream().filter(p -> groupRegionMap.containsKey(p.getId()) && p.getParentId() != 0).collect(Collectors.toList()); // 过滤掉根节点 和 没有区域的部门
        if (CollectionUtils.isNotEmpty(havRegionGroupList)) {
            for (AgentGroup group : havRegionGroupList) {
                groupRegionMap.get(group.getId()).forEach(p -> {
                    ExRegion exRegion = allRegionMap.get(p.getRegionCode());
                    if (exRegion != null) {
                        AgentGroupRegion parentGroupRegion = null;
                        List<AgentGroupRegion> parentGroupRegionList = groupRegionMap.get(group.getParentId());
                        if (CollectionUtils.isNotEmpty(parentGroupRegionList)) {
                            parentGroupRegion = parentGroupRegionList.stream().filter(k -> k.getRegionCode() == exRegion.getCityCode()).findFirst().orElse(null);
                        }
                        if (parentGroupRegion == null) { // 有上级部门，但上级部门不负责该区域
                            checkGroupResultList.add("部门 【" + group.getGroupName() + "(ID:" + p.getGroupId() + ")】负责的区域【" + exRegion.getName() + "】不在上级部门负责的区域范围内");
                        }
                    } else {
                        checkGroupResultList.add("部门 【" + group.getGroupName() + "(ID:" + p.getGroupId() + ")】负责的区域【" + p.getRegionCode() + "】不存在");
                    }
                });
            }
        }

        //// 检查部门人员
        // 部门没有任何人员
        List<String> noUserGoupMessages = agentGroupList.stream().filter(p -> !groupUserMap.containsKey(p.getId())).map(p -> StringUtils.join("部门 【", p.getGroupName(), "(ID:", p.getId(), ")】中没有任何人员")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(noUserGoupMessages)) {
            checkGroupResultList.addAll(noUserGoupMessages);
        }

        //
        List<AgentGroup> havUserGroupList = agentGroupList.stream().filter(p -> groupUserMap.containsKey(p.getId())).collect(Collectors.toList()); // 过滤掉没有人员的部门
        if (CollectionUtils.isNotEmpty(havUserGroupList)) {
            havUserGroupList.forEach(group -> {
                List<AgentGroupUser> groupUserList = groupUserMap.get(group.getId());
                long count = 0;
                if (group.fetchGroupRoleType() == AgentGroupRoleType.Country) {
                    count = groupUserList.stream().filter(p -> Objects.equals(p.getUserRoleId(), AgentRoleType.Country.getId())).count();
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit) {
                    count = groupUserList.stream().filter(p -> Objects.equals(p.getUserRoleId(), AgentRoleType.BUManager.getId())).count();
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.Region) {
                    count = groupUserList.stream().filter(p -> Objects.equals(p.getUserRoleId(), AgentRoleType.Region.getId())).count();
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.Area) {
                    count = groupUserList.stream().filter(p -> Objects.equals(p.getUserRoleId(), AgentRoleType.AreaManager.getId())).count();
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.City) {
                    count = groupUserList.stream().filter(p -> Objects.equals(p.getUserRoleId(), AgentRoleType.CityManager.getId())).count();
                }

                if (count == 0) {
                    checkGroupResultList.add("部门 【" + group.getGroupName() + "(ID:" + group.getId() + ")】没有个管理人员");
                } else if (count > 1) {
                    checkGroupResultList.add("部门 【" + group.getGroupName() + "(ID:" + group.getId() + ")】有多个管理人员");
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.City) {
                    if (groupUserList.size() == count) {
                        checkGroupResultList.add("部门 【" + group.getGroupName() + "(ID:" + group.getId() + ")】没有专员或代理");
                    }
                }
            });
        }

        //// 判断学校
        List<AgentGroup> cityGroupList = agentGroupList.stream().filter(p -> Objects.equals(p.getRoleId(), AgentGroupRoleType.City.getId())).collect(Collectors.toList());
        Map<Long, Integer> cityGroupRoleMap = cityGroupList.stream().collect(Collectors.toMap(AgentGroup::getId, AgentGroup::getRoleId));
        if (CollectionUtils.isNotEmpty(cityGroupList)) {
            List<AgentGroupUser> noneCityManagerUserList = agentGroupUserList.stream().filter(p -> cityGroupRoleMap.containsKey(p.getGroupId()) && p.getUserRoleType() != AgentRoleType.CityManager).collect(Collectors.toList()); // 获取专员和代理列表

            // 过滤出没有配置学校的专员和代理
            List<AgentGroupUser> havNoSchoolUsers = noneCityManagerUserList.stream().filter(p -> !userSchoolMap.containsKey(p.getUserId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(havNoSchoolUsers)) {
                havNoSchoolUsers.stream().forEach(p -> {
                    AgentUser user = allUserMap.get(String.valueOf(p.getUserId()));
                    AgentGroup group = groupMap.get(p.getGroupId());
                    checkGroupResultList.add("部门 【" + group.getGroupName() + "(ID:" + group.getId() + ")】下人员【" + user.getRealName() + "(ID:" + user.getId() + ")】没有配置任何学校");
                });
            }

            agentUserSchoolList.forEach(p -> {
                AgentUser user = allUserMap.get(String.valueOf(p.getUserId()));
                List<AgentUserSchool> userSchoolList1 = agentUserSchoolList.stream().filter(k -> Objects.equals(p.getSchoolId(), k.getSchoolId())).collect(Collectors.toList());
                Set<Long> otherUsers = new HashSet<>();
                if (CollectionUtils.isNotEmpty(userSchoolList1)) {
                    otherUsers = userSchoolList1.stream().map(AgentUserSchool::getUserId).filter(k -> !Objects.equals(k, p.getUserId())).collect(Collectors.toSet());
                }

                if (CollectionUtils.isNotEmpty(otherUsers)) {
                    for (Long otherUserId : otherUsers) {
                        Long sameGroup = getSameGroup(p.getUserId(), otherUserId);
                        // 是同一部门
                        if (sameGroup != null) {
                            // 判断是否是同一角色
                            if (isSameRole(p.getUserId(), otherUserId)) {
                                checkGroupResultList.add("人员【" + user.getRealName() + "(ID:" + user.getId() + ")】负责的学校（id=" + p.getSchoolId() + "）同时有相同角色的人负责");
                                break;
                            }
                        } else {
                            checkGroupResultList.add("人员【" + user.getRealName() + "(ID:" + user.getId() + ")】负责的学校（id=" + p.getSchoolId() + "）同时有其他部门的人负责");
                            break;
                        }
                    }
                } else {
                    List<AgentGroupUser> userGroupList = userGroupMap.get(p.getUserId());
                    if (CollectionUtils.isNotEmpty(userGroupList)) {
                        List<AgentGroupRegion> groupRegionList = groupRegionMap.get(userGroupList.get(0).getGroupId());
                        List<Integer> groupRegionCodeList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(groupRegionList)) {
                            groupRegionList.forEach(r -> groupRegionCodeList.add(r.getRegionCode()));
                        }
                        if (CollectionUtils.isEmpty(groupRegionCodeList) || !groupRegionCodeList.contains(p.getRegionCode())) { // 学校不在部门负责的区域范围内
                            checkGroupResultList.add("人员【" + user.getRealName() + "(ID:" + user.getId() + ")】负责的id=" + p.getSchoolId() + "的学校不在该部门的权限范围内");
                        }
                    }
                }

            });
        }

        if (CollectionUtils.isNotEmpty(checkGroupResultList)) {
            message.setSuccess(false);
            return message;
        }
        return message;
    }


    /**
     * 获取没有人负责的学校
     *
     * @return
     */
    public List<Long> getNoUserManagedSchools() {
        List<AgentDictSchool> allDictSchool = baseDictService.loadAllSchoolDictData();
        Map<Long, AgentDictSchool> dictSchoolMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allDictSchool)) {
            Map<Long, AgentDictSchool> map = allDictSchool.stream().filter(p -> !p.isDisabledTrue()).collect(Collectors.toMap(AgentDictSchool::getSchoolId, (p) -> p, (k1, k2) -> {
                if (k1.fetchUpdateTimestamp() > k2.fetchUpdateTimestamp()) {
                    return k1;
                }
                return k2;
            }));
            if (MapUtils.isNotEmpty(map)) {
                dictSchoolMap.putAll(map);
            }
        }

        //去掉有人负责的学校
        List<AgentUserSchool> allUserSchoolList = agentUserSchoolLoaderClient.findAll();
        if (CollectionUtils.isNotEmpty(allUserSchoolList)) {
            for (AgentUserSchool userSchool : allUserSchoolList) {
                dictSchoolMap.remove(userSchool.getSchoolId());
            }
        }

        if (MapUtils.isEmpty(dictSchoolMap)) {
            return Collections.emptyList();
        }

        List<AgentGroupRegion> groupRegionList = agentGroupRegionLoaderClient.findAll();
        Map<Integer, List<AgentGroupRegion>> regionGroupLevelMap = groupRegionList.stream().collect(Collectors.groupingBy(AgentGroupRegion::getRegionCode));
        List<Long> retList = dictSchoolMap.values().stream().filter(p -> {
            List<AgentGroupRegion> groupRegionList1 = regionGroupLevelMap.get(p.getCountyCode());
            if (CollectionUtils.isEmpty(groupRegionList1)) {
                return true;
            }
            return false;
        }).map(AgentDictSchool::getSchoolId).collect(Collectors.toList());
        return retList;
    }

    private PartnerData createPartnerDataByUser(AgentUser user) {
        if (user == null) {
            return null;
        }
        PartnerData data = new PartnerData();
        data.setPartnerId(user.getId());
        data.setPartnerName(user.getRealName());
        return data;
    }

    private Long getSameGroup(Long userId, Long targetUserId) {
        List<Long> userGroupIdList = baseOrgService.getGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(userGroupIdList)) {
            return null;
        }

        List<Long> targetUserGroupIdList = baseOrgService.getGroupIdListByUserId(targetUserId);
        if (CollectionUtils.isEmpty(targetUserGroupIdList)) {
            return null;
        }
        for (Long groupId : targetUserGroupIdList) {
            if (userGroupIdList.contains(groupId)) {
                return groupId;
            }
        }
        return null;
    }

    private boolean isSameRole(Long userId, Long targetUserId) {
        List<AgentRoleType> userRoleList = baseOrgService.getUserRoleList(userId);
        if (CollectionUtils.isEmpty(userRoleList)) {
            return false;
        }

        List<AgentRoleType> targetUserRoleList = baseOrgService.getUserRoleList(targetUserId);
        if (CollectionUtils.isEmpty(targetUserRoleList)) {
            return false;
        }
        for (AgentRoleType targetUserRole : targetUserRoleList) {
            if (userRoleList.contains(targetUserRole)) {
                return true;
            }
        }
        return false;
    }

    public List<AgentUserMaterialBudgetRecord> loadUserMaterialBudgetRecordList(Long userId) {
        return agentUserMaterialBudgetRecordDao.findByUserId(userId);
    }

    public List<AgentUserCashRecord> loadUserCashRecordList(Long userId) {
        return agentUserCashRecordDao.findByUserId(userId);
    }

    // 获取一个学校所对应的部门信息
    public Map<Long, SchoolDepartmentInfo> loadDepartmentInfoBySchool(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }

        // 获取学校地区
        List<AgentDictSchool> allDictSchoolList = baseDictService.loadAllSchoolDictData();
        List<AgentDictSchool> dictSchoolList = allDictSchoolList.stream().filter(p -> schoolIds.contains(p.getSchoolId())).collect(Collectors.toList());
        Map<Long, AgentDictSchool> dictSchoolMap = dictSchoolList.stream().collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity(), (o1, o2) -> o1));
        Set<Integer> regionCode = dictSchoolList.stream().map(AgentDictSchool::getCountyCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> schoolRegion = raikouSystem.getRegionBuffer().loadRegions(regionCode);

        // 获取负责该地区的部门信息
        Map<Integer, List<AgentGroupRegion>> groupRegionMap = agentGroupRegionLoaderClient.findByRegionCodes(regionCode);

        // 获取部门数据
        Map<Long, AgentGroup> allGroupMap = agentGroupLoaderClient.findAllGroups().stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));

        // 获取市经理信息
        List<AgentGroupUser> allGroupUsers = agentGroupUserLoaderClient.findAll();
        List<AgentGroupUser> cityManageUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.CityManager)).collect(Collectors.toList());
        Map<Long, Long> cityManageUsersMap = cityManageUsers.stream().collect(Collectors.toMap(AgentGroupUser::getGroupId, AgentGroupUser::getUserId, (o1, o2) -> o1));

        // 获取负责学校的专员
        List<AgentUserSchool> allUserSchools = agentUserSchoolLoaderClient.findAll();
        // 过滤出专员， 排除代理
        Set<Long> bdUserIds = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.BusinessDeveloper)).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        Map<Long, Long> schoolUserMap = allUserSchools.stream().filter(p -> schoolIds.contains(p.getSchoolId()) && bdUserIds.contains(p.getUserId())).collect(Collectors.toMap(AgentUserSchool::getSchoolId, AgentUserSchool::getUserId, (o1, o2) -> o1));

        Map<String, AgentUser> allUsersMap = baseUserService.getAllAgentUsers();

        Map<Long, SchoolDepartmentInfo> result = new HashMap<>();
        for (Long schoolId : schoolIds) {
            SchoolDepartmentInfo info = new SchoolDepartmentInfo();
            AgentDictSchool dictSchool = dictSchoolMap.get(schoolId);
            if (dictSchool == null || dictSchool.getCountyCode() == null || groupRegionMap.get(dictSchool.getCountyCode()) == null) {
                continue;
            }

            Integer region = dictSchool.getCountyCode();

            List<Long> groupIds = agentGroupSupport.getMarketGroupIdsBySchool(schoolId);
            if (CollectionUtils.isEmpty(groupIds) || allGroupMap.get(groupIds.get(0)) == null) {
                continue;
            }

            // 设置学校所在的地区信息
            if (schoolRegion.get(region) != null) {
                info.setRegionName(StringUtils.formatMessage("{}-{}", schoolRegion.get(region).getCityName(), schoolRegion.get(region).getCountyName()));
            }


            // 设置部门信息
            Long groupId = groupIds.get(0);
            AgentGroup group = allGroupMap.get(groupId);
            info.setGroupId(group.getId());
            info.setGroupName(group.getGroupName());

            // 设置学校所在的大区信息
            AgentGroup regionGroup = allGroupMap.get(group.getParentId());
            if (regionGroup != null) {
                info.setRegionGroupName(regionGroup.getGroupName());
            }

            // 设置市经理信息
            if (cityManageUsersMap.get(groupId) != null) {
                Long cityManageId = cityManageUsersMap.get(groupId);
                if (allUsersMap.get(String.valueOf(cityManageId)) != null) {
                    AgentUser user = allUsersMap.get(String.valueOf(cityManageId));
                    info.setCityManagerId(user.getId());            // 插入市经理的id
                    info.setCityManagerName(user.getRealName());    // 插入市经理的名称
                }
            }

            // 设置专员信息
            if (schoolUserMap.get(schoolId) != null) {
                Long userId = schoolUserMap.get(schoolId);
                if (allUsersMap.get(String.valueOf(userId)) != null) {
                    AgentUser user = allUsersMap.get(String.valueOf(userId));
                    info.setBusinessDeveloperId(user.getId());
                    info.setBusinessDeveloperName(user.getRealName());
                }
            }
            result.put(schoolId, info);
        }

        return result;
    }


//    public MapMessage setSchoolForGroup(Long groupId, Long schoolId){
//        AgentGroupRoleType roleType = baseOrgService.getGroupRole(groupId);
//        if(roleType != AgentGroupRoleType.City){
//            return MapMessage.errorMessage("部门级别错误");
//        }
//        List<AgentDictSchool> dictSchools = agentDictSchoolService.findSchoolDictDataBySchool(schoolId);
//        if(CollectionUtils.isEmpty(dictSchools)){
//            return MapMessage.errorMessage("学校不是字典表学校");
//        }
//        AgentDictSchool dictSchool = dictSchools.get(0);
//        List<Integer> groupRegionCodeList = baseOrgService.getGroupRegionCodeList(groupId);
//        List<Integer> countyRegionCodes = agentRegionService.getCountyCodes(groupRegionCodeList);
//        if(CollectionUtils.isEmpty(countyRegionCodes) || !countyRegionCodes.contains(dictSchool.getCountyCode())){
//            return MapMessage.errorMessage("学校不在该部门的负责范围内");
//        }
//
//
//        AgentGroupSchool groupSchool = agentGroupSchoolLoaderClient.findBySchoolId(schoolId);
//        if(groupSchool == null){
//            groupSchool = new AgentGroupSchool();
//            groupSchool.setGroupId(groupId);
//            groupSchool.setSchoolId(schoolId);
//            groupSchool.setRegionCode(dictSchool.getCountyCode());
//            agentGroupSchoolServiceClient.insert(groupSchool);
//
//        }else if(!Objects.equals(groupSchool.getGroupId(), groupId)){// 该学校在其他部门下面
//            // 清除userSchool
//            List<AgentUserSchool> userSchoolList = baseOrgService.getUserSchoolBySchool(schoolId);
//            if (CollectionUtils.isNotEmpty(userSchoolList)) {
//                userSchoolList.forEach(p -> {
//                    p.setDisabled(true);
//                    agentUserSchoolServiceClient.update(p.getId(), p);
//                });
//            }
//            // 清除groupSchool
//            agentGroupSchoolServiceClient.deleteBySchoolId(schoolId);
//
//            groupSchool = new AgentGroupSchool();
//            groupSchool.setGroupId(groupId);
//            groupSchool.setSchoolId(schoolId);
//            groupSchool.setRegionCode(dictSchool.getCountyCode());
//            agentGroupSchoolServiceClient.insert(groupSchool);
//        }
//
//        return MapMessage.successMessage();
//    }

    /**
     * 导出组织结构
     *
     * @param organizationList
     */
    public void exportOrganization(SXSSFWorkbook workbook, List<Map<String, Object>> organizationList) {
        try {
            Sheet sheet = workbook.createSheet("组织结构");
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
            HssfUtils.setCellValue(firstRow, 0, firstRowStyle, "市场");
            HssfUtils.setCellValue(firstRow, 1, firstRowStyle, "大区");
            HssfUtils.setCellValue(firstRow, 2, firstRowStyle, "区域");
            HssfUtils.setCellValue(firstRow, 3, firstRowStyle, "分区");
            HssfUtils.setCellValue(firstRow, 4, firstRowStyle, "负责区域");
            HssfUtils.setCellValue(firstRow, 5, firstRowStyle, "角色");
            HssfUtils.setCellValue(firstRow, 6, firstRowStyle, "姓名");
            HssfUtils.setCellValue(firstRow, 7, firstRowStyle, "userId");
            HssfUtils.setCellValue(firstRow, 8, firstRowStyle, "工号");
            HssfUtils.setCellValue(firstRow, 9, firstRowStyle, "账号");
            HssfUtils.setCellValue(firstRow, 10, firstRowStyle, "负责学校数量");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

            if (CollectionUtils.isNotEmpty(organizationList)) {
                Integer index = 1;
                for (Map<String, Object> organization : organizationList) {
                    Row row = sheet.createRow(index++);
                    HssfUtils.setCellValue(row, 0, cellStyle, ConversionUtils.toString(organization.get("marketingName")));
                    HssfUtils.setCellValue(row, 1, cellStyle, ConversionUtils.toString(organization.get("regionName")));
                    HssfUtils.setCellValue(row, 2, cellStyle, ConversionUtils.toString(organization.get("areaName")));
                    HssfUtils.setCellValue(row, 3, cellStyle, ConversionUtils.toString(organization.get("cityName")));
                    HssfUtils.setCellValue(row, 4, cellStyle, ConversionUtils.toString(organization.get("agentGroupRegionInfo")));
                    HssfUtils.setCellValue(row, 5, cellStyle, ConversionUtils.toString(organization.get("userRole")));
                    HssfUtils.setCellValue(row, 6, cellStyle, ConversionUtils.toString(organization.get("realName")));
                    HssfUtils.setCellValue(row, 7, cellStyle, ConversionUtils.toString(organization.get("userId")));
                    HssfUtils.setCellValue(row, 8, cellStyle, ConversionUtils.toString(organization.get("accountNumber")));
                    HssfUtils.setCellValue(row, 9, cellStyle, ConversionUtils.toString(organization.get("accountName")));
                    HssfUtils.setCellValue(row, 10, cellStyle, ConversionUtils.toString(organization.get("schoolNum")));
                }
            }
        } catch (Exception ex) {
            logger.error("error info: ", ex);
            emailServiceClient.createPlainEmail()
                    .body("error info: " + ex)
                    .subject("导出组织结构异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
    }


    /**
     * 删除部门下所有用户（或者指定用户）对应的权限内的SchoolLevel缓存
     *
     * @param groupId
     */
    public void deleteUserSchoolLevelCacheKey(Long groupId, Long userId) {
        List<String> cacheKeyList = new ArrayList<>();
        if (null == userId) {
            List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(groupId);
            Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            userIds.forEach(item -> {
                cacheKeyList.add("AGENT_USER_SERVICE_SCHOOL_LEVELS:UID=" + item);
            });
        } else {
            cacheKeyList.add("AGENT_USER_SERVICE_SCHOOL_LEVELS:UID=" + userId);
        }
        agentCacheSystem.CBS.flushable.delete(cacheKeyList);
    }

    public void adjustSubGroupServiceTypes(Long groupId) {

        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group == null) {
            return;
        }
        List<AgentServiceType> serviceTypeList = group.fetchServiceTypeList();
        List<AgentGroup> subGroups = baseOrgService.getGroupListByParentId(groupId);
        if (CollectionUtils.isNotEmpty(subGroups)) {
            for (AgentGroup subGroup : subGroups) {
                List<AgentServiceType> subGroupServiceTypeList = subGroup.fetchServiceTypeList();
                if (subGroupServiceTypeList.stream().anyMatch(p -> !serviceTypeList.contains(p))) {  // 有删除权限的情况
                    List<AgentServiceType> targetTypeList = subGroupServiceTypeList.stream().filter(serviceTypeList::contains).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(targetTypeList)) {
                        List<String> serviceTypeNameList = targetTypeList.stream().map(AgentServiceType::name).collect(Collectors.toList());
                        subGroup.setServiceType(StringUtils.join(serviceTypeNameList, ","));
                    } else {
                        subGroup.setServiceType("");
                    }
                    baseOrgService.updateAgentGroup(subGroup);

                    adjustSubGroupServiceTypes(subGroup.getId());
                }
            }
        }
    }
}
