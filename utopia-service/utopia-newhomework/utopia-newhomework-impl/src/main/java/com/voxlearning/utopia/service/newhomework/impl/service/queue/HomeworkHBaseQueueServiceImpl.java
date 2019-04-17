package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.service.HomeworkHBaseQueueService;
import com.voxlearning.utopia.service.newhomework.impl.queue.HomeworkHBaseQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/8/17
 */
@Named
@ExposeService(interfaceClass = HomeworkHBaseQueueService.class)
public class HomeworkHBaseQueueServiceImpl implements HomeworkHBaseQueueService {

    @Inject private HomeworkHBaseQueueProducer homeworkHBaseQueueProducer;

    @Override
    public void sendSubHomeworkResult(List<SubHomeworkResult> results) {
        for (SubHomeworkResult result : results) {
            Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(result));
            homeworkHBaseQueueProducer.getHomeworkResultHbaseProducer().produce(message);
        }

        // SaveHomeworkResultHBaseCommand command = new SaveHomeworkResultHBaseCommand();
        // command.setResults(results);
        // command.setCurrentTime(System.currentTimeMillis());
        // Message message = Message.newMessage().writeObject(command);
        // homeworkHBaseQueueProducer.getProcessHomeworkHbaseProducer().produce(message);
    }

    @Override
    public void sendSubHomeworkResultAnswer(List<SubHomeworkResultAnswer> results) {
        for (SubHomeworkResultAnswer result : results) {
            Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(result));
            homeworkHBaseQueueProducer.getHomeworkResultAnswerHbaseProducer().produce(message);
        }

        // SaveHomeworkResultAnswerHBaseCommand command = new SaveHomeworkResultAnswerHBaseCommand();
        // command.setResults(results);
        // command.setCurrentTime(System.currentTimeMillis());
        // Message message = Message.newMessage().writeObject(command);
        // homeworkHBaseQueueProducer.getProcessHomeworkHbaseProducer().produce(message);
    }

    @Override
    public void sendSubHomeworkProcessResult(List<SubHomeworkProcessResult> results) {
        for (SubHomeworkProcessResult result : results) {
            Message message = Message.newMessage().withPlainTextBody(JsonUtils.toJson(result));
            homeworkHBaseQueueProducer.getHomeworkProcessResultHbaseProducer().produce(message);
        }

        // SaveHomeworkProcessResultHBaseCommand command = new SaveHomeworkProcessResultHBaseCommand();
        // command.setResults(results);
        // command.setCurrentTime(System.currentTimeMillis());
        // Message message = Message.newMessage().writeObject(command);
        // homeworkHBaseQueueProducer.getProcessHomeworkHbaseProducer().produce(message);
    }
}
