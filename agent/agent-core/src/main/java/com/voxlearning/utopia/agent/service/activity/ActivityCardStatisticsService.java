package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCardStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCard;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.activity.ActivityCardStatisticsView;
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
public class ActivityCardStatisticsService {

    @Inject
    private ActivityCardStatisticsDao cardStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void cardStatistics(ActivityCard card){
        if(card == null || StringUtils.isBlank(card.getActivityId()) || card.getUserId() == null || card.getCardTime() == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(card.getCardTime(), "yyyyMMdd"));

        ActivityCardStatistics cardStatistics = cardStatisticsDao.loadByUidAndDay(card.getActivityId(), card.getUserId(), day);
        if(cardStatistics == null){
            cardStatistics = new ActivityCardStatistics();
            cardStatistics.setActivityId(card.getActivityId());
            cardStatistics.setUserId(card.getUserId());
            cardStatistics.setUserName(card.getUserName());
            cardStatistics.setDay(day);
        }

        cardStatistics.setCount(SafeConverter.toInt(cardStatistics.getCount()) + 1);
        cardStatisticsDao.upsert(cardStatistics);
    }

    public void cardUsedStatistics(ActivityCard card){
        if(card == null || StringUtils.isBlank(card.getActivityId()) || card.getUserId() == null || card.getCardTime() == null){
            return;
        }
        if(!SafeConverter.toBoolean(card.getIsUsed())){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(card.getCardTime(), "yyyyMMdd"));

        ActivityCardStatistics cardStatistics = cardStatisticsDao.loadByUidAndDay(card.getActivityId(), card.getUserId(), day);
        if(cardStatistics == null){
            cardStatistics = new ActivityCardStatistics();
            cardStatistics.setActivityId(card.getActivityId());
            cardStatistics.setUserId(card.getUserId());
            cardStatistics.setUserName(card.getUserName());
            cardStatistics.setDay(day);
        }
        cardStatistics.setUsedCount(SafeConverter.toInt(cardStatistics.getUsedCount()) + 1);
        cardStatisticsDao.upsert(cardStatistics);
    }


    public List<ActivityCardStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityCardStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityCardStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityCardStatisticsView> future : futureList) {
            try {
                ActivityCardStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityCardStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityCardStatistics> totalDataList = new ArrayList<>();
        List<ActivityCardStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityCardStatistics> dataList = cardStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityCardStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        return generateDataView(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, group.getGroupName(), targetDayDataList, totalDataList);
    }

    public List<ActivityCardStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityCardStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityCardStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityCardStatisticsView> future : futureList) {
            try {
                ActivityCardStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityCardStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityCardStatistics> totalDataList = new ArrayList<>();
        List<ActivityCardStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityCardStatistics> dataList = cardStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityCardStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        return generateDataView(user.getId(), AgentConstants.INDICATOR_TYPE_USER, user.getRealName(), targetDayDataList, totalDataList);
    }

    private ActivityCardStatisticsView generateDataView(Long id, Integer idType, String name, List<ActivityCardStatistics> targetDayDataList, List<ActivityCardStatistics> totalDataList){
        ActivityCardStatisticsView view = new ActivityCardStatisticsView();
        view.setId(id);
        view.setIdType(idType);
        view.setName(name);

        int dayCardCount = 0;
        int dayUsedCount = 0;
        for(ActivityCardStatistics statistics : targetDayDataList){
            dayCardCount += SafeConverter.toInt(statistics.getCount());
            dayUsedCount += SafeConverter.toInt(statistics.getUsedCount());
        }
        view.setDayCardCount(dayCardCount);
        view.setDayUsedCount(dayUsedCount);

        int totalCardCount = 0;
        int totalUsedCount = 0;
        for(ActivityCardStatistics statistics : totalDataList){
            totalCardCount += SafeConverter.toInt(statistics.getCount());
            totalUsedCount += SafeConverter.toInt(statistics.getUsedCount());
        }
        view.setTotalCardCount(totalCardCount);
        view.setTotalUsedCount(totalUsedCount);
        return view;
    }

    public List<ActivityCardStatistics> getCardStatistics(String activityId, Collection<Long> userIds, Collection<Integer> days){
        return cardStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
    }
}
