package com.voxlearning.utopia.agent.service.daily;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.constants.AgentDailyScoreIndex;
import com.voxlearning.utopia.agent.dao.mongo.daily.AgentDailyScoreDao;
import com.voxlearning.utopia.agent.dao.mongo.daily.AgentDailyScoreStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDailyScore;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDailyScoreStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 日报得分统计service
 * @author deliang.che
 * @since  2018/11/23
 */
@Named
public class AgentDailyScoreStatisticsService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Inject
    private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject
    private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private AgentDailyScoreStatisticsDao agentDailyScoreStatisticsDao;
    @Inject
    private AgentDailyScoreDao agentDailyScoreDao;
    @Inject
    private BaseOrgService baseOrgService;

    /**
     * 生成部门的统计数据
     * @param groupList
     * @param startDate
     * @param endDate
     * @param dateType
     */
    public void generateGroupStatisticsData(List<AgentGroup> groupList, Date startDate, Date endDate, Integer dateType){

        if(CollectionUtils.isEmpty(groupList) || startDate == null || endDate == null || dateType == null){
            return;
        }

        List<AgentDailyScoreStatistics> dataList = new ArrayList<>();
        DayRange dayRange = DayRange.current();
        WeekRange weekRange = WeekRange.current();
        MonthRange monthRange = MonthRange.current();

        if(dateType == 2){
            startDate = WeekRange.newInstance(startDate.getTime()).getStartDate();
        }else if(dateType == 3){
            startDate = MonthRange.newInstance(startDate.getTime()).getStartDate();
        }

        while (startDate.before(endDate)) {
            // 判断是否是当前日期
            boolean isCurrentDate = false;
            Date targetDate = startDate;
            if(dateType == 1){
                // 判断是否是当前天
                if(startDate.getTime() >= dayRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addDays(startDate, 1);
            }else if(dateType == 2){
                // 判断是否是当前周
                if(startDate.getTime() >= weekRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addWeeks(startDate, 1);
            }else if(dateType == 3){
                // 判断是否是当前月
                if(startDate.getTime() >= monthRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addMonths(startDate, 1);
            }

            if(!isCurrentDate){
                Map<Long, AgentDailyScoreStatistics> dataMap = getGroupRealTimeStatistics(groupList, targetDate, dateType);
                if(MapUtils.isNotEmpty(dataMap)){
                    dataList.addAll(dataMap.values());
                }
            }else {
                // 当前周，当前月的情况下
                if(dateType == 2 || dateType == 3){
                    Map<Long, AgentDailyScoreStatistics> dataMap = getGroupRealTimeStatistics(groupList, targetDate, dateType);
                    if(MapUtils.isNotEmpty(dataMap)){
                        dataList.addAll(dataMap.values());
                    }
                }
            }

            // 删除现有数据
            Set<Long> groupIds = dataList.stream().map(AgentDailyScoreStatistics::getGroupId).collect(Collectors.toSet());
            agentDailyScoreStatisticsDao.disableData(groupIds, SafeConverter.toInt(DateUtils.dateToString(agentWorkRecordStatisticsService.getStartDatePub(targetDate, dateType), "yyyyMMdd")), dateType, 1);
            // 插入新数据
            agentDailyScoreStatisticsDao.inserts(dataList);
            dataList.clear();

            if(isCurrentDate){
                break;
            }
        }
    }

    /**
     * 生成User的统计数据
     * @param userIdList
     * @param startDate
     * @param endDate
     * @param dateType
     */
    public void generateUserStatisticsData(Collection<Long> userIdList, Date startDate, Date endDate, Integer dateType){

        if(CollectionUtils.isEmpty(userIdList) || startDate == null || endDate == null || dateType == null){
            return;
        }

        List<AgentDailyScoreStatistics> dataList = new ArrayList<>();
        DayRange dayRange = DayRange.current();
        WeekRange weekRange = WeekRange.current();
        MonthRange monthRange = MonthRange.current();

        if(dateType == 2){
            startDate = WeekRange.newInstance(startDate.getTime()).getStartDate();
        }else if(dateType == 3){
            startDate = MonthRange.newInstance(startDate.getTime()).getStartDate();
        }
        while (startDate.before(endDate)) {
            // 判断是否是当前日期
            boolean isCurrentDate = false;
            Date targetDate = startDate;
            if(dateType == 1){
                // 判断是否是当前天
                if(startDate.getTime() >= dayRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addDays(startDate, 1);
            }else if(dateType == 2){
                // 判断是否是当前周
                if(startDate.getTime() >= weekRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addWeeks(startDate, 1);
            }else if(dateType == 3){
                // 判断是否是当前月
                if(startDate.getTime() >= monthRange.getStartTime()){
                    isCurrentDate = true;
                }
                startDate = DateUtils.addMonths(startDate, 1);
            }

            if(!isCurrentDate){
                dataList.addAll(generateStatisticsDataForUser(userIdList, targetDate, dateType));
            }else {
                // 当前周，当前月的情况下
                if(dateType == 2 || dateType == 3){
                    dataList.addAll(generateStatisticsDataForUser(userIdList, targetDate, dateType));
                }
            }

            // 删除现有数据
            Set<Long> userIds = dataList.stream().map(AgentDailyScoreStatistics::getUserId).collect(Collectors.toSet());
            agentDailyScoreStatisticsDao.disableData(userIds, SafeConverter.toInt(DateUtils.dateToString(agentWorkRecordStatisticsService.getStartDatePub(targetDate, dateType), "yyyyMMdd")), dateType, 2);
            // 插入新数据
            agentDailyScoreStatisticsDao.inserts(dataList);
            dataList.clear();
            if(isCurrentDate){
                break;
            }
        }
    }


    /**
     * 获取用户统计
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long,AgentDailyScoreStatistics> getUserStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        if (dateType == 1 && agentWorkRecordStatisticsService.isCurrentDatePub(date, dateType)){
            return getUserRealTimeStatistics(userIds, date, dateType);
        }else {
            return getUserHistoryStatistics(userIds, date, dateType);
        }
    }

    /**
     * 获取用户实时统计
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long, AgentDailyScoreStatistics> getUserRealTimeStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        List<AgentDailyScoreStatistics> agentWorkRecordStatistics = generateStatisticsDataForUser(userIds, date, dateType);
        if (CollectionUtils.isNotEmpty(agentWorkRecordStatistics)){
            return agentWorkRecordStatistics.stream().collect(Collectors.toMap(AgentDailyScoreStatistics::getUserId, Function.identity()));
        }
        return new HashMap<>();
    }

    /**
     * 获取用户历史统计
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentDailyScoreStatistics> getUserHistoryStatistics(Collection<Long> userIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(userIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentDailyScoreStatisticsDao.getUserDailyStatistics(userIds, day, dateType);
    }

    /**
     * 生成用户统计数据
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    private List<AgentDailyScoreStatistics> generateStatisticsDataForUser(Collection<Long> userIds, Date date, Integer dateType){
        List<AgentDailyScoreStatistics> dataList = new ArrayList<>();

        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Date endDate = agentWorkRecordStatisticsService.getEndDatePub(date, dateType);

        Integer startDateInt = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        Integer endDateInt = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(endDate,-1), "yyyyMMdd"));

        Map<Long,List<AgentDailyScore>> userDailyListMap = agentDailyScoreDao.loadByUserIdsAndTime(userIds, startDateInt, endDateInt);
        Map<Long,List<AgentDailyScore>> userDailyTotalScoreListMap = new HashMap<>();
        userDailyListMap.forEach((k,v) -> {
            userDailyTotalScoreListMap.put(k,v.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.TOTAL_SCORE).collect(Collectors.toList()));
        });

        Map<Long, AgentGroupUser> userGroupMap = new HashMap<>();
        Map<Long, List<AgentGroupUser>> userGroupListMap = agentGroupUserLoaderClient.findByUserIds(userIds);
        userGroupListMap.forEach((k, v) -> {
            if(CollectionUtils.isNotEmpty(v)){
                userGroupMap.put(k, v.get(0));
            }
        });
        Map<Long, AgentGroup> groupMap = new HashMap<>();
        Set<Long> userGroupIds = userGroupMap.values().stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
        Map<Long, AgentGroup> tem1 = agentGroupLoaderClient.loads(userGroupIds);
        if(MapUtils.isNotEmpty(tem1)){
            groupMap.putAll(tem1);
            Set<Long> parentGroupIds = tem1.values().stream().map(AgentGroup::getParentId).collect(Collectors.toSet());
            groupMap.putAll(agentGroupLoaderClient.loads(parentGroupIds));
        }

        Map<Long, AgentUser> userMap = agentUserLoaderClient.findByIds(userIds);


        List<Future<AgentDailyScoreStatistics>> futureList = new ArrayList<>();
        userIds.forEach(p -> {
            AgentUser user = userMap.get(p);

            AgentGroupUser groupUser = userGroupMap.get(p);

            Long groupId = groupUser.getGroupId();
            AgentGroup group = groupMap.get(groupId);

            AgentGroup parentGroup = groupMap.get(group.getParentId());

            List<AgentDailyScore> dailyTotalScoreList = userDailyTotalScoreListMap.get(p);

            Future<AgentDailyScoreStatistics> futureData = AlpsThreadPool.getInstance().submit(() -> generateUserStatisticsData(user, group, parentGroup, dailyTotalScoreList, startDate, dateType));
            futureList.add(futureData);
        });

        for(Future<AgentDailyScoreStatistics> futureData : futureList){
            try {
                AgentDailyScoreStatistics data = futureData.get();
                if (data != null) {
                    dataList.add(data);
                }
            }catch (Exception e){

            }
        }
        return dataList;
    }


    /**
     * 生成用户统计数据
     * @param user
     * @param group
     * @param parentGroup
     * @param dailyTotalScoreList
     * @param startDate
     * @param dateType
     * @return
     */
    private AgentDailyScoreStatistics generateUserStatisticsData(AgentUser user, AgentGroup group, AgentGroup parentGroup, List<AgentDailyScore> dailyTotalScoreList, Date startDate, Integer dateType){
        if(user == null || group == null || startDate == null || dateType == null){
            return null;
        }
        AgentDailyScoreStatistics statisticsData = new AgentDailyScoreStatistics(SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd")), dateType, 2, group.getId(), group.getGroupName());
        if(parentGroup != null){
            statisticsData.setParentGroupId(parentGroup.getId());
            statisticsData.setParentGroupName(parentGroup.getGroupName());
        }
        statisticsData.setUserId(user.getId());
        statisticsData.setUserName(user.getRealName());
        if(CollectionUtils.isNotEmpty(dailyTotalScoreList)){
            double totalScore = 0;
            for (AgentDailyScore dailyScore : dailyTotalScoreList) {
                totalScore = MathUtils.doubleAddNoScale(totalScore, dailyScore.getScore());
            }
            statisticsData.setDailyScore(MathUtils.doubleDivide(totalScore,dailyTotalScoreList.size(),1));
        }

        return statisticsData;
    }




    /**
     * 获取Group统计信息
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    public Map<Long,AgentDailyScoreStatistics> getGroupStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        if (dateType == 1 && agentWorkRecordStatisticsService.isCurrentDatePub(date, dateType)){
            return getGroupRealTimeStatisticsByIds(groupIds, date, dateType);
        }else {
            return getGroupHistoryStatistics(groupIds, date, dateType);
        }
    }

    /**
     * 获取Group实时统计信息
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentDailyScoreStatistics> getGroupRealTimeStatisticsByIds(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }

        List<AgentGroup> agentGroupList = baseOrgService.getGroupByIds(groupIds);
        if (CollectionUtils.isEmpty(agentGroupList)){
            return new HashMap<>();
        }
        return getGroupRealTimeStatistics(agentGroupList, date, dateType);
    }

    /**
     * 获取group历史统计
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long,AgentDailyScoreStatistics> getGroupHistoryStatistics(Collection<Long> groupIds, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groupIds) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Integer day = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        return agentDailyScoreStatisticsDao.getGroupDailyStatistics(groupIds, day, dateType);
    }

    /**
     * 生成Group实时统计信息
     * @param groups
     * @param date
     * @param dateType
     * @return
     */
    private Map<Long, AgentDailyScoreStatistics> getGroupRealTimeStatistics(Collection<AgentGroup> groups, Date date, Integer dateType){
        if (CollectionUtils.isEmpty(groups) || null == date || dateType == null) {
            return new HashMap<>();
        }
        Map<Long,AgentDailyScoreStatistics> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groups)){
            List<Future<AgentDailyScoreStatistics>> futureList = new ArrayList<>();
            groups.forEach(item -> {
                Future<AgentDailyScoreStatistics> futureData = AlpsThreadPool.getInstance().submit(() -> generateStatisticsDataForGroup(item, date, dateType));
                futureList.add(futureData);

            });
            for(Future<AgentDailyScoreStatistics> futureData : futureList){
                try {
                    AgentDailyScoreStatistics data = futureData.get();
                    if(data != null && data.getGroupId() != null){
                        result.putIfAbsent(data.getGroupId(), data);
                    }
                }catch (Exception e){

                }
            }
        }
        return result;

    }

    /**
     * 生成Group统计信息
     * @param group
     * @param date
     * @param dateType
     * @return
     */
    private AgentDailyScoreStatistics generateStatisticsDataForGroup(AgentGroup group, Date date, Integer dateType){
        Date startDate = agentWorkRecordStatisticsService.getStartDatePub(date, dateType);
        Date endDate = agentWorkRecordStatisticsService.getEndDatePub(date, dateType);

        List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(group.getId());

        Set<Long> userIds = groupUserList.stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        AgentDailyScoreStatistics statisticsData = new AgentDailyScoreStatistics(SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd")), dateType, 1, group.getId(), group.getGroupName());
        AgentGroup parentGroup = baseOrgService.getGroupById(group.getParentId());
        if(parentGroup != null){
            statisticsData.setParentGroupId(parentGroup.getId());
            statisticsData.setParentGroupName(parentGroup.getGroupName());
        }
        Integer startDateInt = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        Integer endDateInt = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addDays(endDate,-1), "yyyyMMdd"));
        Map<Long, List<AgentDailyScore>> userDailyScoreListMap = agentDailyScoreDao.loadByUserIdsAndTime(userIds, startDateInt, endDateInt);

        Map<Long,List<AgentDailyScore>> userDailyTotalScoreListMap = new HashMap<>();
        userDailyScoreListMap.forEach((k,v) -> {
            userDailyTotalScoreListMap.put(k,v.stream().filter(p -> p.getIndex() == AgentDailyScoreIndex.TOTAL_SCORE).collect(Collectors.toList()));
        });

        double userTotalScore = 0;
        int userScoreNum = 0;   //有得分的人数
        for (Long userId : userIds){
            List<AgentDailyScore> dailyTotalScoreList = userDailyTotalScoreListMap.get(userId);
            if (CollectionUtils.isNotEmpty(dailyTotalScoreList)){

                double totalScore = 0;
                double averageScore = 0;
                for (AgentDailyScore dailyScore : dailyTotalScoreList) {
                    totalScore = MathUtils.doubleAdd(totalScore,dailyScore.getScore());
                }
                averageScore = MathUtils.doubleDivide(totalScore,dailyTotalScoreList.size());

                userTotalScore = MathUtils.doubleAddNoScale(userTotalScore, averageScore);
                userScoreNum ++;
            }
        }
        if (userScoreNum > 0){
            statisticsData.setDailyScore(MathUtils.doubleDivide(userTotalScore,userScoreNum,1));
        }
        return statisticsData;
    }


}
