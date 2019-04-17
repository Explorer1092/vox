package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/19
 */
@Named
public class FTQQ_TransformQuizQuestion extends SpringContainerSupport implements IAfentiTask<FetchTermQuizQuestionContext> {

    @Override
    public void execute(FetchTermQuizQuestionContext context) {
        
        for (AfentiQuizResult result : context.getQrs()) {
            Map<String, Object> question = new HashMap<>();
            question.put("examId", result.getExamId());
            question.put("rightNum", result.getRightNum());
            question.put("errorNum", result.getErrorNum());
            question.put("subject", result.getSubject());
            question.put("userId", result.getUserId().toString());
            question.put("bookId", result.getNewBookId());
            question.put("unitId", result.getNewUnitId());
            context.getQuestions().add(question);
        }
    }
}
