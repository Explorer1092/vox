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
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.campaign.api.constant.PlanConstant;
import com.voxlearning.utopia.service.campaign.impl.service.TeacherWinterPlanServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsEnum;
import com.voxlearning.utopia.service.campaign.impl.support.CacheExistsUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.campaign.api.constant.PlanConstant.PARENT_ACTIVITY_INDEX;
import static com.voxlearning.utopia.service.campaign.api.constant.PlanConstant.STUDENT_ACTIVITY_INDEX;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.assign.new.term.plan.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.assign.new.term.plan.topic")
        }
)
public class TeacherNewTermPlanListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.assign.new.term.plan.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;


    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private CacheExistsUtils cacheExistsUtils;
    @Inject
    private TeacherWinterPlanServiceImpl teacherWinterPlanService;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private VendorLoaderClient vendorLoaderClient;

    private static final String STU_CONTENT = "亲爱的同学，老师发起《新学期快速收心计划活动》，设定新学期目标，坚持打卡21天，赢取学豆，成为超级行动派哦！赶快来参与吧！";
    private static final String PARENT_CONTENT = "尊敬的家长，老师发起《新学期快速收心计划活动》，辅助孩子设定新学期目标，坚持打卡21天，培养受益终身的好习惯，赶快行动吧！";

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
            logger.warn("utopia.assign.new.term.plan.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        handler(msgMap);
    }

    private void handler(Map<String, Object> msg) {
        try {
            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("utopia.assign.new.term.plan.topic msg:{}", JSON.toJSONString(msg));
            }

            Long teacherId = MapUtils.getLong(msg, "teacherId");

            List<User> users = studentLoaderClient.loadTeacherStudents(teacherId);
            if (CollectionUtils.isEmpty(users)) {
                return;
            }
            Set<Long> studentIdSet = users.stream().map(LongIdEntity::getId).collect(Collectors.toSet());
            Map<Long, List<StudentParent>> studentParents = parentLoaderClient.loadStudentParents(studentIdSet);

            for (Long sid : studentIdSet) {
                sendStudentMsg(sid, STU_CONTENT);

                List<StudentParent> parentList = studentParents.get(sid);

                // 没绑定家长给学生的手机发短信
                if (CollectionUtils.isEmpty(parentList)) {
                    boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.PARENT_NOTICE_ED, sid);
                    if (noExists) {
                        cacheExistsUtils.set(CacheExistsEnum.PARENT_NOTICE_ED, sid);
                        String mobile = sensitiveUserDataServiceClient.loadUserMobile(sid);
                        if (StringUtils.isNotBlank(mobile)) {
                            SmsMessage smsMessage = new SmsMessage();
                            smsMessage.setMobile(mobile);
                            smsMessage.setType(SmsType.PARENT_NEWTERM_PLAN_NOTIFY.name());
                            smsMessage.setSmsContent(PARENT_CONTENT);
                            smsServiceClient.getSmsService().sendSms(smsMessage);
                        }
                    }
                } else {
                    // 是否有人使用家长通 有的话就给那个人发 push
                    for (StudentParent studentParent : parentList) {
                        Long parentId = studentParent.getParentUser().getId();
                        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Parent", parentId);
                        if (vendorAppsUserRef != null) {
                            boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.PARENT_NOTICE_ED, parentId);
                            if (noExists) {
                                cacheExistsUtils.set(CacheExistsEnum.PARENT_NOTICE_ED, parentId);
                                sendParentMsg(parentId, PARENT_CONTENT);
                            }
                            break; // 找到人后跳出循环
                        }
                    }

                    // 改为 7 天后没布置计划才发短信
                    /*if (sendSms) {
                        // 找关键家长,如果没有就随便取
                        StudentParent studentParent = parentList.stream()
                                .filter(StudentParent::isKeyParent)
                                .findFirst()
                                .orElse(parentList.get(0));

                        boolean noExists = cacheExistsUtils.noExists(CacheExistsEnum.PARENT_NOTICE_ED, studentParent.getParentUser().getId());
                        if (noExists) {
                            cacheExistsUtils.set(CacheExistsEnum.PARENT_NOTICE_ED, studentParent.getParentUser().getId());
                            String mobile = sensitiveUserDataServiceClient.loadUserMobile(studentParent.getParentUser().getId());
                            if (StringUtils.isNotBlank(mobile)) {
                                SmsMessage smsMessage = new SmsMessage();
                                smsMessage.setMobile(mobile);
                                smsMessage.setType(SmsType.PARENT_NEWTERM_PLAN_NOTIFY.name());
                                smsMessage.setSmsContent(PARENT_CONTENT);
                                smsServiceClient.getSmsService().sendSms(smsMessage);
                            }
                        }
                    }*/
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
        message.setTitle(PlanConstant.NEW_TERM_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(STUDENT_ACTIVITY_INDEX);
        message.setImageUrl("");
        message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        // push
        Map<String, Object> extInfo = MapUtils.map(
                "s", StudentAppPushType.ACTIVITY_REMIND.getType(),
                "link", STUDENT_ACTIVITY_INDEX,
                "t", "h5",
                "key", "j");
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
    }

    private void sendParentMsg(Long parentId, String msg) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(parentId);
        message.setTitle(PlanConstant.NEW_TERM_PLAN_ACTIVITY_NAME);
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(PARENT_ACTIVITY_INDEX);
        message.setImageUrl("");
        message.setMessageType(ParentMessageType.REMINDER.getType());
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("tag", ParentMessageTag.通知.name());
        message.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", PARENT_ACTIVITY_INDEX);
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.ACTIVITY.name());
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras);
    }
}
