package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.NewHomeworkResultUpdateFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.NewHomeworkResultUpdateTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2016/10/27
 */
@Named
public class HR_CheckBasicAppPracticeFinish extends SpringContainerSupport implements HomeworkResultTask {

    @Inject private NewHomeworkResultUpdateFactory newHomeworkResultUpdateFactory;

    @Override
    public void execute(HomeworkResultContext context) {
        if ((ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.KEY_POINTS.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.NEW_READ_RECITE.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(context.getObjectiveConfigType())
                || ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(context.getObjectiveConfigType()))
                && context.getIsOneByOne()) {
            NewHomeworkResultUpdateTemplate template = newHomeworkResultUpdateFactory.getTemplate(context.getObjectiveConfigType().getNewHomeworkContentProcessTemp());
            template.checkNewHomeworkAppFinish(context);
        }
    }
}
