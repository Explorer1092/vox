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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ApplyManagementService
 *
 * @author song.wang
 * @date 2017/1/3
 */
@Named
public class AgentApplyManagementService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private ApplyManagementLoaderClient applyManagementLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;

    @Inject
    private SchoolResourceService schoolResourceService;
    @Inject
    private AgentRegionService agentRegionService;

    public List<AbstractBaseApply> fetchUserApplyList(Long userId, ApplyStatus status, Boolean includeRevokeData) {
        Page page = applyManagementLoaderClient.fetchUserApplyList(SystemPlatformType.AGENT, String.valueOf(userId), status, includeRevokeData, 1, 10000);
        return page.getContent();
    }

    public ApplyWithProcessResultData getApplyDetailWithProcessResultByApplyId(ApplyType applyType, Long applyId) {
        return applyManagementLoaderClient.fetchApplyWithProcessResultByApplyId(applyType, applyId, true);
    }

    public ApplyWithProcessResultData getApplyDetailByWorkflowId(ApplyType applyType, Long workflowId, Boolean withCurrentProcess) {
        return applyManagementLoaderClient.fetchApplyWithProcessResultByWorkflowId(applyType, workflowId, withCurrentProcess);
    }

    // 判断是否可以撤销申请
    public boolean judgeCanRevokeApply(ApplyType applyType, Long applyId) {
        return applyManagementLoaderClient.judgeCanRevokeApply(applyType, applyId);
    }

    public List<AbstractBaseApply> fetchApplyListByTypeAndDate(ApplyType applyType, Date startDate, Date endDate) {
        Page page = applyManagementLoaderClient.fetchApplyListByType(applyType, startDate, endDate, 1, 10000);
        return page.getContent();
    }

    public List<ApplyWithProcessResultData> getApplyListByTypeAndStatus(Long userId, ApplyType applyType, ApplyStatus status) {
        Page page = applyManagementLoaderClient.fetchUserApplyWithProcessResult(SystemPlatformType.AGENT, String.valueOf(userId), applyType, status, null, null, true, 1, 10000);
        return page.getContent();
    }

    /**
     * 给指定区域内查询学校时，根据schoolId查找学校列表
     *
     * @param schoolIdList 学校ID列表
     * @return map
     */
    public Map<String, Object> searchSchoolsIncludeAll(Set<Long> schoolIdList, AuthCurrentUser authCurrentUser, Integer cityCode) {
        if (CollectionUtils.isEmpty(schoolIdList)) {
            return Collections.emptyMap();
        }
        List<School> schoolList = searchSchoolBySchoolId(schoolIdList, authCurrentUser);
        //过滤出有效的学校 详见School    认证状态（0等待认证、1已认证、3未通过(假)）
        Set<Long> schoolIdSet = schoolList.stream().filter(p -> p.getAuthenticationState() != null && (p.getAuthenticationState() == 0 || p.getAuthenticationState() == 1)).map(School::getId).collect(Collectors.toSet());
        Map<Long, CrmSchoolSummary> schools = crmSummaryLoaderClient.loadSchoolSummary(schoolIdSet);
        //过滤城市
        List<CrmSchoolSummary> dataList = schools.values().stream().filter(p -> Objects.equals(p.getCityCode(), cityCode)).collect(Collectors.toList());
        Set exitSchoolId = dataList.stream().map(CrmSchoolSummary::getSchoolId).collect(Collectors.toSet());

        Map<String, Object> retMap = new HashMap<>();
        retMap.put("dataList", dataList);
        schoolIdList.removeAll(exitSchoolId);
        retMap.put("invaildSchoolIdList", schoolIdList);
        return retMap;
    }

    private List<School> searchSchoolBySchoolId(Set<Long> schoolIdList, AuthCurrentUser user) {
        // 获取用户所在部门的地区
        List<Long> groupIds = baseOrgService.getGroupIdListByUserId(user.getUserId());
        Set<Integer> userGroupRegions = baseOrgService.getGroupRegionsByGroupSet(groupIds).stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());

        // 获取到用户所在城市
        Set<Integer> userCities = getCurrentUserCities(user.getUserId());
        AgentRoleType roleType = null;
        if (user.isBusinessDeveloper()) {
            roleType = AgentRoleType.BusinessDeveloper;
        } else if (user.isCityManager()) {
            roleType = AgentRoleType.CityManager;
        } else if (user.isAreaManager()) {
            roleType = AgentRoleType.AreaManager;
        } else if (user.isRegionManager()) {
            roleType = AgentRoleType.Region;
        } else if (user.isBuManager()) {
            roleType = AgentRoleType.BUManager;
        } else if (user.isCountryManager()) {
            roleType = AgentRoleType.Country;
        } else if (user.isProductOperator()) { // 如果是产品运营角色，以全国总监的身份查看数据
            roleType = AgentRoleType.Country;
        }
        return schoolResourceService.searchSchoolBySchoolId(schoolIdList, userGroupRegions, userCities, roleType);
    }

    private Set<Integer> getCurrentUserCities(Long userId) {
        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
        Set<Integer> regionSet = baseOrgService.getGroupRegionsByGroupSet(groupIdList).stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionSet);
        return exRegionMap.values().stream().map(ExRegion::getCityCode).collect(Collectors.toSet());
    }


    /**
     * 限 市经理 和 专员 使用 若有需要请拆分
     * 获取市经理权限的管线范围内的城市信息
     * 专员获取其上属市经理的管辖范围
     */
    public List<Map<String, Object>> getCityManagerAdministerCityInfo(AuthCurrentUser authCurrentUser) {
        //将查询条件上升到市经理
        Long userId = authCurrentUser.getUserId();
        List<AgentGroupUser> agentGroupUserList = baseOrgService.getGroupUserByUser(userId);
        if (CollectionUtils.isEmpty(agentGroupUserList)) {
            return new ArrayList<>();
        }
        //过滤出所需要的城市信息
        List<Map<String, Object>> cityInfoList = new ArrayList<>();

        List<AgentGroupRegion> agrList = baseOrgService.getGroupRegionsByGroupSet(agentGroupUserList.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet()));
        Set<Integer> regionCodeList = new HashSet<>();
        agrList.forEach(p -> regionCodeList.add(p.getRegionCode()));

        Map<Integer, ExRegion> erMap = raikouSystem.getRegionBuffer().loadRegions(regionCodeList);

        Map<Integer, ExRegion> cityCodeMap = new HashMap<>();
        erMap.values().stream().forEach(p -> {
            if (p.fetchRegionType() == RegionType.PROVINCE) {
                p.getChildren().forEach(r -> {
                    if (r.fetchRegionType() == RegionType.CITY && !cityCodeMap.containsKey(r.getId())) {
                        cityCodeMap.put(r.getId(), r);
                    }
                });
            } else if (p.fetchRegionType() == RegionType.CITY) {
                if (!cityCodeMap.containsKey(p.getId())) {
                    cityCodeMap.put(p.getId(), p);
                }
            } else if (p.fetchRegionType() == RegionType.COUNTY) {
                if (!cityCodeMap.containsKey(p.getCityCode())) {
                    ExRegion exRegion = raikouSystem.loadRegion(p.getCityCode());
                    if (exRegion != null) {
                        cityCodeMap.put(exRegion.getId(), exRegion);
                    }
                }
            }
        });
        for (Integer regionCode : cityCodeMap.keySet()) {
            ExRegion er = cityCodeMap.get(regionCode);
            Map<String, Object> cityInfo = new HashMap<>();
            cityInfo.put("cityCode", er.getCityCode());
            cityInfo.put("cityName", er.getCityName());
            cityInfo.put("provinceCode", er.getProvinceCode());
            cityInfo.put("provinceName", er.getProvinceName());
            List<Integer> countyCodeList = agentRegionService.getCountyCodes(er.getId());
            List<Long> groupIdList = agrList.stream().filter(p -> Objects.equals(p.getRegionCode(), regionCode) || countyCodeList.contains(p.getRegionCode()) || Objects.equals(p.getRegionCode(), er.getProvinceCode())).map(AgentGroupRegion::getGroupId).collect(Collectors.toList());
            //标记当前城市是在哪个组下
            cityInfo.put("groupId", CollectionUtils.isNotEmpty(groupIdList) ? groupIdList.get(0) : 0L);//兼容下错误信息
            cityInfoList.add(cityInfo);
        }
        return cityInfoList;
    }
}
