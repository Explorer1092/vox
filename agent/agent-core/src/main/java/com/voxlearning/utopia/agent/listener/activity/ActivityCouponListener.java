package com.voxlearning.utopia.agent.listener.activity;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.service.activity.ActivityCouponService;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.parent.17xue.studyCourse.sendCoupon.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.parent.17xue.studyCourse.sendCoupon.topic"),
        }
)
public class ActivityCouponListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private ActivityCouponService activityCouponService;
    @Inject
    private PalaceActivityService palaceActivityService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "agent_activity_coupon_message");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);


        String activityId = SafeConverter.toString(dataMap.get("activityId"), "");
        String couponId =  SafeConverter.toString(dataMap.get("couponId"), "");
        String couponName = SafeConverter.toString(dataMap.get("couponName"), "");
        Long parentId = SafeConverter.toLong(dataMap.get("parentId"));
        Long userId = SafeConverter.toLong(dataMap.get("clerkId"));
        long couponTime = SafeConverter.toLong(dataMap.get("time"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(couponTime);



        // 故宫立春课程的优惠券处理  立春测试环境活动ID:5c45ab63ca7f5e06c06481a7   线上ID:5c488a9329f665381f1d4f5d
        if(StringUtils.isNotBlank(activityId) && (Objects.equals(activityId, "5c45ab63ca7f5e06c06481a7") || Objects.equals(activityId, "5c488a9329f665381f1d4f5d"))){
            palaceActivityService.resolveCouponData(activityId, couponId, couponName, parentId, calendar.getTime(), userId);
        }else{
            // 通用的优惠券处理
            activityCouponService.handleListenerData(activityId, couponId, couponName, parentId, calendar.getTime(), userId);
        }

    }
}
