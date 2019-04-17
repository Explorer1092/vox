package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkProcessResultServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;


@Named
public class COH_UpdateCorrect extends SpringContainerSupport implements CorrectHomeworkTask {

    @Inject
    private NewHomeworkProcessResultServiceImpl newHomeworkProcessResultService;
    @Inject private NewHomeworkResultServiceImpl newHomeworkResultService;

    @Override
    public void execute(CorrectHomeworkContext context) {
        boolean result;
        if (context.getType() == ObjectiveConfigType.NEW_READ_RECITE) {

            result = newHomeworkResultService.finishCorrectToAppForSubHomeworkResult(context.getHomeworkId(),
                    context.getNewHomeworkResultId(),
                    context.getType(),
                    context.getQuestionBoxId(),
                    context.getReview(),
                    context.getCorrectType(),
                    context.getCorrection(),
                    context.getTeacherMark(),
                    context.getIsBatch());

        } else {
            result = newHomeworkProcessResultService.updateCorrection(context.getProcessResultId(),
                    context.getHomeworkId(),
                    context.getQuestionId(),
                    context.getStudentId(),
                    context.getReview(),
                    context.getCorrectType(),
                    context.getCorrection(),
                    context.getTeacherMark(),
                    context.getIsBatch());
        }


        if (!Objects.equals(Boolean.TRUE, result)) {

            logger.error("correct fails, ProcessResultId:{}" + context.getProcessResultId());
            context.setSuccessful(false);
        }
    }
}
