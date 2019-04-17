package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.UniSoundScoreLevelHelper;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.VoxScoreLevelHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.SubScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
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
 * 计算本题得分
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_CalculateScore extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private UniSoundScoreLevelHelper uniSoundScoreLevelHelper;
    @Inject
    private DubbingLoaderClient dubbingLoaderClient;
    @Inject
    private VoxScoreLevelHelper voxScoreLevelHelper;

    @Override
    public void execute(VacationHomeworkResultContext context) {
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
        //初始化用户提交的答案的题目数据后面会用到
        context.setUserAnswerQuestionMap(newQuestionMap);
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
    private void buildSomeMap(VacationHomeworkResultContext context,
                              List<StudentHomeworkAnswer> answers,
                              Map<String, NewQuestion> newQuestionMap,
                              Map<String, QuestionScoreResult> scoreResultMap,
                              Map<String, List<List<String>>> standardAnswerMap,
                              Map<String, List<List<Boolean>>> subGraspMap,
                              Map<String, List<Double>> subScoreMap) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
        List<Map<String, Object>> uniSoundWordScoreLevels = uniSoundScoreLevelHelper.loadWordScoreLevels(studentDetail);
        List<Map<String, Object>> uniSoundSentenceScoreLevels = uniSoundScoreLevelHelper.loadSentenceScoreLevels(studentDetail);
        List<Map<String, Object>> dubbingWithScoreNormalScoreLevels = voxScoreLevelHelper.loadSentenceScoreLevels(studentDetail);
        List<Map<String, Object>> dubbingWithScoreSongScoreLevels = voxScoreLevelHelper.loadSongScoreLevels(studentDetail);

        boolean isSong = false;
        //判断趣味配音类作业形式，是否包含歌曲类型
        if (ObjectiveConfigType.DUBBING_WITH_SCORE.equals(context.getObjectiveConfigType())) {
            Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(context.getDubbingId())).get(context.getDubbingId());
            //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
            DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
            isSong = dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId());
        }
        for (StudentHomeworkAnswer studentHomeworkAnswer : answers) {
            if (studentHomeworkAnswer.getDurationMilliseconds() == null) {
                studentHomeworkAnswer.setDurationMilliseconds(NewHomeworkConstants.DEFAULT_DURATION_MILLISECONDS);
            }
            QuestionScoreResult scoreResult = new QuestionScoreResult();
            String questionId = studentHomeworkAnswer.getQuestionId();
            NewQuestion newquestion = newQuestionMap.get(questionId);

            if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType())) {
                if (studentHomeworkAnswer.getDurationMilliseconds() == null) {
                    studentHomeworkAnswer.setDurationMilliseconds(0L);
                }
                List<List<String>> userAnswer = studentHomeworkAnswer.getAnswer();
                List<List<String>> givenAnswer = new ArrayList<>();
                // 每个子题都没有作答
                if (CollectionUtils.isEmpty(userAnswer)) {
                    NewQuestionsContent newQuestionsContent = newquestion.getContent();
                    List<NewQuestionsSubContents> newQuestionsContentSubContents = newQuestionsContent.getSubContents();
                    if (CollectionUtils.isNotEmpty(newQuestionsContentSubContents)) {
                        for (NewQuestionsSubContents newQuestionsSubContents : newQuestionsContentSubContents) {
                            List<String> subAnswers = new ArrayList<>();
                            List<NewQuestionAnswer> newQuestionAnswers = newQuestionsSubContents.getAnswers();
                            if (CollectionUtils.isNotEmpty(newQuestionAnswers)) {
                                for (NewQuestionAnswer ignored : newQuestionAnswers) {
                                    subAnswers.add("");
                                }
                            }
                            givenAnswer.add(subAnswers);
                        }
                    }
                    studentHomeworkAnswer.setAnswer(givenAnswer);
                }
            }

            // TODO 对16、17题型的特殊处理，稍后改回到判分模板中 xuesong.zhang
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
                        || ObjectiveConfigType.LEVEL_READINGS.equals(context.getObjectiveConfigType())
                        || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())) {
                    String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                    if (StringUtils.isBlank(voiceScoringMode)) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getVacationHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_SCORING_MODE_IS_NULL,
                                "op", "student vacation homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_SCORING_MODE_IS_NULL);
                        return;
                    }
                    if (studentHomeworkAnswer.getVoiceEngineType() == null) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getVacationHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_ENGINE_TYPE_IS_NULL,
                                "op", "student vacation homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_ENGINE_TYPE_IS_NULL);
                        return;
                    }
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getVacationHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "op", "student vacation homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }
                    if (studentHomeworkAnswer.getOralScoreDetails().get(0) == null
                            || studentHomeworkAnswer.getOralScoreDetails().get(0).get(0) == null
                            || studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getMacScore() == null) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getVacationHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR,
                                "op", "student vacation homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR);
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
                                    // 给自然拼读的绕口令句子计算8分制等级
                                    if (ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())
                                            && context.getPracticeType() != null
                                            && NatureSpellingType.TONGUE_TWISTER.getCategoryId() == SafeConverter.toInt(context.getPracticeType().getCategoryId())) {
                                        List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = studentHomeworkAnswer.getOralScoreDetails();
                                        if (CollectionUtils.isNotEmpty(oralScoreDetails)) {
                                            NewHomeworkProcessResult.OralDetail oralDetails = oralScoreDetails.get(0).get(0);
                                            if (oralDetails != null) {
                                                List<NaturalSpellingSentence> sentences = oralDetails.getSentences();
                                                if (CollectionUtils.isNotEmpty(sentences)) {
                                                    for (NaturalSpellingSentence sentence : sentences) {
                                                        if (sentence.getStandardScore() != null) {
                                                            sentence.setStandardScoreLevel(
                                                                    AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(
                                                                            uniSoundSentenceScoreLevels,
                                                                            sentence.getStandardScore())
                                                                    ).name()
                                                            );
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
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
                                    "mod1", context.getVacationHomeworkId(),
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
                } else if (ObjectiveConfigType.ORAL_PRACTICE.equals(context.getObjectiveConfigType())) {
                    double totalScore = 0;
                    int count = 0;
                    boolean noAudio = false;
                    for (List<NewHomeworkProcessResult.OralDetail> orals : studentHomeworkAnswer.getOralScoreDetails()) {
                        for (NewHomeworkProcessResult.OralDetail oral : orals) {
                            if (StringUtils.isBlank(oral.getAudio())) noAudio = true;
                            totalScore += oral.getOralScore();
                            count++;
                        }
                    }
                    //如果音频地址为空打出日志
                    if (noAudio) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getVacationHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "mod3", JsonUtils.toJson(studentHomeworkAnswer),
                                "op", "student vacation homework result"
                        ));
                    }

                    if (count == 0) {
                        context.errorResponse("录音失败请重新录音");
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }
                    scoreResult.setTotalScore(new BigDecimal(totalScore).divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP).doubleValue());
                } else if (ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(context.getObjectiveConfigType())) {
                    String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getVacationHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "op", "student homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }
                    Integer standardScore = studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getStandardScore();
                    Boolean standar = standardScore != null && standardScore >= 3;
                    scoreResult.setIsRight(standar);
                    scoreResult.setTotalScore(SafeConverter.toDouble(standardScore));
                    scoreResult.setActualScore(SafeConverter.toDouble(standardScore));
                }
            } else if (ObjectiveConfigType.DUBBING.equals(context.getObjectiveConfigType())) {
                scoreResult.setIsRight(true);
                scoreResult.setQuestionId(questionId);

                SubScoreResult subScoreResult = new SubScoreResult();
                subScoreResult.setUserAnswer(Collections.singletonList(""));
                subScoreResult.setStandardAnswer(Collections.singletonList(""));
                subScoreResult.setIsRight(Collections.singletonList(true));
                scoreResult.setSubScoreResults(Collections.singletonList(subScoreResult));
            } else if (ObjectiveConfigType.DUBBING_WITH_SCORE.equals(context.getObjectiveConfigType())) {
                String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getUserId(),
                            "mod1", context.getVacationHomeworkId(),
                            "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                            "op", "student homework result"
                    ));
                    context.errorResponse();
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                    return;
                }
                List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = studentHomeworkAnswer.getOralScoreDetails();
                if (CollectionUtils.isEmpty(oralScoreDetails)
                        || CollectionUtils.isEmpty(oralScoreDetails.get(0))
                        || studentHomeworkAnswer.getOralScoreDetails().get(0).get(0) == null
                        ) {
                    context.errorResponse();
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                    return;
                }
                double score = SafeConverter.toDouble(studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getStandardScore());
                scoreResult.setVoiceEngineScoreType(studentHomeworkAnswer.getVoiceEngineType().getValue());
                scoreResult.setIsRight(score >= 3);
                scoreResult.setActualScore(score);
                scoreResult.setVoiceEngineScoreType("u8Sentence");

                List<Map<String, Object>> scoreLevels = isSong ? dubbingWithScoreSongScoreLevels : dubbingWithScoreNormalScoreLevels;
                AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(
                        scoreLevels,
                        score));
                scoreResult.setTotalScore(appOralScoreLevel.getScore());
                scoreResult.setAppOralScoreLevel(appOralScoreLevel);
                SubScoreResult subScoreResult = new SubScoreResult();
                subScoreResult.setUserAnswer(Collections.singletonList(""));
                subScoreResult.setStandardAnswer(Collections.singletonList(""));
                subScoreResult.setIsRight(Collections.singletonList(true));
                scoreResult.setSubScoreResults(Collections.singletonList(subScoreResult));
            }else {
                if (StringUtils.isBlank(questionId) || context.getStandardScore().get(questionId) == null || CollectionUtils.isEmpty(studentHomeworkAnswer.getAnswer())) {
                    LogCollector.info("backend-general", MiscUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", context.getUserId(),
                            "mod1", context.getVacationHomeworkId(),
                            "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR,
                            "op", "student vacation homework result"
                    ));
                    context.errorResponse();
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR);
                    return;
                }
                UserAnswerMapper uam = new UserAnswerMapper(questionId, context.getStandardScore().get(questionId), studentHomeworkAnswer.getAnswer(), true);
                // 下面是为了输出日志用的
                uam.setUserAgent(context.getUserAgent());
                uam.setUserId(context.getUserId());
                uam.setHomeworkId(context.getVacationHomeworkId());

                scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
                if (scoreResult == null) {
                    Map<String, String> info = new HashMap<>();
                    info.put("questionId", studentHomeworkAnswer.getQuestionId());
                    info.put("errorCode", ErrorCodeConstants.ERROR_CODE_QUESTION_SUBMIT_ANSWERS_ERROR);
                    info.put("userId", SafeConverter.toString(context.getUserId()));
                    info.put("studyType", StudyType.vacationHomework.name());
                    info.put("objectiveConfigType", context.getObjectiveConfigType().name());
                    if (context.getPracticeType() != null)
                        info.put("practiceId", SafeConverter.toString(context.getPracticeId()));
                    info.put("userAnswer", JsonUtils.toJson(studentHomeworkAnswer.getAnswer()));
                    info.put("standardAnswer", JsonUtils.toJson(newquestion.getAnswers()));
                    Mode mode = RuntimeMode.current();
                    info.put("env", mode.name());
                    LogCollector.info("question-error-log", info);

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

            // 口算训练的一道题只要有一个空答错了那么这道题目就不给分数
            if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType()) && !scoreResult.getIsRight()) {
                scoreResult.setTotalScore(0D);
            }
            standardAnswerMap.put(questionId, standardAnswer);
            subGraspMap.put(questionId, subGrasp);
            subScoreMap.put(questionId, subScore);
            scoreResultMap.put(questionId, scoreResult);
        }
    }
}
