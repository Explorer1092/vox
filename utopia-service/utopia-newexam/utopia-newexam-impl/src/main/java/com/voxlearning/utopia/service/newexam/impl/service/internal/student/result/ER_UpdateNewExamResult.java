package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
public class ER_UpdateNewExamResult extends SpringContainerSupport implements NewExamResultTask {
    @Inject private NewExamResultDao newExamResultDao;

    @Override
    public void execute(NewExamResultContext context) {

        context.setNewExamResult(newExamResultDao.doNewExam(context.getNewExamResult().getId(),
                context.getQuestionDocId(),
                context.getCurrentProcessResult().getId(),
                context.getTotalScore(),
                context.getTotalDureation(),
                context.isNewExamFinished()));


    }
}
