package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.constants.AgentPerformanceStatisticsType;
import com.voxlearning.utopia.agent.persist.AgentPerformanceStatisticsPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceStatistics;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PerformanceStatisticsService
 *
 * @author song.wang
 * @date 2017/3/27
 */
@Named
public class PerformanceStatisticsService extends AbstractAgentService {

    @Inject
    AgentPerformanceStatisticsPersistence agentPerformanceStatisticsPersistence;

    @Inject
    PerformanceService performanceService;
    @Inject
    BaseOrgService baseOrgService;


    public List<AgentPerformanceStatistics> loadUserPerformanceStatistics(Long userId){
        List<AgentPerformanceStatistics> retList = new ArrayList<>();
        // 获取当前月份的业绩数据
        Integer day = performanceService.lastSuccessDataDay();
        List<AgentPerformanceStatistics> currentMonthPerformance = loadUserStatisticsDataList(userId, day);
        if(CollectionUtils.isNotEmpty(currentMonthPerformance)){
            retList.addAll(currentMonthPerformance);
        }
        // 获取历史业绩统计
        List<AgentPerformanceStatistics> historyPerformanceList = agentPerformanceStatisticsPersistence.findByUserId(userId);
        if(CollectionUtils.isNotEmpty(historyPerformanceList)){
            retList.addAll(historyPerformanceList);
        }
        return retList;
    }

    private List<AgentPerformanceStatistics> loadUserStatisticsDataList(Long userId, Integer day){

        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return Collections.emptyList();
        }

        AgentRoleType userRoleType = baseOrgService.getUserRole(userId);

        Integer month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
        AgentGroup regionGroup = null;
        AgentGroup cityGroup = null;
        List<Long> regionGroupList = baseOrgService.getGroupListByRole(userId, AgentGroupRoleType.Region);
        if(CollectionUtils.isNotEmpty(regionGroupList)){
            regionGroup = baseOrgService.getGroupById(regionGroupList.get(0));
            if(regionGroup != null){
                List<Long> cityGroupList = baseOrgService.getGroupListByRole(userId, AgentGroupRoleType.City);
                if(CollectionUtils.isNotEmpty(cityGroupList)){
                    cityGroup = baseOrgService.getGroupById(cityGroupList.get(0));
                }
            }
        }

        List<AgentPerformanceStatistics> retList = new ArrayList<>();
        AgentPerformanceStatistics item;
        for(AgentKpiType kpiType : AgentKpiType.fetchValidTypeList()){
            item = new AgentPerformanceStatistics();
            item.setMonth(month);
            if(regionGroup != null){
                item.setRegionGroupId(regionGroup.getId());
                item.setRegionGroupName(regionGroup.getGroupName());
            }
            if(cityGroup != null){
                item.setCityGroupId(cityGroup.getId());
                item.setCityGroupName(cityGroup.getGroupName());
            }
            item.setStatisticsType(AgentPerformanceStatisticsType.USER);
            item.setUserId(user.getId());
            item.setUserName(user.getRealName());
            item.setUserRoleType(userRoleType);
            item.setPerformanceKpiType(kpiType);
            Map<String, Object> dataMap = new HashMap<>();
            item.setBudget(SafeConverter.toLong(dataMap.get("budget")));
            item.setComplete(SafeConverter.toLong(dataMap.get("masc")));
            item.setCompleteRate(SafeConverter.toDouble(dataMap.get("completeRate")));
            retList.add(item);
        }
        return retList;
    }



    public List<AgentPerformanceStatistics> loadManagedUserDataByMonth(Long userId, Integer month){
        // 过滤出用户管理的人员
        List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        Set<Long> managedUserIds = groupIds.stream().map(p -> baseOrgService.getAllGroupUsersByGroupId(p)).filter(CollectionUtils::isNotEmpty).flatMap(List::stream)
                .filter(t -> !Objects.equals(t.getUserId(), userId) && (t.getUserRoleType() == AgentRoleType.BusinessDeveloper || t.getUserRoleType() == AgentRoleType.CityManager || t.getUserRoleType() == AgentRoleType.Region))
                .map(AgentGroupUser::getUserId).collect(Collectors.toSet());

        List<AgentPerformanceStatistics> performanceStatisticsList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(managedUserIds)){
            performanceStatisticsList = loadUserDataByMonth(managedUserIds, month);
        }


        return performanceStatisticsList;
    }

    private List<AgentPerformanceStatistics> loadUserDataByMonth(Collection<Long> userIds, Integer month){
        List<AgentPerformanceStatistics> retList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return retList;
        }
        Integer day = performanceService.lastSuccessDataDay();
        Integer performanceMonth = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
        if(Objects.equals(month, performanceMonth)){
            // 获取当前月份的业绩数据
            List<AgentPerformanceStatistics> currentMonthDataList = userIds.stream().map(p -> loadUserStatisticsDataList(p, day)).filter(CollectionUtils::isNotEmpty).flatMap(List::stream).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(currentMonthDataList)){
                retList.addAll(currentMonthDataList);
            }
        }

        // 获取历史业绩统计
        Map<Long, List<AgentPerformanceStatistics>> historyUserPerformanceMap = agentPerformanceStatisticsPersistence.findByUserIdsAndMonth(userIds, month);
        if(MapUtils.isNotEmpty(historyUserPerformanceMap)){
            List<AgentPerformanceStatistics> historyList = historyUserPerformanceMap.values().stream().filter(CollectionUtils::isNotEmpty).flatMap(List::stream).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(historyList)){
                retList.addAll(historyList);
            }
        }

//        Collections.sort(retList, ((o1, o2) -> {
//            int monthCompare = o1.getMonth().compareTo(o2.getMonth());
//            if(monthCompare != 0){
//                return monthCompare;
//            }
//
//            return 1;
//        }));
        return retList;
    }


    public List<AgentPerformanceStatistics> loadManagedGroupDataListByMonth(Long userId, Integer month){
        // 过滤出用户管理的人员
        List<AgentPerformanceStatistics> retList = new ArrayList<>();
        List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(groupIds)){
            retList = loadGroupDataListByMonth(groupIds, month);
        }
        return retList;
    }


    private List<AgentPerformanceStatistics> loadGroupDataListByMonth(Collection<Long> groupIds, Integer month){
        List<AgentPerformanceStatistics> retList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return retList;
        }
        Integer day = performanceService.lastSuccessDataDay();
        Integer performanceMonth = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));
        if(Objects.equals(month, performanceMonth)){
            // 获取当月部门业绩数据
            List<AgentPerformanceStatistics> currentMonthDataList = groupIds.stream().map(p -> loadGroupStatisticsDataList(p, day)).filter(CollectionUtils::isNotEmpty).flatMap(List::stream).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(currentMonthDataList)){
                retList.addAll(currentMonthDataList);
            }
        }

        // 获取历史业绩统计
        Map<Long, List<AgentPerformanceStatistics>> historyGroupPerformanceMap = agentPerformanceStatisticsPersistence.findByGroupIdsAndMonth(groupIds, month);
        if(MapUtils.isNotEmpty(historyGroupPerformanceMap)){
            List<AgentPerformanceStatistics> historyList = historyGroupPerformanceMap.values().stream().filter(CollectionUtils::isNotEmpty).flatMap(List::stream).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(historyList)){
                retList.addAll(historyList);
            }
        }

//        Collections.sort(retList, ((o1, o2) -> {
//            int monthCompare = o1.getMonth().compareTo(o2.getMonth());
//            if(monthCompare != 0){
//                return monthCompare;
//            }
//
//            return 1;
//        }));
        return retList;
    }


    // 获取部门在指定日期的业绩数据
    private List<AgentPerformanceStatistics> loadGroupStatisticsDataList(Long groupId, Integer day){

        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return Collections.emptyList();
        }

        Integer month = SafeConverter.toInt(DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyyMM"));

        AgentGroup cityGroup = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.City);
        AgentGroup regionGroup = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.Region);

        List<AgentPerformanceStatistics> retList = new ArrayList<>();
        AgentPerformanceStatistics item;
        for(AgentKpiType kpiType : AgentKpiType.fetchValidTypeList()){
            item = new AgentPerformanceStatistics();
            item.setMonth(month);
            if(regionGroup != null){
                item.setRegionGroupId(regionGroup.getId());
                item.setRegionGroupName(regionGroup.getGroupName());
            }
            if(cityGroup != null){
                item.setCityGroupId(cityGroup.getId());
                item.setCityGroupName(cityGroup.getGroupName());
            }
            item.setStatisticsType(AgentPerformanceStatisticsType.GROUP);
            item.setGroupId(groupId);
            item.setGroupRoleType(group.fetchGroupRoleType());
            item.setPerformanceKpiType(kpiType);
            Map<String, Object> dataMap = new HashMap<>();
            item.setBudget(SafeConverter.toLong(dataMap.get("budget")));
            item.setComplete(SafeConverter.toLong(dataMap.get("masc")));
            item.setCompleteRate(SafeConverter.toDouble(dataMap.get("completeRate")));
            retList.add(item);
        }
        return retList;
    }



}
