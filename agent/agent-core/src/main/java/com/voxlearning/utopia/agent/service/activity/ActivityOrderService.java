package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCouponDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityExtendDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityOrderDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentActivityDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCoupon;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityExtend;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrder;
import com.voxlearning.utopia.agent.persist.entity.activity.AgentActivity;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named
public class ActivityOrderService {

    @Inject
    private ActivityOrderDao activityOrderDao;
    @Inject
    private ActivityCouponDao activityCouponDao;
    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityOrderStatisticsService activityOrderStatisticsService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private ActivityOrderUserStatisticsService orderUserStatisticsService;
    @Inject
    private ActivityExtendDao extendDao;


    public void handleListenerData(String activityId, String orderId, Date orderPayTime, BigDecimal orderPayAmount, Long orderUserId, Long userId){

        if(StringUtils.isBlank(orderId) || orderUserId == null){
            return;
        }

        User platformUser = userLoaderClient.loadUser(orderUserId);
        if(platformUser == null){
            return;
        }

        ActivityOrder dbOrder = activityOrderDao.loadByOid(orderId);
        if(dbOrder != null){
            return;
        }

        ActivityCoupon activityCoupon = null;
        if(StringUtils.isBlank(activityId)){

            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
            if(userOrder == null){
                return;
            }
            // 从优惠券中获取活动ID 和市场人员ID
            if(StringUtils.isNotBlank(userOrder.getCouponRefId())){
                CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(userOrder.getCouponRefId());
                if(couponUserRef != null){
                    List<ActivityCoupon> couponList = activityCouponDao.loadByCoupon(couponUserRef.getCouponId(), couponUserRef.getUserId());
                    if(CollectionUtils.isNotEmpty(couponList)){
                        activityCoupon = couponList.get(0);
                        activityId = activityCoupon.getActivityId();
                        userId = activityCoupon.getUserId();
                    }
                }
            }
        }

        if(StringUtils.isBlank(activityId) || userId == null){
            return;
        }

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return;
        }

        ActivityExtend extend = extendDao.loadByAid(activityId);
        boolean multipleOrderFlag = extend != null && SafeConverter.toBoolean(extend.getMultipleOrderFlag());
        if(multipleOrderFlag){
            // 获取该家长在该活动下的历史订单, 如果有，则该订单算作首次下单的专员的订单
            List<ActivityOrder> histOrderList = activityOrderDao.loadByAidAndOrderUserId(activityId, orderUserId);
            // 重置userId,  
            if(CollectionUtils.isNotEmpty(histOrderList)){
                userId = histOrderList.get(0).getUserId();
            }
        }


        ActivityOrder order = new ActivityOrder();
        order.setActivityId(activityId);
        order.setOrderId(orderId);
        order.setOrderPayTime(orderPayTime == null ? new Date(): orderPayTime);
        order.setOrderPayAmount(orderPayAmount);
        order.setOrderUserId(orderUserId);
        order.setOrderUserRegTime(platformUser.getCreateTime());

        order.setUserId(userId);
        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser != null){
            order.setUserName(agentUser.getRealName());
        }

        activityOrderDao.insert(order);

        // 更新优惠券中的订单信息
        if(activityCoupon != null){
            activityCoupon.setOrderId(orderId);
            activityCouponDao.replace(activityCoupon);
        }

        // 订单统计
        AlpsThreadPool.getInstance().submit(() -> activityOrderStatisticsService.orderStatistics(order));
        // 下单的用户量统计
        AlpsThreadPool.getInstance().submit(() -> orderUserStatisticsService.orderUserStatistics(order));
    }


    public Map<String, ActivityOrder> getByOids(Collection<String> orderIds){
        return activityOrderDao.loadByOids(orderIds);
    }

    public List<ActivityOrder> getOrderList(String activityId, Long userId){
        return activityOrderDao.loadByAidAndUid(activityId, userId);
    }



}
