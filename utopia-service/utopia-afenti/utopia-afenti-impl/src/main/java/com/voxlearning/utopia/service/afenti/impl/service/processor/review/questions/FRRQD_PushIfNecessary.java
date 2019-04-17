package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.context.PushReviewQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class FRRQD_PushIfNecessary extends SpringContainerSupport implements IAfentiTask<FetchReviewQuestionsContext> {

    @Inject
    private PushReviewQuestionProcessor pushReviewQuestionProcessor;

    @Override
    public void execute(FetchReviewQuestionsContext context) {
        List<AfentiLearningPlanPushExamHistory> list = context.getHistories();
        if (CollectionUtils.isNotEmpty(list)) {
            return;
        }
        try {
            PushReviewQuestionContext push = pushReviewQuestionProcessor.process(new PushReviewQuestionContext(context.getStudent(), context.getSubject(),
                    context.getBook().book.getId(), context.getUnitId()));
            if (push.isSuccessful()) {
                context.getHistories().addAll(push.getHistories());
            } else {
                context.errorResponse(push.getMessage());
                context.setErrorCode(push.getErrorCode());
            }
        } catch (Exception e) {
            logger.error("FRRQD_PushIfNecessary error. user:{}, book:{}, unit:{}", context.getStudent().getId(),
                    context.getBook().book.getId(), context.getUnitId(), e);
            context.setErrorCode(AfentiErrorType.DEFAULT.getCode());
            context.errorResponse(AfentiErrorType.DEFAULT.getInfo());
        }
    }
}
