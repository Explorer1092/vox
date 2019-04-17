package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskCenterService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

/**
 * 任务中心，老师布置作业监听
 * @author deliang.che
 * @since 2018-06-04
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.junior.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.junior.topic")
        }
)
public class AgentTaskCenterTeacherHomeworkListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private AgentTaskManageService agentTaskManageService;

    @Override
    public void onMessage(Message message) {
        Map map = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            map = JsonUtils.fromJson(messageText);
        }
        if (decoded instanceof Map) map = (Map) decoded;

        if (map == null) {
            logger.error("AgentTaskCenterTeacherHomeworkListener error message {}", JsonUtils.toJson(message.decodeBody()));
            return;
        }

//        HomeworkPublishMessageType messageType = HomeworkPublishMessageType.of(SafeConverter.toString(map.get("messageType")));
        //老师ID
        String messageType1 = getMessageType(map);

        Long teacherId = getTeacherId(map);//SafeConverter.toLong(map.get("teacherId"));

        //布置作业
        if (Objects.equals(messageType1, "assign")) {
            agentTaskManageService.setSubTaskIfHomeworkForListener(teacherId);
        }
    }

    private String getMessageType(Map<String, Object> msg) {
        if (null != MapUtils.getString(msg, "messageType")) {
            return MapUtils.getString(msg, "messageType");
        }
        if (null != MapUtils.getString(msg, "event_type")) {
            return MapUtils.getString(msg, "event_type");
        }
        return null;
    }
    private Long getTeacherId(Map<String, Object> msg) {
        if (null != MapUtils.getLong(msg, "teacherId")) {
            return MapUtils.getLong(msg, "teacherId");
        }
        if (null != MapUtils.getLong(msg, "event_id")) {
            return MapUtils.getLong(msg, "event_id");
        }
        return null;
    }
}
