package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceDao;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.business.teacher.resource.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.business.teacher.resource.topic"),
        }
)
public class TeacherResourceListener implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.business.teacher.resource.topic")
    private MessagePublisher messagePublisherCoupon;
    @Inject
    private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    private TeachingResourceDao teachingResourceDao;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();
        try {
            Object body = message.decodeBody();
            if (body instanceof String) {
                msgMap = JsonUtils.fromJson((String) body);
            } else if (body instanceof Map) {
                msgMap = (Map) body;
            } else {
                log.warn("utopia.business.teacher.resource.topic decode message failed!", JsonUtils.toJson(message.decodeBody()));
                return;
            }

            Long retry = MapUtils.getLong(msgMap, "retry", 0L);
            if (retry > 3L) {
                log.warn("utopia.business.teacher.resource.topic failed ,msg:{}", JsonUtils.toJson(message.decodeBody()));
                return;
            }

            handle(msgMap);

        } catch (CannotAcquireLockException e) {
            try {
                msgMap.put("retry", MapUtils.getLong(msgMap, "retry", 0L) + 1);
                Thread.sleep(300);
                onMessage(message);
            } catch (Exception ignore) {
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handle(Map<String, Object> map) {
        String messageType = MapUtils.getString(map, "messageType");
        if (!Objects.equals("shareParent", messageType)) {
            return;
        }

        Long teacherId = MapUtils.getLong(map, "teacherId");
        String resourceId = MapUtils.getString(map, "resourceId");

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        TeachingResource resource = teachingResourceDao.load(resourceId);

        String firstName = (teacherDetail == null || StringUtils.isEmpty(teacherDetail.fetchRealname()))
                ? "" : teacherDetail.fetchRealname().substring(0, 1);
        String url = "/view/mobile/teacher/teaching_assistant/resourcedetail?resourceId=" + resourceId;
        String msg = String.format("来自%s老师的教育贴士《%s》，点击查看>>", firstName, resource.getName());

        AtomicCallback<MapMessage> callback = () -> {
            List<Long> studentIdList = studentLoaderClient.loadStudentIdsNotTerminal(teacherId);
            Set<Long> collect = parentLoaderClient.loadStudentParents(studentIdList).values().stream().flatMap(Collection::stream)
                    .map(i -> i.getParentUser().getId())
                    .collect(Collectors.toSet());

            for (Long parentId : collect) {
                sendParentMsg(parentId, msg, url);
            }
            return null;
        };
        AtomicCallbackBuilderFactory.getInstance()
                .<MapMessage>newBuilder()
                .keyPrefix("TeacherResourceListener:handle")
                .keys(teacherId)
                .callback(callback)
                .build()
                .execute();
    }

    private void sendParentMsg(Long parentId, String msg, String url) {
        // 系统消息
        AppMessage message = new AppMessage();
        message.setUserId(parentId);
        message.setTitle("系统消息");
        message.setContent(msg);
        message.setLinkType(1);
        message.setLinkUrl(url);
        message.setImageUrl("");
        message.setMessageType(ParentMessageType.REMINDER.getType());
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("tag", ParentMessageTag.通知.name());
        message.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

        //发送jpush
        Map<String, Object> extras = new HashMap<>();
        extras.put("url", url);
        extras.put("tag", ParentMessageTag.通知.name());
        extras.put("s", ParentAppPushType.ACTIVITY.name());
        appMessageServiceClient.sendAppJpushMessageByIds(msg, AppMessageSource.PARENT, Collections.singletonList(parentId), extras);
    }
}
