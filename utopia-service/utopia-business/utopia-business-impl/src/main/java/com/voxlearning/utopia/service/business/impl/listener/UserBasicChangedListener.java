package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.business.impl.listener.handler.TeacherTaskKtwelveMsgHandler;
import com.voxlearning.utopia.service.business.impl.listener.handler.TeacherTaskMsgHandler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * 负责监听用户的基本信息发生变化的事件
 *
 * @author zhouwei
 */
@Named
@Slf4j
@PubsubSubscriber(
    destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.basic.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.user.basic.topic"),
    }
)
public class UserBasicChangedListener implements MessageListener {

    @Inject
    private TeacherTaskMsgHandler teacherTaskMsgHandler;

    @Inject
    private TeacherTaskKtwelveMsgHandler teacherTaskKtwelveMsgHandler;

    /**
     * 每一个handle进来，请添加自己的方法，并添加try - catch
     * @param message
     * @author zhouwei
     */
    @Override
    public void onMessage(Message message) {
        teacherTaskKtwelveMsgHandler(message);
        teacherTaskMsgHandler(message);
    }

    /**
     * 请在自己的方法里面，添加try-catch，避免影响别人的handle
     * @param message
     * @author zhouwei
     */
    public void teacherTaskMsgHandler(Message message) {
        try {
            teacherTaskMsgHandler.handler(message);
        } catch (Throwable e) {
            log.error("teacherTaskMsgHandler consumer error. message: {} . e: {}", message, e);
        }
    }

    public void teacherTaskKtwelveMsgHandler(Message message) {
        try {
            teacherTaskKtwelveMsgHandler.handler(message);
        } catch (Throwable e) {
            log.error("teacherTaskKtwelveMsgHandler consumer error. message: {} . e: {}", message, e);
        }
    }

}
