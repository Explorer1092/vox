package com.voxlearning.utopia.service.parent.homework.impl.template.submit.batch;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.Questions;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 判题算分
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
public class HomeworkProcessResultBatchScoreProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;

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
        HomeworkResult hr = hc.getHomeworkResult();
        Set<String> questionIds = hc.getHomeworkProcessResults().stream().map(f->f.getQuestionId()).collect(Collectors.toSet());
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        hc.getHomeworkProcessResults().forEach(hpr ->{
            Questions questions = hp.getPractices().stream().filter(e->e.getType().equals(hparam.getObjectiveConfigType())).flatMap(e->e.getQuestions().stream()).filter(e->e.getQuestionId().equals(hpr.getQuestionId())).findFirst().get();

            //数学口算倒计时结束会有部分提未作答，上传上来的数据是[]，所以需要根据标准答案给用户答案赋值""来统一答案结构进行判分
            List<List<String>> userAnswer = hpr.getUserAnswers();
            List<List<String>> givenAnswer = new ArrayList<>();
            // 每个子题都没有作答
            NewQuestion newQuestion = newQuestionMap.get(hpr.getQuestionId());
            if (CollectionUtils.isEmpty(userAnswer)) {
                for(List<String> as : newQuestion.getAnswers()){
                    List<String> subAnswers =  new ArrayList<>();
                    for(String subAs : as){
                        subAnswers.add("");
                    }
                    givenAnswer.add(subAnswers);
                }
                hpr.setUserAnswers(givenAnswer);
            }
            //目前除了阅读绘本的题判对错是前后端都有其它应试类的题都需要大小写敏感caseSensitive:true表示需要大小写敏感
            UserAnswerMapper uam = new UserAnswerMapper(hpr.getQuestionId(), questions.getScore(), hpr.getUserAnswers(), !ObjectiveConfigType.READING.equals(hparam.getObjectiveConfigType()));
            QuestionScoreResult scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
            List<List<String>> standardAnswer = new ArrayList<>();
            List<List<Boolean>> subGrasp = new ArrayList<>();
            List<Double> subScore = new ArrayList<>();
            if(scoreResult == null){
                hpr.setRight(false);
                hpr.setUserSubGrasp(subGrasp);
                hpr.setUserSubScore(subScore);
                hpr.setUserScore(0d);
                hpr.setAnswers(standardAnswer);
                hpr.setScore(questions.getScore());
                return;
            }
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

        if(hr.getFinished()){//完成则统计错题数、得分
            List<HomeworkProcessResult> hprs = hc.getHomeworkProcessResults();
            hr.setErrorQuestionCount((int)hprs.stream().filter(e->!e.getRight()).count());
            hr.setUserScore(hprs.stream().mapToDouble(HomeworkProcessResult::getUserScore).sum());
            hr.setScoreLevel(HomeworkUtil.score2Level(hr.getUserScore()).name());
        }
    }

}
