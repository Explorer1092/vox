package com.voxlearning.utopia.service.business.impl.listener;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.business.impl.listener.handler.TeacherTaskMsgHandler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouwei on 2018/9/5
 **/
@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.newhomework.batch.reward.integral.queue"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.newhomework.batch.reward.integral.queue"),
        }
)
public class TeacherAwardStudentListener implements MessageListener {

    @Inject
    private TeacherTaskMsgHandler teacherTaskMsgHandler;

    /**
     * 每一个handle进来，请添加自己的方法，并添加try - catch
     * @param message
     * @author zhouwei
     */
    @Override
    public void onMessage(Message message) {
        teacherTaskMsgHandler(message);
    }

    /**
     * 请在自己的方法里面，添加try-catch，避免影响别人的handle
     * @param message
     * @author zhouwei
     */
    public void teacherTaskMsgHandler(Message message) {
        try {
            Map<String, Object> msgMap;
            Object body = message.decodeBody();
            if (body instanceof String) {
                msgMap = JsonUtils.fromJson((String) body);
            } else if (body instanceof Map) {
                msgMap = (Map) body;
            } else {
                log.warn("Teacher homework reward student message decode message failed!", JsonUtils.toJson(message.decodeBody()));
                return;
            }

            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("junbao-log-utopia.newhomework.batch.reward.integral.queue:{}", JSON.toJSONString(msgMap));
            }

            Map<String, Object> msgMapNew = new HashMap<>();
            msgMapNew.put("messageType", "teacherCommentAndAward");
            msgMapNew.put("teacherId", msgMap.get("teacherId"));
            msgMapNew.put("studentId", msgMap.get("studentId"));
            Map<String, Object> homeworkLocation = (Map<String, Object>)msgMap.get("homeworkLocation");
            msgMapNew.put("homeworkId",homeworkLocation.get("id"));
            msgMapNew.put("type", "award");
            teacherTaskMsgHandler.handler(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgMapNew)));
        } catch (Throwable e) {
            log.error("teacherTaskMsgHandler consumer error. message: {} . e: {}", message, e);
        }
    }

}
