package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityGroupStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroup;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityGroupStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.activity.ActivityGroupStatisticsView;
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
public class ActivityGroupStatisticsService {

    @Inject
    private ActivityGroupStatisticsDao groupStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void newGroupStatistics(ActivityGroup group){
        if(group == null || StringUtils.isBlank(group.getActivityId()) || group.getUserId() == null || group.getGroupTime() == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(group.getGroupTime(), "yyyyMMdd"));

        ActivityGroupStatistics groupStatistics = groupStatisticsDao.loadByUidAndDay(group.getActivityId(), group.getUserId(), day);
        if(groupStatistics == null){
            groupStatistics = new ActivityGroupStatistics();
            groupStatistics.setActivityId(group.getActivityId());
            groupStatistics.setUserId(group.getUserId());
            groupStatistics.setUserName(group.getUserName());
            groupStatistics.setDay(day);
        }

        groupStatistics.setGroupCount(SafeConverter.toInt(groupStatistics.getGroupCount()) + 1);
        groupStatisticsDao.upsert(groupStatistics);
    }

    public void completeGroupStatistics(ActivityGroup group){
        if(group == null || StringUtils.isBlank(group.getActivityId()) || group.getUserId() == null || group.getCompleteTime() == null || !SafeConverter.toBoolean(group.getIsComplete())){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(group.getCompleteTime(), "yyyyMMdd"));

        ActivityGroupStatistics groupStatistics = groupStatisticsDao.loadByUidAndDay(group.getActivityId(), group.getUserId(), day);
        if(groupStatistics == null){
            groupStatistics = new ActivityGroupStatistics();
            groupStatistics.setActivityId(group.getActivityId());
            groupStatistics.setUserId(group.getUserId());
            groupStatistics.setUserName(group.getUserName());
            groupStatistics.setDay(day);
        }

        groupStatistics.setCompleteGroupCount(SafeConverter.toInt(groupStatistics.getCompleteGroupCount()) + 1);
        groupStatisticsDao.upsert(groupStatistics);
    }


    public List<ActivityGroupStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityGroupStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityGroupStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityGroupStatisticsView> future : futureList) {
            try {
                ActivityGroupStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityGroupStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityGroupStatistics> totalDataList = new ArrayList<>();
        List<ActivityGroupStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityGroupStatistics> dataList = groupStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityGroupStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        return generateDataView(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, group.getGroupName(), targetDayDataList, totalDataList);
    }

    public List<ActivityGroupStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityGroupStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityGroupStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityGroupStatisticsView> future : futureList) {
            try {
                ActivityGroupStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityGroupStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityGroupStatistics> totalDataList = new ArrayList<>();
        List<ActivityGroupStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityGroupStatistics> dataList = groupStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityGroupStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        return generateDataView(user.getId(), AgentConstants.INDICATOR_TYPE_USER, user.getRealName(), targetDayDataList, totalDataList);
    }

    private ActivityGroupStatisticsView generateDataView(Long id, Integer idType, String name, List<ActivityGroupStatistics> targetDayDataList, List<ActivityGroupStatistics> totalDataList){
        ActivityGroupStatisticsView view = new ActivityGroupStatisticsView();
        view.setId(id);
        view.setIdType(idType);
        view.setName(name);

        int dayGroupCount = 0;
        int dayCompleteGroupCount = 0;
        for(ActivityGroupStatistics statistics : targetDayDataList){
            dayGroupCount += SafeConverter.toInt(statistics.getGroupCount());
            dayCompleteGroupCount += SafeConverter.toInt(statistics.getCompleteGroupCount());
        }
        view.setDayGroupCount(dayGroupCount);
        view.setDayCompleteGroupCount(dayCompleteGroupCount);

        int totalGroupCount = 0;
        int totalCompleteGroupCount = 0;
        for(ActivityGroupStatistics statistics : totalDataList){
            totalGroupCount += SafeConverter.toInt(statistics.getGroupCount());
            totalCompleteGroupCount += SafeConverter.toInt(statistics.getCompleteGroupCount());
        }
        view.setTotalGroupCount(totalGroupCount);
        view.setTotalCompleteGroupCount(totalCompleteGroupCount);
        return view;
    }
}
