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

package com.voxlearning.utopia.agent.service.mobile.v2;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.SchoolShortInfo;
import com.voxlearning.utopia.agent.dao.CrmVisitPlanDao;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.entity.crm.CrmVisitPlan;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学校拜访计划相关Service
 * Created by yaguang.wang on 2016/7/5.
 */
@Named
public class CrmVisitPlanService extends AbstractAgentService {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private CrmVisitPlanDao crmVisitPlanDao;
    @Inject private BaseOrgService baseOrgService;
    @Inject private SearchService searchService;

    public CrmVisitPlan getCrmVisitPlan(String planId) {
        if (StringUtils.isBlank(planId)) {
            return null;
        }
        return crmVisitPlanDao.load(planId);
    }

    public List<CrmVisitPlan> getUserVisitPlan(Long userId, Date startDate, Date endDate) {
        if (userId == null || userId == 0L) {
            return Collections.emptyList();
        }

        return crmVisitPlanDao.loadUserVisitPlan(userId).stream()
                .filter(p -> startDate == null || startDate.before(p.getVisitTime()) || Objects.equals(p.getVisitTime().getTime(), startDate.getTime()))
                .filter(p -> endDate == null || endDate.after(p.getVisitTime()))
                .collect(Collectors.toList());
    }

    public Map<Long, List<CrmVisitPlan>> getUserVisitPlan(Collection<Long> userIds, Date startDate, Date endDate) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        Map<Long, List<CrmVisitPlan>> retVisitPlans = new HashMap<>();

        Map<Long, List<CrmVisitPlan>> userVisitPlans = crmVisitPlanDao.loadUserVisitPlan(userIds);
        for (Long userId : userVisitPlans.keySet()) {
            List<CrmVisitPlan> plans = userVisitPlans.get(userId);
            plans = plans.stream()
                    .filter(p -> startDate == null || Objects.equals(p.getVisitTime().getTime(), startDate.getTime()) || startDate.before(p.getVisitTime()))
                    .filter(p -> endDate == null || endDate.after(p.getVisitTime()))
                    .collect(Collectors.toList());

            retVisitPlans.put(userId, plans);

        }
        return retVisitPlans;
    }

    public MapMessage updateVisitPlan(Long userId, String planId, Date visitTime, String planContent) {
        if (userId == null) {
            return MapMessage.errorMessage("用户信息获取失败,请重新登录");
        }

        if (visitTime == null) {
            return MapMessage.errorMessage("时间选择错误");
        }

        if (StringUtils.isBlank(planId)) {
            return MapMessage.errorMessage("计划ID为空");
        }

        CrmVisitPlan crmVisitPlan = getCrmVisitPlan(planId);
        if (crmVisitPlan == null) {
            return MapMessage.errorMessage("所选计划不存在");
        }

        if (!Objects.equals(userId, crmVisitPlan.getUserId())) {
            return MapMessage.errorMessage("只能更新自己的拜访计划!");
        }

        List<CrmVisitPlan> plans = getUserVisitPlan(userId, DayRange.newInstance(visitTime.getTime()).getStartDate(), DayRange.newInstance(visitTime.getTime()).getEndDate());
        if (CollectionUtils.isNotEmpty(plans) && plans.size() >= 5) {
            return MapMessage.errorMessage("同一天计划不能多于五个");
        }

        crmVisitPlan.setVisitTime(visitTime);
        if (StringUtils.isNotBlank(planContent)) {
            crmVisitPlan.setContent(planContent);
        }
        crmVisitPlanDao.update(planId, crmVisitPlan);
        return MapMessage.successMessage();
    }

    public MapMessage updateVisitPTime(Long userId, String planId, Date visitTime) {
        return updateVisitPlan(userId, planId, visitTime, "");
    }

    public MapMessage deleteCrmVisitPlan(Long userId, String planId) {
        if (userId == null) {
            return MapMessage.errorMessage("用户信息获取失败,请重新登录");
        }

        CrmVisitPlan crmVisitPlan = getCrmVisitPlan(planId);
        if (crmVisitPlan == null) {
            return MapMessage.errorMessage("无效的拜访计划:" + planId);
        }

        if (!Objects.equals(userId, crmVisitPlan.getUserId())) {
            return MapMessage.errorMessage("只能更新自己的拜访计划!");
        }

        crmVisitPlan.setDisabled(true);
        crmVisitPlanDao.update(planId, crmVisitPlan);
        return MapMessage.successMessage();
    }

    public MapMessage saveCrmVisitPlan(Long schoolId, AuthCurrentUser user, Date visitTime, String programContent) {
        if (user == null) {
            return MapMessage.errorMessage("用户信息获取失败,请重新登录");
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在");
        }

        if (visitTime == null) {
            return MapMessage.errorMessage("拜访时间填写错误");
        }

        long timestamp = DayRange.current().getStartTime() + 86400 * 1000;
        if (visitTime.before(new Date(timestamp))) {
            return MapMessage.errorMessage("拜访时间只能为第二天之后的时间");
        }

        DayRange dr = DayRange.newInstance(visitTime.getTime());
        List<CrmVisitPlan> plans = getUserVisitPlan(user.getUserId(), dr.getStartDate(), dr.getEndDate());
        if (CollectionUtils.isNotEmpty(plans) && plans.size() >= 5) {
            return MapMessage.errorMessage("同一天计划不能多于五个");
        }

        plans = plans.stream().filter(p -> Objects.equals(schoolId, p.getSchoolId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(plans)) {
            return MapMessage.errorMessage("该学校已经添加过进校计划了");
        }

        CrmVisitPlan crmVisitPlan = new CrmVisitPlan();
        crmVisitPlan.setUserId(user.getUserId());
        crmVisitPlan.setUserName(user.getRealName());
        crmVisitPlan.setSchoolId(schoolId);
        crmVisitPlan.setSchoolName(school.getCname());
        crmVisitPlan.setVisitTime(visitTime);
        crmVisitPlan.setContent(programContent);
        crmVisitPlan.setDisabled(false);
        crmVisitPlanDao.insert(crmVisitPlan);
        return MapMessage.successMessage();
    }

//    // 学校搜索  scope :  1:用户管理的的学校  2：用户所在城市的学校
//    public MapMessage searchVisibleSchool(AuthCurrentUser user, Integer scope, String schoolKey) {
//        if (user == null || user.getUserId() == null) {
//            return MapMessage.errorMessage("用户信息错误请重新登录");
//        }
//        if (!user.isCityManager() && !user.isBusinessDeveloper()) {
//            return MapMessage.errorMessage("当前用户角色不能操作此功能");
//        }
//
//        List<School> schools = new ArrayList<>();
//        if (scope == null || scope == 1) {
//            List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(user.getUserId());
//            if (CollectionUtils.isEmpty(managedSchoolList)) {
//                return MapMessage.errorMessage("用户无管理的学校，请联系上级配置");
//            }
//
//            Map<Long, School> managedSchoolMap = schoolLoaderClient.getSchoolLoader()
//                    .loadSchools(managedSchoolList)
//                    .getUninterruptibly();
//            if (managedSchoolMap == null || managedSchoolMap.size() == 0) {
//                return MapMessage.errorMessage("用户无管理的学校，请联系上级配置");
//            }
//            schools.addAll(managedSchoolMap.values());
//        } else if (scope == 2 && StringUtils.isNotBlank(schoolKey)) {
//            // 获取用户所在的组
//            List<AgentGroup> userGroups = baseOrgService.getUserGroups(user.getUserId());
//            if (CollectionUtils.isNotEmpty(userGroups)) {
//                Set<Integer> regionCodes = new HashSet<>();
//                userGroups.forEach(p -> {
//                    List<AgentGroupRegion> groupRegionList = baseOrgService.getGroupRegionByGroup(p.getId());
//                    if (CollectionUtils.isNotEmpty(groupRegionList)) {
//                        regionCodes.addAll(groupRegionList.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet()));
//                    }
//                });
//
//                Set<Integer> cityCodes = new HashSet<>();
//                regionCodes.forEach(p -> {
//                    ExRegion exRegion = regionServiceClient.getExRegionBuffer().loadRegion(p);
//                    if (exRegion != null) {
//                        cityCodes.add(exRegion.getCityCode());
//                    }
//                });
//                List<Integer> countyList = agentRegionService.getCountyCodes(cityCodes);
//                schools.addAll(loadAreaSchools(countyList));
//            }
//        }
//        //过滤出有效的学校 详见School    认证状态（0等待认证、1已认证、3未通过(假)） 请原谅我这种写法吧,总感觉用枚举会造成误解
//        schools = schools.stream().filter(p -> p.getAuthenticationState() != null && (p.getAuthenticationState() == 0 || p.getAuthenticationState() == 1)).collect(Collectors.toList());
//        // 专员的时候如果schoolKey为空，默认返回所有学校列表，市经理不处理
//        if (StringUtils.isBlank(schoolKey)) {
//            if (user.isBusinessDeveloper()) {
//                return MapMessage.successMessage().add("schools", createSchoolShortInfo(schools));
//            } else {
//                return MapMessage.successMessage().add("schools", new ArrayList<>());
//            }
//        } else {
//            Long schoolId = SafeConverter.toLong(schoolKey);
//            if (schoolId > 0L) {
//                schools = schools.stream().filter(p -> p.getId().equals(schoolId)).collect(Collectors.toList());
//            } else {
//                schools = schools.stream().filter(p -> p.getCname().contains(schoolKey) || (StringUtils.isNotBlank(p.getShortName()) && p.getShortName().contains(schoolKey))).collect(Collectors.toList());
//            }
//
//            return MapMessage.successMessage().add("schools", createSchoolShortInfo(schools));
//        }
//
//    }

    public MapMessage searchVisibleSchool(AuthCurrentUser user, Integer scope, String schoolKey) {
        if (user == null || user.getUserId() == null) {
            return MapMessage.errorMessage("用户信息错误请重新登录");
        }
        List<Long> schoolIds = new ArrayList<>();
        if (StringUtils.isBlank(schoolKey)) {
            if (user.isBusinessDeveloper() && (scope == null || scope == 1)) {
                schoolIds = baseOrgService.getManagedSchoolList(user.getUserId());
            }
        } else {
           /* if(scope == null || scope == 1){
                schoolIds = schoolResourceService.loadBusinessSchoolByScene(user.getUserId(), schoolKey, "dict");
            }else if(scope == 2){*/
//            schoolIds = schoolResourceService.loadBusinessSchoolByScene(user.getUserId(), schoolKey, "space");
            schoolIds = searchService.searchSchoolsForSceneWithNew(user.getUserId(), schoolKey, scope);
            // }
        }
        return MapMessage.successMessage().add("schools", createSchoolShortInfoFromSummary(schoolIds));
    }

    public List<SchoolShortInfo> createSchoolShortInfo(List<School> schools) {
        if (CollectionUtils.isEmpty(schools)) {
            return Collections.emptyList();
        }
        List<SchoolShortInfo> schoolShortList = new ArrayList<>();
        schools.forEach(p -> {
            SchoolShortInfo info = new SchoolShortInfo();
            info.setSchoolId(p.getId());
            info.setSchoolName(p.getCname());
            info.setLevel(p.getLevel());    //1.为小学，2.为中学，4 高中
            schoolShortList.add(info);
        });
        return schoolShortList;
    }

    private List<SchoolShortInfo> createSchoolShortInfoFromSummary(List<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Map<Long, School> mapAlpsFuture = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIds).getUninterruptibly();
        List<SchoolShortInfo> schoolShortInfoList = new ArrayList<>();
        schoolIds.forEach(p -> {
            School school = mapAlpsFuture.get(p);
            if (school != null && (school.getSchoolAuthenticationState() == AuthenticationState.SUCCESS || school.getSchoolAuthenticationState() == AuthenticationState.WAITING)) {
                SchoolShortInfo info = new SchoolShortInfo();
                info.setSchoolId(p);
                info.setSchoolName(school.getCname());
                info.setLevel(school.getLevel() == null ? 1 : school.getLevel());    //1.为小学，2.为中学，4 高中
                schoolShortInfoList.add(info);
            }
        });
        return schoolShortInfoList;
    }

}
