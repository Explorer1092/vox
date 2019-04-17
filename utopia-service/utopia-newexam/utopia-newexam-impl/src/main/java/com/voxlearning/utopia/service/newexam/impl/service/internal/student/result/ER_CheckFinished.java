package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;

import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
public class ER_CheckFinished extends SpringContainerSupport implements NewExamResultTask {
    @Override
    public void execute(NewExamResultContext context) {
        int finishQuestionCount = 0;
        if (context.getNewExamResult().getAnswers() != null) {
            finishQuestionCount = context.getNewExamResult().getAnswers().size();
            if (!context.getNewExamResult().getAnswers().keySet().contains(context.getQuestionDocId())) {
                finishQuestionCount += 1;
            }
        }

        if (finishQuestionCount == context.getNewPaper().getQuestionScoreMap().size()) {
            context.setNewExamFinished(true);
        }
    }
}
