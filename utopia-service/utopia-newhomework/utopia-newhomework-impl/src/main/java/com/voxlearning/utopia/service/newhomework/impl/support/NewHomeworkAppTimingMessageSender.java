package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.queue.JpushHomeworkQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 29/7/2016
 * 学生完成作业生成jpush定时消息,送到queue里,queue的消费在vendor里
 */
@Named
public class NewHomeworkAppTimingMessageSender extends SpringContainerSupport {

    @Inject private JpushHomeworkQueueProducer jpushHomeworkQueueProducer;

    public void sendTimingMessage(FinishHomeworkContext context) {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "HOMEWORK_FINISH"); //这个值是约定的,要改就两边都改
        info.put("userId", context.getUserId());
        info.put("subject", context.getHomework().getSubject().name());
        info.put("timestamp", Instant.now().toEpochMilli());
        info.put("homeworkId", context.getHomeworkId());
        info.put("homeworkType", context.getNewHomeworkType());

        Message message = Message.newMessage().withStringBody(JsonUtils.toJson(info));
        jpushHomeworkQueueProducer.getProducer().produce(message);
    }
}
