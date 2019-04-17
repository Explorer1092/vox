package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.impl.loader.TikuStrategy;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamPaperHelper;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
public class CE_CalculateStandardScore extends SpringContainerSupport implements CorrectNewExamTask {
    @Inject private TikuStrategy tikuStrategy;

    @Override
    public void execute(CorrectNewExamContext context) {
        NewExamResult newExamResult = context.getNewExamResultMap().values().iterator().next();
        String paperDocId = NewExamPaperHelper.fetchPaperId(newExamResult, context.getNewExam(), newExamResult.getUserId());
        NewPaper newPaper = tikuStrategy.loadPaperByDocid(paperDocId);
        if (newPaper == null) {
            logger.error("NewPaper is null paperId {}", paperDocId);
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PAPER_NOT_EXIST);
            return;
        }
        Map<String, Double> questionScoreMap = newPaper.getQuestionScoreMapByQid();
        if (questionScoreMap == null || questionScoreMap.isEmpty() || questionScoreMap.get(context.getNewQuestion().getId()) == null) {
            logger.error("NewPaper {} does not contain question {}", newPaper, context.getQuestionId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_QUESTION_NOT_EXIST);
            return;
        }
        context.setStandardScore(questionScoreMap.get(context.getQuestionId()));

        if (context.getIsNewOral()) {
            int subContentSize = context.getNewQuestion().getContent().getSubContents().size();
            Double subStandardScore = new BigDecimal(context.getStandardScore()).divide(new BigDecimal(subContentSize), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            context.setSubStandardScore(subStandardScore);
        }
    }
}
