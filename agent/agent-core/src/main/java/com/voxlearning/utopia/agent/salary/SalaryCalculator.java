/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.salary;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016Persistence;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.mobile.AgentPerformanceRankingService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentPerformanceConfigService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/9/24
 */
@Slf4j
public abstract class SalaryCalculator {

    @Inject
    protected BaseOrgService baseOrgService;

    @Inject
    protected AgentPerformanceRankingService agentPerformanceRankingService;
    @Inject
    protected AgentUserKpiResultSpring2016Persistence agentUserKpiResultSpring2016Persistence;
    @Inject
    protected AgentPerformanceConfigService agentPerformanceConfigService;
    @Inject
    protected BaseUserService baseUserService;

    protected SalaryCalculatorContext context;


    // 启动计算在职员工工资
    public abstract void calculate(List<AgentUser> users);

    // 启动计算已离职员工工资
    public abstract void calculateFormer(List<FormerEmployeeData> formerEmployeeDataList);

    public void setContext(SalaryCalculatorContext context) {
        this.context = context;
    }

    // 获取用户所在的城市的级别
    protected AgentCityLevelType getUserCityLevel(Long userId) {
        AgentRoleType role = baseOrgService.getUserRole(userId);
        if (role != null && AgentRoleType.BusinessDeveloper != role && AgentRoleType.CityAgent != role && AgentRoleType.CityManager != role && AgentRoleType.CityAgentLimited != role) {
            return null;
        }
        List<Long> groupList = baseOrgService.getGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(groupList)) {
            return null;
        }
        List<AgentGroupRegion> groupRegionList = new ArrayList<>();
        groupList.stream().map(baseOrgService::getGroupRegionByGroup).filter(CollectionUtils::isNotEmpty).forEach(groupRegionList::addAll);
        Set<Integer> regionList = groupRegionList.stream().map(AgentGroupRegion::getRegionCode).filter(p -> p != null).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(regionList)) {
            return null;
        }
        Integer regionCode = regionList.stream().findFirst().get();
        return context.regionLevel(regionCode);
    }

    // 判断中小学是否是代理模式
    protected boolean isAgentModel(Long userId, SchoolLevel schoolLevel) {
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        // 只有专员和市经理区分代理和直营模式
        if (AgentRoleType.BusinessDeveloper != userRole && AgentRoleType.CityManager != userRole && AgentRoleType.CityAgentLimited != userRole) {
            return false;
        }
        List<Long> schoolIdList = baseOrgService.getManagedSchoolList(userId);
        List<Long> managedTargetSchoolList = baseOrgService.getSchoolListByLevel(schoolIdList, schoolLevel);
        if (CollectionUtils.isEmpty(managedTargetSchoolList)) {
            return false;
        }
        Map<Long, List<AgentUserSchool>> schoolMap = baseOrgService.getUserSchoolBySchools(managedTargetSchoolList);
        if (MapUtils.isEmpty(schoolMap)) {
            return false;
        }
        for (List<AgentUserSchool> userSchoolList : schoolMap.values()) {
            // 过滤出负责该学校的其他人员
            List<AgentUserSchool> tragetUserSchoolList = userSchoolList.stream().filter(p -> !Objects.equals(p.getUserId(), userId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tragetUserSchoolList)) {
                long agentCount = tragetUserSchoolList.stream().map(u -> baseOrgService.getUserRole(u.getUserId())).filter(r -> AgentRoleType.CityAgent == r || AgentRoleType.CityAgentLimited == r).count();
                if (agentCount > 0) { // 负责该学校的
                    return true;
                }
            }
        }
        return false;
    }


    protected void saveUserCpaMonthlyResult(AgentUser user, Integer salaryMonth, School school,
                                            Date firstDate, Date endDate,
                                            SalaryKpiType kpiType,
                                            Long cpaTarget, Long result, Double salary, String note) {

        try {
            AgentUserKpiResultSpring2016 userKpiResult = new AgentUserKpiResultSpring2016();

            // 设置用户所在的大区级部门信息
            List<Long> regionRroupIdList = baseOrgService.getGroupListByRole(user.getId(), AgentGroupRoleType.Region);
            List<AgentGroup> regionGroupList = baseOrgService.getGroupByIds(regionRroupIdList);
            if (CollectionUtils.isNotEmpty(regionGroupList)) {
                AgentGroup firstGroup = regionGroupList.get(0);
                userKpiResult.setRegionId(firstGroup.getId());
                userKpiResult.setRegionName(firstGroup.getGroupName());
            } else {
                userKpiResult.setRegionId(0L);
                userKpiResult.setRegionName(null);
            }

            // 设置用户所在的市级部门信息
            List<Long> cityRroupIdList = baseOrgService.getGroupListByRole(user.getId(), AgentGroupRoleType.City);
            List<AgentGroup> cityGroupList = baseOrgService.getGroupByIds(cityRroupIdList);
            if (CollectionUtils.isNotEmpty(cityGroupList)) {
                AgentGroup firstGroup = cityGroupList.get(0);
                userKpiResult.setProvinceId(firstGroup.getId());
                userKpiResult.setProvinceName(firstGroup.getGroupName());
            } else {
                userKpiResult.setProvinceId(0L);
                userKpiResult.setProvinceName(null);
            }


            userKpiResult.setCountyCode(0);
            userKpiResult.setCountyName(null);
//            if (region != 0L) {
//                userKpiResult.setCountyName(agentRegionService.getRegionName(region));
//            } else {
//                userKpiResult.setCountyName(null);
//            }

            if (school != null) {
                userKpiResult.setSchoolId(school.getId());
                userKpiResult.setSchoolName(school.getCname());
                userKpiResult.setSchoolLevel(school.getLevel());
            } else {
                userKpiResult.setSchoolId(null);
                userKpiResult.setSchoolName(null);
                userKpiResult.setSchoolLevel(null);
            }
            userKpiResult.setSalaryMonth(salaryMonth);
            userKpiResult.setUserId(user.getId());
            userKpiResult.setUserName(user.getRealName());
            userKpiResult.setFinanceCheck(false);
            userKpiResult.setMarketCheck(false);

            userKpiResult.setStartDate(firstDate);
            userKpiResult.setEndDate(endDate);
            userKpiResult.setCpaType(kpiType.getDesc());
            userKpiResult.setCpaTarget(cpaTarget);
            userKpiResult.setCpaResult(result);
            userKpiResult.setCpaSalary(roundHalfUpLong(salary));
            userKpiResult.setCpaNote(note);

            agentUserKpiResultSpring2016Persistence.persist(userKpiResult);
        } catch (Exception ex) {
            log.error("保存KPI结果失败", ex);
            throw new UtopiaRuntimeException("保存KPI结果失败");
        }
    }

    private long roundHalfUpLong(Double doubleValue) {
        return new BigDecimal(doubleValue).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    protected Date getSalaryStartDate(AgentUser user) {
        Date salaryStartDate;
        Date firstDayOfMonth = DayUtils.getFirstDayOfMonth(context.getRunTime());
        if (user.getContractStartDate() != null) {
            if (user.getContractStartDate().before(DateUtils.addDays(firstDayOfMonth, 5))) {
                salaryStartDate = firstDayOfMonth;
            } else if (user.getContractStartDate().after(DateUtils.addDays(firstDayOfMonth, 5)) && user.getContractStartDate().before(DateUtils.addDays(firstDayOfMonth, 19))) {
                salaryStartDate = user.getContractStartDate();
            } else {
                salaryStartDate = user.getContractStartDate();
            }
        } else {
            salaryStartDate = DateUtils.stringToDate(String.valueOf(DateUtils.dateToString(new Date(), "yyyyMMdd")), "yyyyMMdd");
        }

        return salaryStartDate;
    }

    protected Date getSalaryEndDate(AgentUser user) {
        Date contractEndDate = user.getContractEndDate();
        if (contractEndDate == null) {
            return DayUtils.getLastDayOfMonth(context.getRunTime());
        }
        Date firstDayOfMonth = DayUtils.getFirstDayOfMonth(context.getRunTime());
        Date lastDayOfMonth = DayUtils.getLastDayOfMonth(context.getRunTime());
        if (contractEndDate.after(firstDayOfMonth) && contractEndDate.before(DayUtils.addDay(lastDayOfMonth, 1))) {
            return contractEndDate;
        } else if (contractEndDate.after(lastDayOfMonth)) {
            return lastDayOfMonth;
        }
        return firstDayOfMonth;
    }

    // 获取用户本月参与工资结算的天数
    protected int getSalaryDays(AgentUser user) {
        Date salaryStartDate = getSalaryStartDate(user);
        if (salaryStartDate == null || DayUtils.getDay(salaryStartDate) > 19) {
            return 0;
        }
        Date salaryEndDate = getSalaryEndDate(user);
        int diffDays = (int) DateUtils.dayDiff(salaryEndDate, salaryStartDate);
        if (diffDays < 0) {
            return 0;
        }
        return diffDays + 1;
    }

    // 获取用户工作的天数比例（小数点后 3 位）
    protected double getSalaryDayRate(AgentUser user) {
        int salaryDays = getSalaryDays(user);
        int monthDays = DayUtils.getMonthDays(context.getRunTime());
        return MathUtils.doubleDivide(salaryDays, monthDays, 3);
    }


}
