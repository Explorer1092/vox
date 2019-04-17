package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
public class SS_CalculateScore extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Inject private ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;


    @Override
    public void execute(SelfStudyHomeworkContext context) {
        if (ObjectiveConfigType.ORAL_INTERVENTIONS.equals(context.getObjectiveConfigType())) {
            return;
        }
        Map<String, QuestionScoreResult> scoreResultMap = new HashMap<>();
        Map<String, List<List<String>>> standardAnswerMap = new LinkedHashMap<>();
        Map<String, List<List<Boolean>>> subGraspMap = new HashMap<>();
        Map<String, List<Double>> subScoreMap = new HashMap<>();

        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(Collections.singleton(context.getStudentHomeworkAnswer().getQuestionId()));

        StudentHomeworkAnswer answer = context.getStudentHomeworkAnswer();

        buildSomeMap(context, answer, newQuestionMap, scoreResultMap, standardAnswerMap, subGraspMap, subScoreMap);

        context.setScoreResult(scoreResultMap);
        context.setStandardAnswer(standardAnswerMap);
        context.setSubGrasp(subGraspMap);
        context.setSubScore(subScoreMap);
    }

    private void buildSomeMap(SelfStudyHomeworkContext context,
                              StudentHomeworkAnswer studentHomeworkAnswer,
                              Map<String, NewQuestion> newQuestionMap,
                              Map<String, QuestionScoreResult> scoreResultMap,
                              Map<String, List<List<String>>> standardAnswerMap,
                              Map<String, List<List<Boolean>>> subGraspMap,
                              Map<String, List<Double>> subScoreMap) {

        if (studentHomeworkAnswer.getDurationMilliseconds() == null) {
            studentHomeworkAnswer.setDurationMilliseconds(NewHomeworkConstants.DEFAULT_DURATION_MILLISECONDS);
        }
        QuestionScoreResult scoreResult = new QuestionScoreResult();
        String questionId = studentHomeworkAnswer.getQuestionId();
        NewQuestion newquestion = newQuestionMap.get(questionId);
        if (newquestion == null
                || questionContentTypeLoaderClient.isSubjective(newquestion.findSubContentTypeIds())
                || questionContentTypeLoaderClient.isOral(newquestion.findSubContentTypeIds())) {
            scoreResult.setIsRight(true);
            scoreResult.setQuestionId(questionId);

            SubScoreResult subScoreResult = new SubScoreResult();
            subScoreResult.setUserAnswer(Collections.singletonList(""));
            subScoreResult.setStandardAnswer(Collections.singletonList(""));
            subScoreResult.setIsRight(Collections.singletonList(true));
            scoreResult.setSubScoreResults(Collections.singletonList(subScoreResult));

        } else {
            if (StringUtils.isBlank(questionId) || context.getStandardScore().get(questionId) == null || CollectionUtils.isEmpty(studentHomeworkAnswer.getAnswer())) {
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR);
                return;
            }
            UserAnswerMapper uam = new UserAnswerMapper(questionId, context.getStandardScore().get(questionId), studentHomeworkAnswer.getAnswer());
            // 下面是为了输出日志用的
            uam.setUserAgent(context.getUserAgent());
            uam.setUserId(context.getUserId());
            uam.setHomeworkId(context.getHomeworkId());

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
