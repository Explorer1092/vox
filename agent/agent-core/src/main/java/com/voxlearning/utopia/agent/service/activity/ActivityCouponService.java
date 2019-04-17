package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCouponDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCoupon;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.persist.entity.activity.palace.PalaceActivityRecord;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
public class ActivityCouponService {

    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityCouponDao activityCouponDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private ActivityCouponStatisticsService activityCouponStatisticsService;


    @Inject
    private UserLoaderClient userLoaderClient;

    public void handleListenerData(String activityId, String couponId, String couponName, Long couponUserId, Date couponTime, Long userId){
        if(StringUtils.isBlank(activityId) || StringUtils.isBlank(couponId) || couponUserId == null || userId == null){
            return;
        }

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return;
        }

        User platformUser = userLoaderClient.loadUser(couponUserId);
        if(platformUser == null){
            return;
        }

        List<ActivityCoupon> dataList = activityCouponDao.loadByCoupon(couponId, couponUserId);
        if(CollectionUtils.isNotEmpty(dataList)){
            return;
        }
        if(couponTime == null){
            couponTime = new Date();
        }

        ActivityCoupon item = new ActivityCoupon();

        item.setActivityId(activityId);

        item.setCouponId(couponId);
        item.setCouponName(couponName);
        item.setCouponUserId(couponUserId);
        item.setCouponTime(couponTime);
        item.setCouponUserRegTime(platformUser.getCreateTime());


        item.setUserId(userId);
        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser != null){
            item.setUserName(agentUser.getRealName());
        }
        activityCouponDao.insert(item);


        // 优惠券统计
        AlpsThreadPool.getInstance().submit(() -> activityCouponStatisticsService.couponStatistics(item));
    }


    public List<ActivityCoupon> getCouponList(String activityId, Long userId){
        return activityCouponDao.loadByAidAndUid(activityId, userId);
    }



}
