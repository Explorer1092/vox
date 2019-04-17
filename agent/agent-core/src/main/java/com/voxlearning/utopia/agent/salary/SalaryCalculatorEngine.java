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

package com.voxlearning.utopia.agent.salary;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.persist.spring2016.AgentUserKpiResultSpring2016Persistence;
import com.voxlearning.utopia.agent.salary.autumn2016.*;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/9/24
 */
@Named
@Slf4j
public class SalaryCalculatorEngine {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentRegionService agentRegionService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentDictSchoolService agentDictSchoolService;

    @Inject
    private BusinessDeveloperSalaryCalculator businessDeveloperSalaryCalculator;
    @Inject
    private CityAgentSalaryCalculator cityAgentSalaryCalculator;
    @Inject
    private CityManagerSalaryCalculator cityManagerSalaryCalculator;
    @Inject
    private RegionManagerSalaryCalculator regionManagerSalaryCalculator;
    @Inject
    private DictSchoolCalculator dictSchoolCalculator;
    @Inject
    private PerformanceService performanceService;
    @Inject
    protected AgentUserKpiResultSpring2016Persistence agentUserKpiResultSpring2016Persistence;
    @Inject
    private BaseDictService baseDictService;

    // JOB入口
    public synchronized void execute() {
        Date evalDate = DateUtils.truncate(new Date(), Calendar.DATE);
        start(evalDate, null, null, true);
    }

    // 启动入口
    public synchronized boolean start(Date evalDate, List<Long> userIds, List<FormerEmployeeData> formerEmployeeDataList, boolean includeDictSchool) {
        if (evalDate == null) {
            log.error("Null evalDate");
            return false;
        }

        Date start = new Date();
        log.info("Start @ {}", start);


        // 初始化context
        Map<AgentRoleType, List<AgentUser>> roleUsers = buildRoleUsers(userIds);
        Map<Integer, AgentCityLevelType> levelRegions = buildLevelRegions();
        Map<String, Map<String, Object>> regionTree = agentRegionService.getAllRegionTreeCopy();
        List<AgentDictSchool> dictSchoolList = baseDictService.loadAllSchoolDictData();
        Map<AgentRoleType, List<FormerEmployeeData>> formerUserDatas = buildFormerRoleUsers(formerEmployeeDataList);

        Date performanceDate = performanceService.lastSuccessDataDate();
        if (evalDate.after(performanceDate)) {
            evalDate = performanceDate;
        }
        SalaryCalculatorContext context = new SalaryCalculatorContext(evalDate, roleUsers, levelRegions, regionTree, dictSchoolList, formerUserDatas, includeDictSchool);

        // 启动计算器
        startCalculator(context);

        Date end = new Date();
        long timeCost = end.getTime() - start.getTime();
        log.info("Finish @ {} for evalDate = {}, timeCost = {} ms", end, evalDate, timeCost);

        return true;
    }


    // 按userId过滤
    private List<AgentUser> filter(List<AgentGroupUser> groupUserList, List<Long> userIds) {
        if (CollectionUtils.isEmpty(groupUserList)) {
            return Collections.emptyList();
        }
        Set<Long> userIdSet = groupUserList.stream().map(AgentGroupUser::getUserId).filter(p -> CollectionUtils.isEmpty(userIds) || userIds.contains(p)).collect(Collectors.toSet());
        return userIdSet.stream().map(baseOrgService::getUser).filter(p -> p != null).collect(Collectors.toList());
    }

    // 启动计算器
    private void startCalculator(SalaryCalculatorContext context) {
        // 清除所有当月工资数据
//        agentUserKpiResultSpring2016Persistence.deleteByMonth(context.getSalaryMonth());
        // 计算在职人员不同角色的工资数据
        Map<AgentRoleType, List<AgentUser>> roleUsers = context.getRoleUsers();
        for (AgentRoleType role : roleUsers.keySet()) {
            List<AgentUser> users = roleUsers.get(role);
            if (CollectionUtils.isEmpty(users)) {
                log.error("Empty users with role = {}", role);
                continue;
            }
            SalaryCalculator calculator = dispatch(role);
            if (calculator == null) {
                log.error("Null SalaryCalculator for role = {}", role);
                continue;
            }
            calculator.setContext(context);
            calculator.calculate(users);
        }

        // 计算离职人员不同角色的工资数据
        Map<AgentRoleType, List<FormerEmployeeData>> formerEmployeeDataMap = context.getFormerEmployeeDatas();
        for (AgentRoleType role : formerEmployeeDataMap.keySet()) {
            List<FormerEmployeeData> formerEmployeeDataList = formerEmployeeDataMap.get(role);
            if (CollectionUtils.isEmpty(formerEmployeeDataList)) {
                log.error("Empty users with role = {}", role);
                continue;
            }
            SalaryCalculator calculator = dispatch(role);
            if (calculator == null) {
                log.error("Null SalaryCalculator for role = {}", role);
                continue;
            }
            calculator.setContext(context);
            calculator.calculateFormer(formerEmployeeDataList);
        }

        if (context.getIncludeDictSchool() != null && context.getIncludeDictSchool()) {
            // 生成字典表的业绩数据
            dictSchoolCalculator.setContext(context);
            dictSchoolCalculator.calculate(null);
        }
    }

    // 按角色调度SalaryCalculator
    private SalaryCalculator dispatch(AgentRoleType role) {
        switch (role) {
            case Region:
                return regionManagerSalaryCalculator;
            case CityManager:
                return cityManagerSalaryCalculator;
            case CityAgent:
                return cityAgentSalaryCalculator;
            case BusinessDeveloper:
                return businessDeveloperSalaryCalculator;
            case CityAgentLimited:
                return cityAgentSalaryCalculator;
            default:
                return null;
        }
    }

    // 按角色分组
    private Map<AgentRoleType, List<AgentUser>> buildRoleUsers(List<Long> userIds) {
        Map<AgentRoleType, List<AgentUser>> roleUsers = new HashMap<>();
        List<AgentGroupUser> regionManagerUserList = baseOrgService.getGroupUserByRole(AgentRoleType.Region.getId());
        roleUsers.put(AgentRoleType.Region, filter(regionManagerUserList, userIds));

        List<AgentGroupUser> cityManagerUserList = baseOrgService.getGroupUserByRole(AgentRoleType.CityManager.getId());
        roleUsers.put(AgentRoleType.CityManager, filter(cityManagerUserList, userIds));

        List<AgentGroupUser> businessDeveloperUserList = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId());
        roleUsers.put(AgentRoleType.BusinessDeveloper, filter(businessDeveloperUserList, userIds));

        List<AgentGroupUser> cityAgentUserList = baseOrgService.getGroupUserByRole(AgentRoleType.CityAgent.getId());
        roleUsers.put(AgentRoleType.CityAgent, filter(cityAgentUserList, userIds));

        List<AgentGroupUser> cityAgentLimitedUserList = baseOrgService.getGroupUserByRole(AgentRoleType.CityAgentLimited.getId());
        roleUsers.put(AgentRoleType.CityAgentLimited, filter(cityAgentLimitedUserList, userIds));

        return roleUsers;
    }

    // 城市级别
    private Map<Integer, AgentCityLevelType> buildLevelRegions() {
        Map<Integer, AgentCityLevelType> levelRegions = new HashMap<>();
        for (AgentCityLevelType level : AgentCityLevelType.values()) {
            raikouSystem.getRegionBuffer().findByTag(level.name())
                    .forEach(e -> levelRegions.put(e, level));
        }
        return levelRegions;
    }

    private Map<AgentRoleType, List<FormerEmployeeData>> buildFormerRoleUsers(List<FormerEmployeeData> formerEmployeeDataList) {
        if (CollectionUtils.isEmpty(formerEmployeeDataList)) {
            return Collections.emptyMap();
        }
        return formerEmployeeDataList.stream().collect(Collectors.groupingBy(FormerEmployeeData::getRoleType, Collectors.toList()));
    }


}
