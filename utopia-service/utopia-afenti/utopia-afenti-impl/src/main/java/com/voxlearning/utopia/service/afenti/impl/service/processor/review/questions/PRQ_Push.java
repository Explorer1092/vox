package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.PushReviewQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.AfentiQuestionsLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.NO_QUESTION;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class PRQ_Push extends SpringContainerSupport implements IAfentiTask<PushReviewQuestionContext> {
    @Inject
    private AfentiQuestionsLoaderClient afentiQuestionsLoaderClient;

    @Override
    public void execute(PushReviewQuestionContext context) {

        Map<String, List<NewQuestion>> questionMap = afentiQuestionsLoaderClient.loadReviewQuestions(context.getBookId(), Collections.singletonList(context.getUnitId()));

        if (questionMap == null || questionMap.isEmpty() || CollectionUtils.isEmpty(questionMap.get(context.getUnitId()))
                || questionMap.get(context.getUnitId()).size() < 3) {
            logger.error("PRQ_Push: data is not enough. bookId :" + context.getBookId() + ", unitId:" + context.getUnitId() + ", questionMap:" + questionMap);
            context.setErrorCode(NO_QUESTION.getCode());
            context.errorResponse(NO_QUESTION.getInfo());
            return;
        }

        context.setReviewQuestions(questionMap.get(context.getUnitId()));
    }
}
