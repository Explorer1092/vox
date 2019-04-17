package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCouponStatistics;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.activity.ActivityOrderStatisticsView;
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
public class ActivityOrderStatisticsService {

    @Inject
    private ActivityOrderStatisticsDao activityOrderStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void orderStatistics(ActivityOrder order){
        if(order == null || StringUtils.isBlank(order.getActivityId()) || order.getUserId() == null || order.getOrderPayTime() == null){
            return;
        }

        // 重试3次
        for(int i = 0; i< 3; i++){
            MapMessage mapMessage = updateOrderStatistics(order);
            if(mapMessage.isSuccess()){
                return;
            }
            // 随机睡眠 100ms - 300ms
            try {
                int sleepTime = RandomUtils.nextInt(100, 300);
                Thread.sleep(sleepTime);
            }catch (Exception e){
            }
        }
    }

    private MapMessage updateOrderStatistics(ActivityOrder order){

        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getOrderPayTime(), "yyyyMMdd"));

        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        try{
            return builder.keyPrefix(this.getClass().getSimpleName() + "orderStatistics")
                    .keys(StringUtils.join(order.getActivityId(),"_", order.getUserId(), "_", day))
                    .callback(() -> {

                            ActivityOrderStatistics orderStatistics = activityOrderStatisticsDao.loadByUidAndDay(order.getActivityId(), order.getUserId(), day);
                            if(orderStatistics == null){
                                orderStatistics = new ActivityOrderStatistics();
                                orderStatistics.setActivityId(order.getActivityId());
                                orderStatistics.setUserId(order.getUserId());
                                orderStatistics.setUserName(order.getUserName());
                                orderStatistics.setDay(day);
                            }

                            orderStatistics.setCount(SafeConverter.toInt(orderStatistics.getCount()) + 1);
                            activityOrderStatisticsDao.upsert(orderStatistics);
                            return MapMessage.successMessage();
                    })
                    .build()
                    .execute();
        }catch (Exception e){
            return MapMessage.errorMessage();
        }
    }


    public List<ActivityOrderStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityOrderStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityOrderStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityOrderStatisticsView> future : futureList) {
            try {
                ActivityOrderStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityOrderStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityOrderStatistics> totalDataList = new ArrayList<>();
        List<ActivityOrderStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityOrderStatistics> dataList = activityOrderStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityOrderStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        ActivityOrderStatisticsView view = new ActivityOrderStatisticsView();
        view.setId(group.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_GROUP);
        view.setName(group.getGroupName());
        int dayCount = 0;
        for(ActivityOrderStatistics statistics : targetDayDataList){
            dayCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setDayOrderCount(dayCount);

        int totalCount = 0;
        for(ActivityOrderStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setTotalOrderCount(totalCount);
        return view;
    }



    public List<ActivityOrderStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityOrderStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityOrderStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityOrderStatisticsView> future : futureList) {
            try {
                ActivityOrderStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityOrderStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityOrderStatistics> totalDataList = new ArrayList<>();
        List<ActivityOrderStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityOrderStatistics> dataList = activityOrderStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityOrderStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        ActivityOrderStatisticsView view = new ActivityOrderStatisticsView();
        view.setId(user.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_USER);
        view.setName(user.getRealName());

        int dayCount = 0;
        for(ActivityOrderStatistics statistics : targetDayDataList){
            dayCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setDayOrderCount(dayCount);

        int totalCount = 0;
        for(ActivityOrderStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setTotalOrderCount(totalCount);
        return view;
    }

    public List<ActivityOrderStatistics> getOrderStatistics(String activityId, Collection<Long> userIds, Collection<Integer> days){
        return activityOrderStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
    }

}
