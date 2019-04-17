package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.UniSoundScoreLevelHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ScoreCalculationLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_CalculateScore extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UniSoundScoreLevelHelper uniSoundScoreLevelHelper;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {
        Map<String, QuestionScoreResult> scoreResultMap = new HashMap<>();
        Map<String, List<List<String>>> standardAnswerMap = new LinkedHashMap<>(); // 标准答案
        Map<String, List<List<Boolean>>> subGraspMap = new HashMap<>(); // 作答区域的掌握情况
        Map<String, List<Double>> subScoreMap = new HashMap<>(); // 作答区域的得分情况

        Set<String> questionIds = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            questionIds.addAll(context.getStudentHomeworkAnswers().stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkOralAnswers())) {
            questionIds.addAll(context.getStudentHomeworkOralAnswers().stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toList()));
        }

        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);

        List<StudentHomeworkAnswer> answers = context.getStudentHomeworkAnswers();
        List<StudentHomeworkAnswer> oralAnswers = context.getStudentHomeworkOralAnswers();
        if (CollectionUtils.isNotEmpty(answers)) {
            buildSomeMap(context, answers, newQuestionMap, scoreResultMap, standardAnswerMap, subGraspMap, subScoreMap);
        }
        if (CollectionUtils.isNotEmpty(oralAnswers)) {
            buildSomeMap(context, oralAnswers, newQuestionMap, scoreResultMap, standardAnswerMap, subGraspMap, subScoreMap);
        }


        context.setScoreResult(scoreResultMap);
        context.setStandardAnswer(standardAnswerMap);
        context.setSubGrasp(subGraspMap);
        context.setSubScore(subScoreMap);
    }

    /**
     * 构建分数Map，标准答案Map，作答区域掌握Map，作答区域得分Map
     */
    private void buildSomeMap(LiveCastHomeworkResultContext context,
                              List<StudentHomeworkAnswer> answers,
                              Map<String, NewQuestion> newQuestionMap,
                              Map<String, QuestionScoreResult> scoreResultMap,
                              Map<String, List<List<String>>> standardAnswerMap,
                              Map<String, List<List<Boolean>>> subGraspMap,
                              Map<String, List<Double>> subScoreMap) {

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
        List<Map<String, Object>> uniSoundWordScoreLevels = uniSoundScoreLevelHelper.loadWordScoreLevels(studentDetail);
        List<Map<String, Object>> uniSoundSentenceScoreLevels = uniSoundScoreLevelHelper.loadSentenceScoreLevels(studentDetail);

        for (StudentHomeworkAnswer studentHomeworkAnswer : answers) {
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

                if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())
                        || ObjectiveConfigType.READING.equals(context.getObjectiveConfigType())
                        || ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())) {
                    String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                    if (StringUtils.isBlank(voiceScoringMode)) {
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_SCORING_MODE_IS_NULL);
                        return;
                    }
                    if (studentHomeworkAnswer.getVoiceEngineType() == null) {
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_ENGINE_TYPE_IS_NULL);
                        return;
                    }
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }

                    double score = studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getMacScore().doubleValue();
                    Integer sentenceType = studentHomeworkAnswer.getSentenceType();
                    //听读模式全部为D级别
                    if ("Normal".equals(voiceScoringMode) || "ListenOnly".equals(voiceScoringMode)) {
                        if (VoiceEngineType.ChiVox.equals(studentHomeworkAnswer.getVoiceEngineType())) {
                            AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(ChiVoxScoreLevel.processLevel(score).name());
                            scoreResult.setAppOralScoreLevel(appOralScoreLevel);
                            scoreResult.setTotalScore(appOralScoreLevel.getScore());
                            scoreResult.setVoiceEngineScoreType("chiVox");
                        } else {
                            AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(UnisoundScoreLevel.processLevel(score).name());
                            scoreResult.setVoiceEngineScoreType("unisound");
                            if (sentenceType != null) {
                                Integer standardScore = studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getStandardScore();
                                score = standardScore == null ? 0 : standardScore.doubleValue();
                                appOralScoreLevel = AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(uniSoundWordScoreLevels, score));
                                scoreResult.setVoiceEngineScoreType("u8Word");
                                if (sentenceType == 0) {
                                    appOralScoreLevel = AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(uniSoundSentenceScoreLevels, score));
                                    scoreResult.setVoiceEngineScoreType("u8Sentence");
                                } else if (sentenceType == 10) {
                                    appOralScoreLevel = AppOralScoreLevel.A;
                                    scoreResult.setVoiceEngineScoreType("zhidu");
                                }
                            }
                            scoreResult.setAppOralScoreLevel(appOralScoreLevel);
                            scoreResult.setTotalScore(appOralScoreLevel.getScore());
                        }
                    } else {
                        scoreResult.setAppOralScoreLevel(AppOralScoreLevel.D);
                        scoreResult.setTotalScore(AppOralScoreLevel.D.getScore());
                        scoreResult.setVoiceEngineScoreType("skip");
                    }
                    // 如果是新绘本阅读，跟读题得分还要做一次特殊处理
                    // 上面的代码已经将引擎分转为等级分，用等级分乘标准分再除100
                    if (ObjectiveConfigType.LEVEL_READINGS == context.getObjectiveConfigType()) {
                        Double totalScore = scoreResult.getTotalScore();
                        Double standScore = context.getStandardScore().get(questionId);
                        if (standScore == null) {
                            LogCollector.info("backend-general", MapUtils.map(
                                    "env", RuntimeMode.getCurrentStage(),
                                    "usertoken", context.getUserId(),
                                    "mod1", context.getHomeworkId(),
                                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR,
                                    "op", "student homework result"
                            ));
                            context.errorResponse();
                            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR);
                            return;
                        }
                        scoreResult.setTotalScore(new BigDecimal(totalScore * standScore).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                    }
                    scoreResult.setActualScore(score);
                }
            } else if (ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())) {
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
}
