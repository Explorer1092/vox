package com.voxlearning.utopia.service.newhomework.impl.listener.handler;

import com.voxlearning.utopia.service.newhomework.api.entity.JournalStudentHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.JournalStudentHomeworkDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/7/25
 */
@Named
public class SaveJournalStudentHomeworkHandler {

    @Inject private JournalStudentHomeworkDao journalStudentHomeworkDao;

    public void handle(JournalStudentHomework journalStudentHomework) {
        if (journalStudentHomework == null) {
            return;
        }
        journalStudentHomeworkDao.saveJournalStudentHomework(journalStudentHomework);
    }

}
