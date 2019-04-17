package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
public class FSS_CalculateScoreAndDuration extends SpringContainerSupport implements FinishSelfStudyHomeworkTask {

    @Inject private CalculateScoreAndDuration calculateScoreAndDuration;

    @Override
    public void execute(FinishSelfStudyHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(objectiveConfigType)) {
            SelfStudyHomeworkResult selfStudyHomeworkResult = context.getSelfStudyHomeworkResult();
            LinkedHashMap<String, BaseHomeworkResultAppAnswer> appAnswer = selfStudyHomeworkResult.findAppAnswer(objectiveConfigType);
            long duration = appAnswer.values().stream().mapToLong(BaseHomeworkResultAppAnswer::getDuration).sum();
            double score = appAnswer.values().stream().mapToDouble(BaseHomeworkResultAppAnswer::getScore).average().orElse(0D);
            context.setPracticeScore(score);
            context.setPracticeDuration(duration);
        }else {
            Set<String> processIds = context.getProcessIds();
            CalculateResult result = calculateScoreAndDuration.calculate(processIds);
            if (result != null) {
                context.setPracticeScore(result.getScore());
                context.setPracticeDuration(result.getDuration());
            }
        }
    }
}
