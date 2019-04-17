package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
public class FQQ_TransformQuizQuestion extends SpringContainerSupport implements IAfentiTask<FetchQuizQuestionContext> {

    @Override
    public void execute(FetchQuizQuestionContext context) {

        for (AfentiQuizResult result : context.getResults()) {
            Map<String, Object> question = new HashMap<>();
            question.put("userId", result.getUserId().toString());
            question.put("bookId", result.getNewBookId());
            question.put("unitId", result.getNewUnitId());
            question.put("examId", result.getExamId());
            question.put("rightNum", result.getRightNum());
            question.put("errorNum", result.getErrorNum());
            question.put("subject", result.getSubject());
            context.getQuestions().add(question);
        }
    }
}
