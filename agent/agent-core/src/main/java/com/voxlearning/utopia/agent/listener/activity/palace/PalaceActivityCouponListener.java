package com.voxlearning.utopia.agent.listener.activity.palace;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Map;

@Named
//@PubsubSubscriber(
//        destinations = {
//                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.parent.17xue.studyCourse.sendCoupon.topic"),
//                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.parent.17xue.studyCourse.sendCoupon.topic"),
//        }
//)
public class PalaceActivityCouponListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private PalaceActivityService palaceActivityService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            dataMap = JsonUtils.fromJson(messageText);
        }else if (decoded instanceof Map)
            dataMap = (Map) decoded;

        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        String activityId = SafeConverter.toString(dataMap.get("activityId"), "");
        String couponId =  SafeConverter.toString(dataMap.get("couponId"), "");
        String couponName = SafeConverter.toString(dataMap.get("couponName"), "");
        Long parentId = SafeConverter.toLong(dataMap.get("parentId"));
        Long userId = SafeConverter.toLong(dataMap.get("clerkId"));
        long couponTime = SafeConverter.toLong(dataMap.get("time"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(couponTime);
        palaceActivityService.resolveCouponData(activityId, couponId, couponName, parentId, calendar.getTime(), userId);
    }
}
