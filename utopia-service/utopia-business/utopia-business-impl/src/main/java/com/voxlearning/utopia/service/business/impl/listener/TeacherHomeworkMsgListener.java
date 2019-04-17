package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.business.impl.listener.handler.TeacherTaskMsgHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 老师作业消息监听
 */
@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.business.activity.scholarship.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.retry.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.junior.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.business.activity.scholarship.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.retry.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.junior.topic")
        },
        maxPermits = 4
)
public class TeacherHomeworkMsgListener extends SpringContainerSupport implements MessageListener{

    /** 处理作业消息的handler列表 **/
    private List<TeacherHomeworkMsgHandler> handlers;

    @Inject
    private TeacherTaskMsgHandler teacherTaskMsgHandler;

    @Override
    public void afterPropertiesSet(){
        handlers = new ArrayList<>();
        handlers.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(
                getApplicationContext(),
                TeacherHomeworkMsgHandler.class,
                false,
                true)
                .values());
    }


    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap;

        Object body = message.decodeBody();
        if (body instanceof String)
            msgMap = JsonUtils.fromJson((String) body);
        else if (body instanceof Map)
            msgMap = (Map) body;
        else {
            logger.warn("Teacher homework message decode message failed!", JsonUtils.toJson(message.decodeBody()));
            return;
        }
        teacherTaskMsgHandler(message);
        handlers.forEach(h -> {
            try{
                h.handle(msgMap);
            }catch (Throwable t){
                logger.error("Business teacher homework msg listener error!",t);
            }
        });
    }

    /**
     * 老师任务体系处理
     * @param message
     * @author zhouwei
     */
    private void teacherTaskMsgHandler(Message message) {
        try {
            teacherTaskMsgHandler.handler(message);
        } catch (Throwable e) {
            log.error("teacherTaskMsgHandler consumer error. message: {} . e: {}", message, e);
        }
    }
}
