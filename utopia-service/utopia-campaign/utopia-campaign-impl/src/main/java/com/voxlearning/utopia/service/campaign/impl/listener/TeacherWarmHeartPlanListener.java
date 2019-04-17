package com.voxlearning.utopia.service.campaign.impl.listener;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.campaign.impl.service.WarmHeartPlanServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsEnum;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.WarmHeartPlanConstant.*;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.assign.warm.heart.plan.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.assign.warm.heart.plan.topic")
        }
)
public class TeacherWarmHeartPlanListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.assign.warm.heart.plan.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private CacheExistsUtils cacheExistsUtils;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private WarmHeartPlanServiceImpl warmHeartPlanService;

    private static final String studentMsg = "【好消息】你的老师已开启《家校共育》计划，和爸妈每天做一些小事，成为最幸福的成长之星！点击参加>>";
    private static final String parentMsg = "【好消息】老师已开启《家校共育》计划。每天陪孩子做些小事，坚持21天，和孩子一起成为更好的自己！点击查看>>";

    @Override
    @SuppressWarnings("ALL")
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("utopia.assign.warm.heart.plan.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        handler(msgMap);
    }

    private void handler(Map<String, Object> msg) {
        try {
            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("utopia.assign.warm.heart.plan.topic msg:{}", JSON.toJSONString(msg));
            }

            Long teacherId = MapUtils.getLong(msg, "teacherId");

            warmHeartPlanService.sendTeacherResourceMsg(teacherId,
                    "【家长会教学助力包】奖励",
                    "恭喜您获得【家长会教学助力包】，点击领取>> 请在电脑端下载",
                    RESOURCE_PATH + "5c9b7cc8cd1010493b5a91db");

            List<Long> studentIds = warmHeartPlanService.loadStudentsByTeacherId(teacherId);

            if (CollectionUtils.isEmpty(studentIds)) {
                return;
            }

            Map<Long, List<StudentParent>> studentParents = parentLoaderClient.loadStudentParents(studentIds);

            for (Long sid : studentIds) {
                sendStudentMsg(sid, studentMsg);

                List<StudentParent> parentList = studentParents.get(sid);

                // 没绑定家长给学生的手机发短信
                if (CollectionUtils.isEmpty(parentList)) {
                    boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.WARM_HEART_PARENT_NOTICE_ED, sid);
                    if (noExists) {
                        cacheExistsUtils.set(CacheExistsEnum.WARM_HEART_PARENT_NOTICE_ED, sid);
                        String mobile = sensitiveUserDataServiceClient.loadUserMobile(sid);
                        if (StringUtils.isNotBlank(mobile)) {
                            SmsMessage smsMessage = new SmsMessage();
                            smsMessage.setMobile(mobile);
                            smsMessage.setType(SmsType.PARENT_NEWTERM_PLAN_NOTIFY.name());
                            smsMessage.setSmsContent(parentMsg);
                            smsServiceClient.getSmsService().sendSms(smsMessage);
                        }
                    }
                } else {
                    for (StudentParent studentParent : parentList) {
                        Long parentId = studentParent.getParentUser().getId();
                        boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.PARENT_NOTICE_ED, parentId);
                        if (noExists) {
                            cacheExistsUtils.set(CacheExistsEnum.PARENT_NOTICE_ED, parentId);
                            sendParentMsg(parentId, parentMsg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendStudentMsg(Long studentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(studentId);
        message.setTitle(WARM_HEART_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(STUDENT_INDEX_PAGE);
        message.setImageUrl("");
        message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        // push
        Map<String, Object> extInfo = MapUtils.map(
                "s", StudentAppPushType.ACTIVITY_REMIND.getType(),
                "link", STUDENT_INDEX_PAGE,
                "t", "h5",
                "key", "j");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
    }

    private void sendParentMsg(Long parentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(parentId);
        message.setTitle(WARM_HEART_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(PARENT_INDEX_PAGE);
        message.setImageUrl("");
        message.setMessageType(ParentMessageType.REMINDER.getType());
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("tag", ParentMessageTag.通知.name());
        message.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", PARENT_INDEX_PAGE);
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.ACTIVITY.name());
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras);
    }
}
