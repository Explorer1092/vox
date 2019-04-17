package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.impl.loader.TikuStrategy;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by tanguohong on 2016/3/24.
 */
@Named
public class CE_LoadNewQuestion extends SpringContainerSupport implements CorrectNewExamTask {
    @Inject private TikuStrategy tikuStrategy;
    @Inject private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;

    @Override
    public void execute(CorrectNewExamContext context) {
        NewQuestion newQuestion = tikuStrategy.loadQuestionIncludeDisabled(context.getQuestionId());
        if (newQuestion == null) {
            logger.error("NewQuestion {} not found", context.getQuestionId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }
        context.setNewQuestion(newQuestion);

        List<Integer> subContentTypeIds = newQuestion.findSubContentTypeIds();
        boolean isNewOral = questionContentTypeLoaderClient.isNewOral(subContentTypeIds);
        context.setIsNewOral(isNewOral);
    }
}
