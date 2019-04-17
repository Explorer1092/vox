package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.entity.constant.TeacherTaskCalType;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.service.business.impl.listener.handler.TeacherTaskMsgHandler;
import com.voxlearning.utopia.service.business.impl.loader.TeacherTaskLoaderImpl;
import com.voxlearning.utopia.service.business.impl.queue.BusinessQueueProducer;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherActivateTeacherServiceImpl;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.entity.constant.TeacherTaskCalType.HOMEWORK;
import static com.voxlearning.utopia.entity.constant.TeacherTaskCalType.NORMAL;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.教师激活教师等级一_产品平台;

/**
 *
 * 老师任务成长体系中，用来负责监听并消费重试事件
 *
 * @author zhouwei
 */
@Named
@Slf4j
@PubsubSubscriber(
    destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.teacher.task.retry.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.teacher.task.retry.topic"),
    },
    maxPermits = 6
)
public class TeacherTaskRetryListener implements MessageListener {

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
            teacherTaskMsgHandler.handler(message);
        } catch (Throwable e) {
            log.error("teacherTaskMsgHandler consumer error. message: {} . e: {}", message, e);
        }
    }

}
