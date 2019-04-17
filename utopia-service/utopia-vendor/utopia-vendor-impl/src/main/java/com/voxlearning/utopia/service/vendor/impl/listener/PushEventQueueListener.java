package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.impl.service.AppMessageServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.support.PushEventType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 5/8/18
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.push.event.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.push.event.queue"
                )
        }
)
public class PushEventQueueListener extends SpringContainerSupport implements MessageListener {
    public static final String TAG = "tag";
    @Inject
    private AppMessageServiceImpl appMessageService;

    private static final String TYPE = "type";
    private static final String TEXT = "text";
    private static final String SOURCE = "source";
    private static final String USER_IDS = "userIds";
    private static final String EXT = "ext";
    private static final String SEND_TIME = "sendTime";

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (!(body instanceof String)) {
            logger.error("Unsupported message type");
            return;
        }
        if (RuntimeModeLoader.getInstance().current().le(Mode.STAGING)) {
            logger.info("push event queue listener: {}", JsonUtils.toJson(body));
        }

        Map<String, Object> info = JsonUtils.fromJson(body.toString());
        if (MapUtils.isEmpty(info)) {
            logger.error("Invalid message content,{}", JsonUtils.toJson(body));
            return;
        }

        if (!info.containsKey(TYPE)) {
            logger.error("Message type not found");
            return;
        }
        String type = info.get(TYPE).toString();
        PushEventType eventType = PushEventType.of(type);
        if (null == eventType) {
            logger.error("Unsupported event type,{}", JsonUtils.toJson(info));
            return;
        }

        switch (eventType) {
            case PARENT_REWARD_GENERATE:
                processParentRewardGenerateEvent(info);
                break;
            case PARENT_REWARD_SEND:
                processParentRewardSendEvent(info);
                break;
            case COMMON_SEND_PUSH_UIDS:
                processCommonPushEvent(info);
            default:
        }
    }

    private void processCommonPushEvent(Map<String, Object> info) {
        if (!info.containsKey(TEXT) || !info.containsKey(SOURCE) || !info.containsKey(USER_IDS)
                 || !info.containsKey(EXT)) {
            return;
        }

        AppMessageSource source = AppMessageSource.of(info.get(SOURCE).toString());
        if (source == AppMessageSource.UNKNOWN) {
            logger.error("Unknown app message source,{}", JsonUtils.toJson(info));
            return;
        }

        String text = info.get(TEXT).toString();
        List<Long> studentIds = (List<Long>) info.get(USER_IDS);
        Map<String, Object> extInfo = (Map<String, Object>) info.get(EXT);

        Long sendTime = SafeConverter.toLong(info.get(SEND_TIME));
        if (sendTime == 0) {
            appMessageService.sendAppJpushMessageByIds(text, source, studentIds, extInfo);
        }else
            appMessageService.sendAppJpushMessageByIds(text, source, studentIds, extInfo, sendTime);

    }

    @SuppressWarnings("unchecked")
    private void processParentRewardSendEvent(Map<String, Object> info) {
        if (!info.containsKey(TEXT) || !info.containsKey(SOURCE) || !info.containsKey(USER_IDS)
                || !info.containsKey(SEND_TIME) || !info.containsKey(EXT)) {
            return;
        }

        AppMessageSource source = AppMessageSource.of(info.get(SOURCE).toString());
        if (source == AppMessageSource.UNKNOWN) {
            logger.error("Unknown app message source,{}", JsonUtils.toJson(info));
            return;
        }

        String text = info.get(TEXT).toString();
        Long sendTime = SafeConverter.toLong(info.get(SEND_TIME));
        List<Long> studentIds = (List<Long>) info.get(USER_IDS);
        Map<String, Object> extInfo = (Map<String, Object>) info.get(EXT);

        appMessageService.sendAppJpushMessageByIds(text, source, studentIds, extInfo, sendTime);
    }

    @SuppressWarnings("unchecked")
    private void processParentRewardGenerateEvent(Map<String, Object> info) {
        if (!info.containsKey(TEXT) || !info.containsKey(SOURCE) || !info.containsKey(USER_IDS)
                || !info.containsKey(SEND_TIME) || !info.containsKey(EXT)) {
            return;
        }

        AppMessageSource source = AppMessageSource.of(info.get(SOURCE).toString());
        if (source == AppMessageSource.UNKNOWN) {
            logger.error("Unknown app message source,{}", JsonUtils.toJson(info));
            return;
        }

        String text = info.get(TEXT).toString();
        Long sendTime = SafeConverter.toLong(info.get(SEND_TIME));
        List<Long> parentIds = (List<Long>) info.get(USER_IDS);
        Map<String, Object> extInfo = (Map<String, Object>) info.get(EXT);
        if (extInfo.containsKey(TAG)) {
            String tag = extInfo.get("tag").toString();
            ParentMessageTag parentMessageTag = ParentMessageTag.nameOf(tag);
            if (null != parentMessageTag) {
                extInfo.put(TAG, parentMessageTag);
            }
        }

        appMessageService.sendAppJpushMessageByIds(text, source, parentIds, extInfo, sendTime);
    }
}
