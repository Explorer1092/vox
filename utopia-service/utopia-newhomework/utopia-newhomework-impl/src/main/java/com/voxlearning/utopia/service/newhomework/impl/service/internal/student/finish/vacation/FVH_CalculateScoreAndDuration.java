package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.Vacation.VacationCalculator;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.Vacation.VacationCalculatorManager;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_CalculateScoreAndDuration extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Inject
    private VacationCalculatorManager calculatorManager;

    @Override
    public void execute(FinishVacationHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                || ObjectiveConfigType.READING.equals(objectiveConfigType)
                || ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)
                || ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)
                || ObjectiveConfigType.NATURAL_SPELLING.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(objectiveConfigType)) {
            LinkedHashMap<String, NewHomeworkResultAppAnswer> nhraMap = context.getResult().getPractices().get(objectiveConfigType).getAppAnswers();
            double totalScore = 0d;
            Long totalDuration = 0L;
            for (NewHomeworkResultAppAnswer nhra : nhraMap.values()) {
                totalDuration += nhra.getDuration();
                totalScore += nhra.getScore();
            }
            double avgScore = new BigDecimal(totalScore).divide(new BigDecimal(nhraMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            context.setPracticeScore(avgScore);
            context.setPracticeDuration(totalDuration);
        } else if (ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
            //新语文读背、趣味配音不再计算分数
            LinkedHashMap<String, NewHomeworkResultAppAnswer> nhraMap = context.getResult().getPractices().get(objectiveConfigType).getAppAnswers();
            Long totalDuration = 0L;
            for (NewHomeworkResultAppAnswer nhra : nhraMap.values()) {
                totalDuration += nhra.getDuration();
            }
            context.setPracticeDuration(totalDuration);
        } else {
            NewHomeworkResultAnswer answer = context.getResult().getPractices().get(objectiveConfigType);
            Set<String> processIds = new HashSet<>(answer.getAnswers().values());
            // 计算分数和耗时
            VacationCalculator calculator = calculatorManager.getCalculator(objectiveConfigType);
            if (calculator != null) {
                CalculateResult result = calculator.calculate(processIds);
                if (result != null) {
                    context.setPracticeScore(result.getScore());
                    context.setPracticeDuration(result.getDuration());
                }
            }
        }
    }
}
