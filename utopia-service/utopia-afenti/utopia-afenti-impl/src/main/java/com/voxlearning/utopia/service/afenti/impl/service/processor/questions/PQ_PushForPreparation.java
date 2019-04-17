package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.AfentiPreviewQuestion;
import com.voxlearning.utopia.service.question.consumer.AfentiQuestionsLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;

/**
 * Created by Summer on 2017/7/12.
 */
@Named
public class PQ_PushForPreparation extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {

    @Inject private AfentiQuestionsLoaderClient afentiQuestionsLoaderClient;

    @Override
    public void execute(PushQuestionContext context) {
        if (context.getLearningType() != AfentiLearningType.preparation) {
            return;
        }
        // 取题
        List<AfentiPreviewQuestion> questionList = afentiQuestionsLoaderClient.loadPreviewQuestions(context.getBookId(), context.getUnitId(), context.getRank());
        if (CollectionUtils.isEmpty(questionList)) {
            logger.warn("Afenti preparation load question error, book {}, unit {}, rank {}", context.getBookId(), context.getUnitId(), context.getRank());
            context.setErrorCode(DEFAULT.getCode());
            context.errorResponse(DEFAULT.getInfo());
            return;
        }
        context.setPreviewQuestions(questionList);

    }
}
