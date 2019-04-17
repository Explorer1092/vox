package com.voxlearning.utopia.service.piclisten.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.galaxy.service.activity.api.constants.assistgroup.AssistType;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.impl.service.PiclistenKillNamiActivityServiceImpl;
import com.voxlearning.utopia.service.piclisten.impl.support.VendorPushQueueSupporter;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2018-08-23 下午12:31
 **/
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "galaxy.activity.assistgroup.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "galaxy.activity.assistgroup.topic")
})
public class AssistGroupActivitySubscriberListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Inject
//    private PiclistenKillNamiCouponExpireRemindDelayMessageSupport piclistenKillNamiCouponExpireRemindDelayMessageSupport;
//
//    @Inject
//    private PiclistenKillNamiCouponExpireDelayMessageSupport piclistenKillNamiCouponExpireDelayMessageSupport;

    @Inject
    private CouponServiceClient couponServiceClient;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;


    @Inject
    private VendorPushQueueSupporter vendorPushQueueSupporter;

    @Inject
    private PiclistenKillNamiActivityServiceImpl piclistenKillNamiActivityService;


    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = null;
        Object object = message.decodeBody();
        if (object instanceof String) {
            msgMap = JsonUtils.fromJson((String) object);
        }
        if (msgMap == null) {
            return;
        }
        String type = SafeConverter.toString(msgMap.get("assistType"));
        AssistType assistType = AssistType.valueOf(type);
        long userId = SafeConverter.toLong(msgMap.get("userId"));
        String targetId = SafeConverter.toString(msgMap.get("targetId"));
        long successTime = SafeConverter.toLong(msgMap.get("successTime"));
        String  assistId = SafeConverter.toString(msgMap.get("assistId"));


        if (assistType == AssistType.PICLISTEN_201809){
//
//            MapMessage mapMessage = piclistenKillNamiActivityService.checkProductIdCouldAssist(targetId);
//            if (!mapMessage.isSuccess()){
//                logger.error("点读机助力活动，助力产品 id 错误： " + mapMessage.getInfo());
//                return;
//            }
//            String couponId = piclistenKillNamiActivityService.chooseCouponId(mapMessage);
//            if (StringUtils.isBlank(couponId)){
//                logger.error("点读机助力活动，选择优惠券失败！");
//                return;
//            }
//            // 发送优惠券
//            MapMessage sendCoupon = couponServiceClient.sendCoupon(couponId, userId);
//            if (!sendCoupon.isSuccess()){
//                logger.error("点读机助力活动，发送优惠券失败！：" + sendCoupon.getInfo());
//                return;
//            }
//            // 助力成功发送助力成功 push
//            sendPush(userId, "恭喜你已成功获得好友助力，请在24小时之内购买，过期需要重新发起助力。", "/view/mobile/parent/reading/index.vpage");

//            long couponExpireTime = (successTime / 1000) + PiclistenKillNamiActivity.couponExpireTime;
//            // 写入 delayMessage 优惠券后期前一个小时发 push 提醒
//            Long time = couponExpireTime - PiclistenKillNamiActivity.expireRemindTime;
//            piclistenKillNamiCouponExpireRemindDelayMessageSupport.pushDelayMessage(time, assistId);
//
//            // 写入 delayMessage 24小时候，如果这个商品还未购买，则把助力过期掉
//            piclistenKillNamiCouponExpireDelayMessageSupport.pushDelayMessage(couponExpireTime, assistId);
        }

    }

//    private String chooseCouponId(MapMessage mapMessage) {
//        try {
////            OrderProduct product = OrderProduct.class.cast(mapMessage.get("product"));
////            List<OrderProductItem> itemList = (List<OrderProductItem>) mapMessage.get("items");
//            Map<String, NewBookProfile> bookProfileMap = (Map<String, NewBookProfile>) mapMessage.get("bookMap");
//            if (bookProfileMap.size() == 1){
//                NewBookProfile bookProfile = bookProfileMap.values().stream().findFirst().orElse(null);
//                Subject subject = Subject.fromSubjectId(bookProfile.getSubjectId());
//                if (subject == Subject.CHINESE){
//                    return PiclistenKillNamiActivity.couponIdMap.get("CHINESE");
//                }else if (subject == Subject.ENGLISH){
//                    return PiclistenKillNamiActivity.couponIdMap.get("ENGLISH");
//                }
//            }else {
//                return PiclistenKillNamiActivity.couponIdMap.get("PACKAGE");
//            }
//            return "";
//        }catch (Exception e){
//            logger.error("chooseCouponId error ", e);
//            return "";
//        }
//    }


    private void sendPush(Long parentId, String text, String url ) {

        Map<String, Object> messageExtInfo = new HashMap<>();
        messageExtInfo.put("studentId", "");
        messageExtInfo.put("senderName", "");
        messageExtInfo.put("tag", ParentMessageTag.通知.name());
        messageExtInfo.put("type", ParentMessageType.REMINDER.name());

        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(parentId);
        appMessage.setMessageType(ParentMessageType.REMINDER.getType());
        appMessage.setTitle("提醒");
        appMessage.setContent(text);
        appMessage.setExtInfo(messageExtInfo);
        appMessage.setLinkUrl(url);
        appMessage.setLinkType(1);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);

        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("url", url);
        jpushExtInfo.put("tag", ParentMessageTag.通知.name());
        jpushExtInfo.put("shareType", "NO_SHARE_VIEW");
        jpushExtInfo.put("shareContent", "");
        jpushExtInfo.put("shareUrl", "");
        jpushExtInfo.put("s", ParentAppPushType.ACTIVITY.name());
        vendorPushQueueSupporter.sendAppJpushMessageByIds(text, "PARENT", Collections.singletonList(parentId), jpushExtInfo);

    }
}
