package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderUserStatisticsDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityExtendDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderUserStatistics;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityExtend;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.activity.ActivityOrderUserStatisticsView;
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
public class ActivityOrderUserStatisticsService {

    @Inject
    private ActivityOrderUserStatisticsDao orderUserStatisticsDao;
    @Inject
    private ActivityOrderDao orderDao;
    @Inject
    private ActivityExtendDao extendDao;
    @Inject
    private AgentActivityDao activityDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void orderUserStatistics(ActivityOrder order){

        if(order == null || StringUtils.isBlank(order.getActivityId()) || order.getUserId() == null || order.getOrderPayTime() == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getOrderPayTime(), "yyyyMMdd"));
        ActivityOrderUserStatistics userStatistics = orderUserStatisticsDao.loadByUidAndDay(order.getActivityId(), order.getUserId(), day);

        if(userStatistics == null){
            userStatistics = new ActivityOrderUserStatistics();
            userStatistics.setActivityId(order.getActivityId());
            userStatistics.setUserId(order.getUserId());
            userStatistics.setUserName(order.getUserName());
            userStatistics.setDay(day);
        }

        ActivityExtend extend = extendDao.loadByAid(order.getActivityId());
        boolean multipleOrderFlag = extend != null && SafeConverter.toBoolean(extend.getMultipleOrderFlag());
        // 一个用户可以下多个但的情况
        if(multipleOrderFlag){
            AgentActivity agentActivity = activityDao.load(order.getActivityId());
            Date startDate = null;
            Date endDate = null;
            if(agentActivity != null){
                startDate = agentActivity.getStartDate();
                endDate = agentActivity.getEndDate();
            }
            List<ActivityOrder> orderList = orderDao.loadByActivityAndUserAndTime(order.getActivityId(), Collections.singleton(order.getUserId()), startDate, endDate);
            orderList = orderList.stream().filter(p -> Objects.equals(p.getOrderUserId(), order.getOrderUserId())).collect(Collectors.toList());

            DayRange dayRange = DayRange.newInstance(order.getOrderPayTime().getTime());
            Date today = dayRange.getStartDate();

            long dayOrderCount = orderList.stream().filter(t -> t.getOrderPayTime().after(today)).count();
            if(dayOrderCount == 1){
                userStatistics.setOrderUserCount(SafeConverter.toInt(userStatistics.getOrderUserCount()) + 1);
            }

            long preOrderCount = orderList.stream().filter(t -> t.getOrderPayTime().before(today)).count();
            if(preOrderCount == 0 && dayOrderCount == 1){
                userStatistics.setFirstOrderUserCount(SafeConverter.toInt(userStatistics.getFirstOrderUserCount()) + 1);
            }
        }else {
            // 该活动一个用户只可以下一个订单
            userStatistics.setFirstOrderUserCount(SafeConverter.toInt(userStatistics.getFirstOrderUserCount()) + 1);
            userStatistics.setOrderUserCount(SafeConverter.toInt(userStatistics.getOrderUserCount()) + 1);
        }
        orderUserStatisticsDao.upsert(userStatistics);
    }



    public List<ActivityOrderUserStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityOrderUserStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityOrderUserStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityOrderUserStatisticsView> future : futureList) {
            try {
                ActivityOrderUserStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityOrderUserStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityOrderUserStatistics> totalDataList = new ArrayList<>();
        List<ActivityOrderUserStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityOrderUserStatistics> dataList = orderUserStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityOrderUserStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        ActivityOrderUserStatisticsView view = new ActivityOrderUserStatisticsView();
        view.setId(group.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_GROUP);
        view.setName(group.getGroupName());
        int dayCount = 0;
        for(ActivityOrderUserStatistics statistics : targetDayDataList){
            dayCount += SafeConverter.toInt(statistics.getFirstOrderUserCount());
        }
        view.setDayOrderUserCount(dayCount);

        int totalCount = 0;
        for(ActivityOrderUserStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getFirstOrderUserCount());
        }
        view.setTotalOrderUserCount(totalCount);
        return view;
    }



    public List<ActivityOrderUserStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityOrderUserStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityOrderUserStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityOrderUserStatisticsView> future : futureList) {
            try {
                ActivityOrderUserStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityOrderUserStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityOrderUserStatistics> totalDataList = new ArrayList<>();
        List<ActivityOrderUserStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityOrderUserStatistics> dataList = orderUserStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityOrderUserStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        ActivityOrderUserStatisticsView view = new ActivityOrderUserStatisticsView();
        view.setId(user.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_USER);
        view.setName(user.getRealName());

        int dayCount = 0;
        for(ActivityOrderUserStatistics statistics : targetDayDataList){
            dayCount += SafeConverter.toInt(statistics.getFirstOrderUserCount());
        }
        view.setDayOrderUserCount(dayCount);

        int totalCount = 0;
        for(ActivityOrderUserStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getFirstOrderUserCount());
        }
        view.setTotalOrderUserCount(totalCount);
        return view;
    }
}
