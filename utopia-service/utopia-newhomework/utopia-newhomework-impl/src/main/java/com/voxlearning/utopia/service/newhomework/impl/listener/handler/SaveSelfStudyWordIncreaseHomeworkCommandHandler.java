package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.mongodb.MongoBulkWriteException;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.SelfStudyHomeworkGenerateServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.SaveSelfStudyWordIncreaseHomeworkCommand;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/9/27
 */
@Named
public class SaveSelfStudyWordIncreaseHomeworkCommandHandler {

    @Inject private SelfStudyHomeworkGenerateServiceImpl selfStudyHomeworkGenerateService;

    public void handle(SaveSelfStudyWordIncreaseHomeworkCommand command) {
        if (command == null || command.getClazzGroupId() == null || command.getStudentId() == null || MapUtils.isEmpty(command.getBookToKpMap())) {
            return;
        }
        try {
            selfStudyHomeworkGenerateService.generateWordsIncreaseHomework(command.getClazzGroupId(), command.getStudentId(), command.getBookToKpMap());
        } catch (MongoBulkWriteException ignored) {
        }
    }
}
