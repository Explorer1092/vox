package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;


@Named
public class COH_LoadHomeworkResult extends SpringContainerSupport implements CorrectHomeworkTask {

    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public void execute(CorrectHomeworkContext context) {

        String homeworkId = context.getHomeworkId();
        Long studentId = context.getStudentId();

        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (Objects.isNull(newHomework)) {
            context.setSuccessful(false);
        }

        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        if (Objects.isNull(newHomeworkResult)) {
            context.setSuccessful(false);
        }
        context.setNewHomeworkResultId(newHomeworkResult.getId());

        NewHomeworkResultAnswer answer = newHomeworkResult.getPractices().getOrDefault(context.getType(), null);
        if (Objects.isNull(answer)) {
            context.setSuccessful(false);
        }
        if (context.getType() == ObjectiveConfigType.NEW_READ_RECITE) {
            if (StringUtils.isNotBlank(context.getQuestionBoxId())) {
                if (!answer.getAppAnswers().containsKey(context.getQuestionBoxId())) {
                    context.setSuccessful(false);
                }
            }
        } else {
            if (StringUtils.isNotBlank(context.getQuestionId())) {
                String processResultId = answer.processAnswers().getOrDefault(context.getQuestionId(), "");
                if (StringUtils.isBlank(processResultId)) {
                    context.setSuccessful(false);
                }
                context.setProcessResultId(processResultId);
            }
        }
        context.setNewHomework(newHomework);
    }
}
