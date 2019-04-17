package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveJournalStudentHomeworkHandler;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 用于消费journal
 *
 * @author xuesong.zhang
 * @since 2017/7/25
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.newhomework.journal.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.newhomework.journal.queue")
        },
        maxPermits = 256
)
public class JournalHomeworkQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private SaveJournalStudentHomeworkHandler saveJournalStudentHomeworkHandler;

    @Override
    public void onMessage(Message message) {
        Object decoded = message.decodeBody();
        if (decoded instanceof JournalStudentHomework) {
            CommandCounter.getInstance().increase("JournalStudentHomework");

            JournalStudentHomework command = (JournalStudentHomework) decoded;
            try {
                saveJournalStudentHomeworkHandler.handle(command);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            // return;
        }
    }
}
