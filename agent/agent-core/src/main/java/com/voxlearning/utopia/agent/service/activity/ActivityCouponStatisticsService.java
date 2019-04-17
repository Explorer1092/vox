package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCouponStatisticsDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCoupon;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCouponStatistics;
import com.voxlearning.utopia.agent.persist.entity.activity.palace.PalaceActivityUserStatistics;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.activity.ActivityCouponStatisticsView;
import com.voxlearning.utopia.agent.view.activity.palace.PalaceDataView;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ActivityCouponStatisticsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ActivityCouponStatisticsDao activityCouponStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;

    public void couponStatistics(ActivityCoupon coupon){
        if(coupon == null || StringUtils.isBlank(coupon.getActivityId()) || coupon.getUserId() == null || coupon.getCouponTime() == null){
            return;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(coupon.getCouponTime(), "yyyyMMdd"));

        ActivityCouponStatistics couponStatistics = activityCouponStatisticsDao.loadByUidAndDay(coupon.getActivityId(), coupon.getUserId(), day);
        if(couponStatistics == null){
            couponStatistics = new ActivityCouponStatistics();
            couponStatistics.setActivityId(coupon.getActivityId());
            couponStatistics.setUserId(coupon.getUserId());
            couponStatistics.setUserName(coupon.getUserName());
            couponStatistics.setDay(day);
        }

        couponStatistics.setCount(SafeConverter.toInt(couponStatistics.getCount()) + 1);
        activityCouponStatisticsDao.upsert(couponStatistics);
    }


    public List<ActivityCouponStatisticsView> getGroupDataView(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<ActivityCouponStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<ActivityCouponStatisticsView>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupDataView(activityId, groupId, days)));
        }

        for(Future<ActivityCouponStatisticsView> future : futureList) {
            try {
                ActivityCouponStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public ActivityCouponStatisticsView getGroupDataView(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<ActivityCouponStatistics> totalDataList = new ArrayList<>();
        List<ActivityCouponStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                List<ActivityCouponStatistics> dataList = activityCouponStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                    List<ActivityCouponStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(tempDataList)){
                        targetDayDataList.addAll(tempDataList);
                    }
                }
            }
        }

        ActivityCouponStatisticsView view = new ActivityCouponStatisticsView();
        view.setId(group.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_GROUP);
        view.setName(group.getGroupName());
        int dayCount = 0;
        for(ActivityCouponStatistics statistics : targetDayDataList){
            dayCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setDayCouponCount(dayCount);

        int totalCount = 0;
        for(ActivityCouponStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setTotalCouponCount(totalCount);
        return view;
    }



    public List<ActivityCouponStatisticsView> getUserDataView(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<ActivityCouponStatisticsView> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<ActivityCouponStatisticsView>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserDataView(activityId, userId, days)));
        }
        for(Future<ActivityCouponStatisticsView> future : futureList) {
            try {
                ActivityCouponStatisticsView item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }


    public ActivityCouponStatisticsView getUserDataView(String activityId, Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<ActivityCouponStatistics> totalDataList = new ArrayList<>();
        List<ActivityCouponStatistics> targetDayDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){
            Integer targetDay = days.stream().max(Comparator.comparing(Function.identity())).get();
            List<ActivityCouponStatistics> dataList = activityCouponStatisticsDao.loadByUsersAndDays(activityId, Collections.singleton(user.getId()), days);
            if(CollectionUtils.isNotEmpty(dataList)){
                totalDataList.addAll(dataList);
                List<ActivityCouponStatistics> tempDataList = dataList.stream().filter(k -> Objects.equals(k.getDay(), targetDay)).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempDataList)){
                    targetDayDataList.addAll(tempDataList);
                }
            }
        }

        ActivityCouponStatisticsView view = new ActivityCouponStatisticsView();
        view.setId(user.getId());
        view.setIdType(AgentConstants.INDICATOR_TYPE_USER);
        view.setName(user.getRealName());

        int dayCount = 0;
        for(ActivityCouponStatistics statistics : targetDayDataList){
            dayCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setDayCouponCount(dayCount);

        int totalCount = 0;
        for(ActivityCouponStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getCount());
        }
        view.setTotalCouponCount(totalCount);
        return view;
    }

    public List<ActivityCouponStatistics> getCouponStatistics(String activityId, Collection<Long> userIds, Collection<Integer> days){
        return activityCouponStatisticsDao.loadByUsersAndDays(activityId, userIds, days);
    }
}
