package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author majianxin
 */
@Named
public class OS_CalculateStandardScore extends SpringContainerSupport implements OutsideReadingResultTask {

    @Override
    public void execute(OutsideReadingContext context) {

        List<String> objectiveQuestionIds = context.getOutsideReading().findObjectiveQuestionIds(context.getMissionId());
        List<String> subjectiveQuestionIds = context.getOutsideReading().findSubjectiveQuestionIds(context.getMissionId());
        String questionId = context.getStudentHomeworkAnswer().getQuestionId();
        if (!objectiveQuestionIds.contains(questionId) && !subjectiveQuestionIds.contains(questionId)) {
            logger.error("OutsideReading {} mission {} does not contain question {}", context.getReadingId(), context.getMissionId(), questionId);
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }
        Map<String, Double> standardScore = new HashMap<>();
        if (objectiveQuestionIds.size() > 0) {
            standardScore.put(questionId, new BigDecimal(100 / objectiveQuestionIds.size()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        context.setObjectiveQuestionIds(objectiveQuestionIds);
        context.setSubjectiveQuestionIds(subjectiveQuestionIds);
        context.setStandardScore(standardScore);
    }
}
