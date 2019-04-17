package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.mongodb.MongoBulkWriteException;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkSyllableDao;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveHomeworkSyllableCommand;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/9/19
 */
@Named
public class SaveHomeworkSyllableCommandHandler {

    @Inject private NewHomeworkSyllableDao newHomeworkSyllableDao;

    public void handle(SaveHomeworkSyllableCommand command) {
        if (command == null || CollectionUtils.isEmpty(command.getResults())) {
            return;
        }
        try {
            newHomeworkSyllableDao.$inserts(command.getResults());
        } catch (MongoBulkWriteException ignored) {

        }
    }
}
