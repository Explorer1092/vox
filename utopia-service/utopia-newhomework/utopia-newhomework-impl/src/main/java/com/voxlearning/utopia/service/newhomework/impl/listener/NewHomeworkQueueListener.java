package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveHomeworkSyllableCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveJournalNewHomeworkProcessResultCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.SaveSelfStudyWordIncreaseHomeworkCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.listener.handler.UpdateTotalAssignmentRecordCommandHandler;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkSyllableCommand;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveJournalNewHomeworkProcessResultCommand;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveSelfStudyWordIncreaseHomeworkCommand;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.UpdateTotalAssignmentRecordCommand;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @version 0.1
 * @since 2016/3/3
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.newhomework.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.newhomework.queue")
        },
        maxPermits = 256
)
public class NewHomeworkQueueListener extends SpringContainerSupport implements MessageListener {

    @Deprecated
    @Inject private NewHomeworkQueueHandle newHomeworkQueueHandle;

    @Inject private SaveJournalNewHomeworkProcessResultCommandHandler saveJournalNewHomeworkProcessResultCommandHandler;
    @Inject private UpdateTotalAssignmentRecordCommandHandler updateTotalAssignmentRecordCommandHandler;
    @Inject private SaveHomeworkSyllableCommandHandler saveHomeworkSyllableCommandHandler;
    @Inject private SaveSelfStudyWordIncreaseHomeworkCommandHandler saveSelfStudyWordIncreaseHomeworkCommandHandler;

    @Override
    public void onMessage(Message message) {
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            CommandCounter.getInstance().increase("LegacyCommand");

            // ================================================================
            // decoded is plain text, enter legacy mode.
            // keep back compatibility.
            // will be removed in the future.
            // ================================================================
            String messageText = (String) decoded;
            if (logger.isDebugEnabled()) {
                logger.debug("Message received: {}", messageText);
            }
            try {
                newHomeworkQueueHandle.processMessage(messageText);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (decoded instanceof SaveJournalNewHomeworkProcessResultCommand) {
            CommandCounter.getInstance().increase("SaveJournalNewHomeworkProcessResultCommand");

            SaveJournalNewHomeworkProcessResultCommand command = (SaveJournalNewHomeworkProcessResultCommand) decoded;
            try {
                saveJournalNewHomeworkProcessResultCommandHandler.handle(command);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        if (decoded instanceof UpdateTotalAssignmentRecordCommand) {
            CommandCounter.getInstance().increase("UpdateTotalAssignmentRecordCommand");

            UpdateTotalAssignmentRecordCommand command = (UpdateTotalAssignmentRecordCommand) decoded;
            updateTotalAssignmentRecordCommandHandler.handle(command);
            return;
        }

        if (decoded instanceof SaveHomeworkSyllableCommand) {
            CommandCounter.getInstance().increase("SaveHomeworkSyllableCommand");

            SaveHomeworkSyllableCommand command = (SaveHomeworkSyllableCommand) decoded;
            saveHomeworkSyllableCommandHandler.handle(command);
            return;
        }

        if (decoded instanceof SaveSelfStudyWordIncreaseHomeworkCommand) {
            CommandCounter.getInstance().increase("SaveSelfStudyWordIncreaseHomeworkCommand");

            SaveSelfStudyWordIncreaseHomeworkCommand command = (SaveSelfStudyWordIncreaseHomeworkCommand) decoded;
            saveSelfStudyWordIncreaseHomeworkCommandHandler.handle(command);
            return;
        }

        CommandCounter.getInstance().increase("Unrecognized");
        throw new UnsupportedOperationException("Unrecognized message received");
    }
}
