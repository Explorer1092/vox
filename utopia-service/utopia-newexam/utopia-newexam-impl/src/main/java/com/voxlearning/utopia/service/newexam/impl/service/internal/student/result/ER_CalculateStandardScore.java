package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;

import javax.inject.Named;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_CalculateStandardScore extends SpringContainerSupport implements NewExamResultTask {
    @Override
    public void execute(NewExamResultContext context) {
        Map<String, Double> questionScoreMap =  context.getQuestionScoreMap();
        if(questionScoreMap == null || questionScoreMap.isEmpty() || questionScoreMap.get(context.getQuestionId()) == null){
            logger.warn("NewPaper {} does not contain question {}", context.getNewPaper(), context.getQuestionId());
            context.errorResponse("请退出测验重新进入测验。");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }
        context.setStandardScore(questionScoreMap.get(context.getQuestionId()));
    }
}
