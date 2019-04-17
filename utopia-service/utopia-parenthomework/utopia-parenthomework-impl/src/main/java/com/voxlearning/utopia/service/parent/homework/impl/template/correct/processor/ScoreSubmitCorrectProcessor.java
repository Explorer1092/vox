package com.voxlearning.utopia.service.parent.homework.impl.template.correct.processor;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 判题算分
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
public class ScoreSubmitCorrectProcessor implements IProcessor<CorrectContext> {

    //Local variables
    @Inject private ScoreCalculationLoaderClient scoreCalculationLoaderClient;

    //Logic
    /**
     * 判题算分
     *
     * @param c args
     * @return result
     */
    public void process(CorrectContext c) {
        c.getHomeworkProcessResults().forEach(hpr ->{
            //目前除了阅读绘本的题判对错是前后端都有其它应试类的题都需要大小写敏感caseSensitive:true表示需要大小写敏感
            UserAnswerMapper uam = new UserAnswerMapper(hpr.getQuestionId(), hpr.getScore(), hpr.getUserAnswers(), true);
            QuestionScoreResult scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
            List<List<String>> standardAnswer = new ArrayList<>();
            List<List<Boolean>> subGrasp = new ArrayList<>();
            List<Double> subScore = new ArrayList<>();
            for (SubScoreResult e : scoreResult.getSubScoreResults()) {
                if (e.getTooLong() != null && e.getTooLong()) {
                    c.setMapMessage(MapMessage.errorMessage("输入答案超长,请重新输入答案").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER));
                    return;
                }
                standardAnswer.add(e.getStandardAnswer());
                subGrasp.add(e.getIsRight());
                subScore.add(e.getScore());
            }
            hpr.setRight(scoreResult.getIsRight());
            hpr.setUserSubGrasp(subGrasp);
            hpr.setUserSubScore(subScore);
            hpr.setUserScore(scoreResult.getTotalScore());
            hpr.setAnswers(standardAnswer);
        });

    }

}
