package com.voxlearning.utopia.service.parent.homework.impl.template.submit.single;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.Questions;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * 判题算分
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
public class HomeworkProcessResultScoreProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private ScoreCalculationLoaderClient scoreCalculationLoaderClient;

    //Logic
    /**
     * 判题算分
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkPractice hp = hc.getHomeworkPractice();
        HomeworkParam hparam = hc.getHomeworkParam();

        hc.getHomeworkProcessResults().forEach(hpr ->{
            Questions questions = hp.getPractices().stream().filter(e->e.getType().equals(hparam.getObjectiveConfigType())).flatMap(e->e.getQuestions().stream()).filter(e->e.getQuestionId().equals(hpr.getQuestionId())).findFirst().get();
            //目前除了阅读绘本的题判对错是前后端都有其它应试类的题都需要大小写敏感caseSensitive:true表示需要大小写敏感
            UserAnswerMapper uam = new UserAnswerMapper(hpr.getQuestionId(), questions.getScore(), hpr.getUserAnswers(), !ObjectiveConfigType.READING.equals(hparam.getObjectiveConfigType()));
            QuestionScoreResult scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
            List<List<String>> standardAnswer = new ArrayList<>();
            List<List<Boolean>> subGrasp = new ArrayList<>();
            List<Double> subScore = new ArrayList<>();
            for (SubScoreResult e : scoreResult.getSubScoreResults()) {
                if (e.getTooLong() != null && e.getTooLong()) {
                    hc.setMapMessage(MapMessage.errorMessage("输入答案超长,请重新输入答案").setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER));
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
            hpr.setScore(questions.getScore());
        });

    }

}
