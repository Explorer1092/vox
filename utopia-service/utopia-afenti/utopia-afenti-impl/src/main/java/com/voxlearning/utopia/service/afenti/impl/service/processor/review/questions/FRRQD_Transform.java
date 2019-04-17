package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class FRRQD_Transform extends SpringContainerSupport implements IAfentiTask<FetchReviewQuestionsContext> {

    @Override
    public void execute(FetchReviewQuestionsContext context) {
        for (AfentiLearningPlanPushExamHistory history : context.getHistories()) {
            Map<String, Object> question = new HashMap<>();
            question.put("userId", history.getUserId().toString());
            String bookId = AfentiUtils.getBookId(history.getNewBookId(), AfentiLearningType.castle);

            question.put("bookId", bookId);
            question.put("unitId", history.getNewUnitId());
            question.put("rankId", history.getRank());
            question.put("examId", history.getExamId());
            question.put("rightNum", history.getRightNum());
            question.put("scoreCoefficient", history.getScoreCoefficient());
            question.put("subject", history.getSubject());
            context.getQuestions().add(question);
        }
    }
}
