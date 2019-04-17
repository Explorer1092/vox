package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupUserDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupUserStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroup;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupUser;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupUserStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.activity.ActivityGroupUserStatisticsView;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ActivityGroupUserStatisticsService {

    @Inject
    private ActivityGroupUserStatisticsDao groupUserStatisticsDao;
    @Inject
    private ActivityGroupDao groupDao;
    @Inject
    private ActivityGroupUserDao groupUserDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void userCountStatistics(ActivityGroupUser groupUser){
        if(groupUser == null || StringUtils.isBlank(groupUser.getGroupId()) || groupUser.getJoinTime() == null){
            return;
        }

        ActivityGroup group = groupDao.loadByGid(groupUser.getGroupId());
        if(group == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(groupUser.getJoinTime(), "yyyyMMdd"));

        ActivityGroupUserStatistics groupUserStatistics = groupUserStatisticsDao.loadByUidAndDay(group.getActivityId(), group.getUserId(), day);
        if(groupUserStatistics == null){
            groupUserStatistics = new ActivityGroupUserStatistics();
            groupUserStatistics.setActivityId(group.getActivityId());
            groupUserStatistics.setUserId(group.getUserId());
            groupUserStatistics.setUserName(group.getUserName());
            groupUserStatistics.setDay(day);
        }

        groupUserStatistics.setUserCount(SafeConverter.toInt(groupUserStatistics.getUserCount()) + 1);
        groupUserStatisticsDao.upsert(groupUserStatistics);
    }

    public void completeUserCountStatistics(ActivityGroup group){
        if(group == null || StringUtils.isBlank(group.getGroupId()) || !SafeConverter.toBoolean(group.getIsComplete()) || group.getCompleteTime() == null){
            return;
        }

        List<ActivityGroupUser> userList = groupUserDao.loadByGid(group.getGroupId());
        if(CollectionUtils.isEmpty(userList)){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(group.getCompleteTime(), "yyyyMMdd"));

        ActivityGroupUserStatistics groupUserStatistics = groupUserStatisticsDao.loadByUidAndDay(group.getActivityId(), group.getUserId(), day);
        if(groupUserStatistics == null){
            groupUserStatistics = new ActivityGroupUserStatistics();
            groupUserStatistics.setActivityId(group.getActivityId());
            groupUserStatistics.setUserId(group.getUserId());
            groupUserStatistics.setUserName(group.getUserName());
            groupUserStatistics.setDay(day);
        }
        groupUserStatistics.setCompleteUserCount(SafeConverter.toInt(groupUserStatistics.getCompleteUserCount()) + userList.size());
        groupUserStatisticsDao.upsert(groupUserStatistics);
    }

    public List<ActivityGroupUserStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityGroupUserStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityGroupUserStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityGroupUserStatisticsView> future : futureList) {
            try {
                ActivityGroupUserStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityGroupUserStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityGroupUserStatistics> totalDataList = new ArrayList<>();
        List<ActivityGroupUserStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityGroupUserStatistics> dataList = groupUserStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityGroupUserStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        return generateDataView(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, group.getGroupName(), targetDayDataList, totalDataList);
    }

    public List<ActivityGroupUserStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityGroupUserStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityGroupUserStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityGroupUserStatisticsView> future : futureList) {
            try {
                ActivityGroupUserStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityGroupUserStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityGroupUserStatistics> totalDataList = new ArrayList<>();
        List<ActivityGroupUserStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityGroupUserStatistics> dataList = groupUserStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityGroupUserStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        return generateDataView(user.getId(), AgentConstants.INDICATOR_TYPE_USER, user.getRealName(), targetDayDataList, totalDataList);
    }

    private ActivityGroupUserStatisticsView generateDataView(Long id, Integer idType, String name, List<ActivityGroupUserStatistics> targetDayDataList, List<ActivityGroupUserStatistics> totalDataList){
        ActivityGroupUserStatisticsView view = new ActivityGroupUserStatisticsView();
        view.setId(id);
        view.setIdType(idType);
        view.setName(name);

        int dayUserCount = 0;
        int dayCompleteUserCount = 0;
        for(ActivityGroupUserStatistics statistics : targetDayDataList){
            dayUserCount += SafeConverter.toInt(statistics.getUserCount());
            dayCompleteUserCount += SafeConverter.toInt(statistics.getCompleteUserCount());
        }
        view.setDayUserCount(dayUserCount);
        view.setDayCompleteUserCount(dayCompleteUserCount);

        int totalUserCount = 0;
        int totalCompleteUserCount = 0;
        for(ActivityGroupUserStatistics statistics : totalDataList){
            totalUserCount += SafeConverter.toInt(statistics.getUserCount());
            totalCompleteUserCount += SafeConverter.toInt(statistics.getCompleteUserCount());
        }
        view.setTotalUserCount(totalUserCount);
        view.setTotalCompleteUserCount(totalCompleteUserCount);
        return view;
    }

    public List<ActivityGroupUserStatistics> getGroupUserStatistics(String activityId, Collection<Long> userIds, Collection<Integer> days){
        return groupUserStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
    }
}
