package com.voxlearning.utopia.service.feedback.impl.event.pubsub;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.impl.dao.UserFeedbackPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author mengyang.qin
 * @since 2019/3/7
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "equator.unclepei.feedback.plain.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "equator.unclepei.feedback.plain.queue")
        },
        maxPermits = 3
)


public class UnclepeiUserFeedbackEventListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(UnclepeiUserFeedbackEventListener.class.getName());

    @Inject
    private UserFeedbackPersistence userFeedbackPersistence;

    @Override
    public void onMessage(Message message) {
        try {
            Map msgMap = null;
            Object obj = message.decodeBody();
            if (obj instanceof String) {
                msgMap = JsonUtils.fromJson((String) obj);
            }
            if (obj instanceof Map) {
                msgMap = (Map) obj;
            }
            if (msgMap == null) {
                logger.warn("Unclepei decode message failed,{}", JsonUtils.toJson(message.decodeBody()));
                return;
            }

            if (!msgMap.containsKey("userId") || !msgMap.containsKey("realName") || !msgMap.containsKey("content")) {
                logger.warn("UnclepeiUserFeedbackEventListener message param lost,{}", JsonUtils.toJson(message.decodeBody()));
                return;
            }

            long userId = SafeConverter.toLong(msgMap.get("userId"));
            String realName = SafeConverter.toString(msgMap.get("realName"));
            String content = SafeConverter.toString(msgMap.get("content"));
            UserType userType = UserType.valueOf(msgMap.get("userType").toString());

            UserFeedback userFeedback = UserFeedback.newInstance(userId, content, "佩叔学英语");
            userFeedback.setUserType(userType.getType());
            userFeedback.setRealName(realName);
            userFeedbackPersistence.$upsert(userFeedback);
        } catch (Exception e) {
            logger.error("Unclepei save userfeedback message failed,{}", JsonUtils.toJson(message.decodeBody()), e);
        }
    }


}
