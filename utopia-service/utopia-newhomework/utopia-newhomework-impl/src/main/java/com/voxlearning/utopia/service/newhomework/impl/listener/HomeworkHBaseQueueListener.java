package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveHomeworkProcessResultCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveHomeworkResultAnswerCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveHomeworkResultCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkProcessResultHBaseCommand;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkResultAnswerHBaseCommand;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkResultHBaseCommand;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/8/15
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.homework.hbase.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.homework.hbase.queue")
        },
        maxPermits = 256
)
public class HomeworkHBaseQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private SaveHomeworkResultCommandHandler saveHomeworkResultCommandHandler;
    @Inject private SaveHomeworkResultAnswerCommandHandler saveHomeworkResultAnswerCommandHandler;
    @Inject private SaveHomeworkProcessResultCommandHandler saveHomeworkProcessResultCommandHandler;

    @Override
    public void onMessage(Message message) {

        Object decoded = message.decodeBody();
        if (decoded instanceof SaveHomeworkResultHBaseCommand) {
            CommandCounter.getInstance().increase("SaveHomeworkResultHBaseCommand");
            SaveHomeworkResultHBaseCommand command = (SaveHomeworkResultHBaseCommand) decoded;
            try {
                saveHomeworkResultCommandHandler.handle(command);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        if (decoded instanceof SaveHomeworkResultAnswerHBaseCommand) {
            CommandCounter.getInstance().increase("SaveHomeworkResultAnswerHBaseCommand");
            SaveHomeworkResultAnswerHBaseCommand command = (SaveHomeworkResultAnswerHBaseCommand) decoded;
            try {
                saveHomeworkResultAnswerCommandHandler.handle(command);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        if (decoded instanceof SaveHomeworkProcessResultHBaseCommand) {
            CommandCounter.getInstance().increase("SaveHomeworkProcessResultHBaseCommand");
            SaveHomeworkProcessResultHBaseCommand command = (SaveHomeworkProcessResultHBaseCommand) decoded;
            try {
                saveHomeworkProcessResultCommandHandler.handle(command);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        CommandCounter.getInstance().increase("Unrecognized");
        throw new UnsupportedOperationException("Unrecognized message received");
    }
}
