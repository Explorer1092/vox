package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author majianxin
 */
@Named
public class OS_CalculateScore extends SpringContainerSupport implements OutsideReadingResultTask {

    @Inject private ScoreCalculationLoaderClient scoreCalculationLoaderClient;

    @Override
    public void execute(OutsideReadingContext context) {
        Map<String, QuestionScoreResult> scoreResultMap = new HashMap<>();
        Map<String, List<List<String>>> standardAnswerMap = new LinkedHashMap<>();
        Map<String, List<List<Boolean>>> subGraspMap = new HashMap<>();
        Map<String, List<Double>> subScoreMap = new HashMap<>();

        buildSomeMap(context, context.getStudentHomeworkAnswer(), scoreResultMap, standardAnswerMap, subGraspMap, subScoreMap);

        context.setScoreResult(scoreResultMap);
        context.setStandardAnswer(standardAnswerMap);
        context.setSubGrasp(subGraspMap);
        context.setSubScore(subScoreMap);
    }

    private void buildSomeMap(OutsideReadingContext context,
                              StudentHomeworkAnswer studentHomeworkAnswer,
                              Map<String, QuestionScoreResult> scoreResultMap,
                              Map<String, List<List<String>>> standardAnswerMap,
                              Map<String, List<List<Boolean>>> subGraspMap,
                              Map<String, List<Double>> subScoreMap) {

        if (studentHomeworkAnswer.getDurationMilliseconds() == null) {
            studentHomeworkAnswer.setDurationMilliseconds(NewHomeworkConstants.DEFAULT_DURATION_MILLISECONDS);
        }
        QuestionScoreResult scoreResult = new QuestionScoreResult();
        String questionId = studentHomeworkAnswer.getQuestionId();
        if (context.getSubjectiveQuestionIds().contains(questionId)) {
            scoreResult.setIsRight(true);
            scoreResult.setQuestionId(questionId);
            SubScoreResult subScoreResult = new SubScoreResult();
            subScoreResult.setUserAnswer(Collections.singletonList(""));
            subScoreResult.setStandardAnswer(Collections.singletonList(""));
            subScoreResult.setIsRight(Collections.singletonList(true));
            scoreResult.setSubScoreResults(Collections.singletonList(subScoreResult));
        } else {
            UserAnswerMapper uam = new UserAnswerMapper(questionId, context.getStandardScore().get(questionId), studentHomeworkAnswer.getAnswer());
            // 下面是为了输出日志用的
            uam.setUserAgent(context.getUserAgent());
            uam.setUserId(context.getUserId());
            uam.setHomeworkId(context.getReadingId());
            scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
            if (scoreResult == null) {
                logger.error("Cannot calculate newQuestion {} score", questionId);
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CALCULATE_SCORE);
                return;
            }
        }
        List<List<String>> standardAnswer = new ArrayList<>();
        List<List<Boolean>> subGrasp = new ArrayList<>();
        List<Double> subScore = new ArrayList<>();
        scoreResult.getSubScoreResults().forEach(e -> {
            standardAnswer.add(e.getStandardAnswer());
            subGrasp.add(e.getIsRight());
            subScore.add(e.getScore());
        });
        standardAnswerMap.put(questionId, standardAnswer);
        subGraspMap.put(questionId, subGrasp);
        subScoreMap.put(questionId, subScore);
        scoreResultMap.put(questionId, scoreResult);
    }
}
