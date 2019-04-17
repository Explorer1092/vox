/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.UniSoundScoreLevelHelper;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.VoxScoreLevelHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.DubbingCategory;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
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
 *
 * @author Ruib
 * @author guohong.tan
 * @author xuesong.zhang
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class HR_CalculateScore extends SpringContainerSupport implements HomeworkResultTask {
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
    public void execute(HomeworkResultContext context) {

        if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType())) {
            return;
        }

        if (ObjectiveConfigType.OCR_DICTATION.equals(context.getObjectiveConfigType())) {
            return;
        }

        // 字词讲练作业形式中，汉字文化模块没有题目，不需要计算分数，直接return
        if (WordTeachModuleType.CHINESECHARACTERCULTURE.equals(context.getWordTeachModuleType())) {
            return;
        }

        Map<String, QuestionScoreResult> scoreResultMap = new HashMap<>();
        Map<String, List<List<String>>> standardAnswerMap = new LinkedHashMap<>(); // 标准答案
        Map<String, List<List<Boolean>>> subGraspMap = new HashMap<>(); // 作答区域的掌握情况
        Map<String, List<Double>> subScoreMap = new HashMap<>(); // 作答区域的得分情况

        List<StudentHomeworkAnswer> answers = context.getStudentHomeworkAnswers();
        List<StudentHomeworkAnswer> oralAnswers = context.getStudentHomeworkOralAnswers();

        Set<String> questionIds = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(answers)) {
            questionIds.addAll(answers.stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(oralAnswers)) {
            questionIds.addAll(oralAnswers.stream().map(StudentHomeworkAnswer::getQuestionId).collect(Collectors.toList()));
        }

        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        //初始化用户提交的答案的题目数据后面会用到
        context.setUserAnswerQuestionMap(newQuestionMap);

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
    private void buildSomeMap(HomeworkResultContext context,
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
        List<Map<String, Object>> oralCommunicationSingleLevel = voxScoreLevelHelper.loadVoxOralCommunicationSingleLevel(studentDetail);
        List<Map<String, Object>> uniSoundWOrdTeachSentenceScoreLevels = uniSoundScoreLevelHelper.loadUniSoundWordTeachSentenceScoreLevels(studentDetail);

        boolean isSong = false;
        //判断趣味配音类作业形式，是否包含歌曲类型
        if (ObjectiveConfigType.DUBBING_WITH_SCORE.equals(context.getObjectiveConfigType())) {
            Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(Collections.singleton(context.getDubbingId())).get(context.getDubbingId());
            //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
            DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
            isSong = dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId());
        }

        for (StudentHomeworkAnswer studentHomeworkAnswer : answers) {
            QuestionScoreResult scoreResult = new QuestionScoreResult();
            String questionId = studentHomeworkAnswer.getQuestionId();
            NewQuestion newquestion = newQuestionMap.get(questionId);
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
                        || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(context.getObjectiveConfigType())
                        || ObjectiveConfigType.NATURAL_SPELLING.equals(context.getObjectiveConfigType())) {
                    String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                    if (StringUtils.isBlank(voiceScoringMode)) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_SCORING_MODE_IS_NULL,
                                "op", "student homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_SCORING_MODE_IS_NULL);
                        return;
                    }
                    if (studentHomeworkAnswer.getVoiceEngineType() == null) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_ENGINE_TYPE_IS_NULL,
                                "op", "student homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_VOICE_ENGINE_TYPE_IS_NULL);
                        return;
                    }
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "op", "student homework result"
                        ));
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
                                // 添加自然拼读standardScore不能为空的校验
                                Integer categoryId = null;
                                if (context.getPracticeType() != null) {
                                    categoryId = context.getPracticeType().getCategoryId();
                                }
                                if (ObjectiveConfigType.NATURAL_SPELLING == context.getObjectiveConfigType() && standardScore == null) {
                                    LogCollector.info("backend-general", MapUtils.map(
                                            "env", RuntimeMode.getCurrentStage(),
                                            "usertoken", context.getUserId(),
                                            "mod1", context.getHomeworkId(),
                                            "mod2", ErrorCodeConstants.ERROR_CODE_ORAL_SCORE_DETAILS_ERROR,
                                            "mod3", JsonUtils.toJson(answers),
                                            "mod4", "standardScore is null",
                                            "categoryId", categoryId,
                                            "op", "student homework result"
                                    ));
                                    // 为null时先默认给个6分，都计算为3星
                                    standardScore = 6;
                                }
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
                } else if (ObjectiveConfigType.ORAL_PRACTICE.equals(context.getObjectiveConfigType()) || ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING.equals(context.getObjectiveConfigType())) {
                    double totalScore = 0;
                    int count = 0;
                    boolean noAudio = false;
                    for (List<NewHomeworkProcessResult.OralDetail> orals : studentHomeworkAnswer.getOralScoreDetails()) {
                        for (NewHomeworkProcessResult.OralDetail oral : orals) {
                            if (StringUtils.isBlank(oral.getAudio())) noAudio = true;
                            if (oral.getStandardScore() == null) {
                                LogCollector.info("backend-general", MapUtils.map(
                                        "env", RuntimeMode.getCurrentStage(),
                                        "usertoken", context.getUserId(),
                                        "mod1", context.getHomeworkId(),
                                        "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR,
                                        "mod3", JsonUtils.toJson(studentHomeworkAnswer),
                                        "op", "student homework result"
                                ));
                                context.errorResponse("打分失败请重新录音");
                                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SUBMIT_DATA_ERROR);
                                return;
                            }
                            AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(uniSoundWordScoreLevels, oral.getStandardScore()));
                            totalScore += appOralScoreLevel.getScore();
                            count++;
                            oral.setOralScoreInterval(appOralScoreLevel.name());
                        }
                    }
                    //如果音频地址为空打出日志
                    if (noAudio) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "mod3", JsonUtils.toJson(studentHomeworkAnswer),
                                "op", "student homework result"
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
                    VoiceEngineType voiceEngineType = studentHomeworkAnswer.getVoiceEngineType();
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "op", "student homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }
                    Integer standardScore = studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getStandardScore();
                    int standScore = VoiceEngineType.SingSound == voiceEngineType ? 4 : 3;
                    Boolean standard = standardScore != null && standardScore >= standScore;
                    scoreResult.setIsRight(standard);
                    scoreResult.setTotalScore(SafeConverter.toDouble(standardScore));
                    scoreResult.setActualScore(SafeConverter.toDouble(standardScore));
                } else if (ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(context.getObjectiveConfigType())) {
                    String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                    VoiceEngineType voiceEngineType = studentHomeworkAnswer.getVoiceEngineType();
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "op", "student homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }
                    int standardScore = 4;
                    if (VoiceEngineType.SingSound == voiceEngineType) {
                        standardScore = 5;
                    } else if (VoiceEngineType.ChiVox == voiceEngineType) {
                        standardScore = 6;
                    }
                    NewHomeworkProcessResult.OralDetail oralDetails = studentHomeworkAnswer.getOralScoreDetails().get(0).get(0);
                    if (oralDetails != null) {
                        List<NaturalSpellingSentence> naturalSpellingSentences = oralDetails.getSentences();
                        if (CollectionUtils.isNotEmpty(naturalSpellingSentences)) {
                            NaturalSpellingSentence naturalSpellingSentence = naturalSpellingSentences.get(0);
                            if (naturalSpellingSentence != null && CollectionUtils.isNotEmpty(naturalSpellingSentence.getWords())) {
                                NaturalSpellingSentence.Word word = naturalSpellingSentence.getWords().get(0);

                                Double wordScore = word != null ? word.getScore() : 0D;
                                scoreResult.setIsRight(wordScore >= standardScore);
                                scoreResult.setTotalScore(SafeConverter.toDouble(wordScore));
                                scoreResult.setActualScore(SafeConverter.toDouble(wordScore));
                            }
                        }
                    }
                } else if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(context.getObjectiveConfigType())) {
                    String voiceScoringMode = studentHomeworkAnswer.getVoiceScoringMode();
                    if (voiceScoringMode.equals("Normal") && CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY,
                                "op", "student homework result"
                        ));
                        context.errorResponse();
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VOICE_IS_EMPTY);
                        return;
                    }
                    Integer totalScore = studentHomeworkAnswer.getOralScoreDetails().get(0).stream().mapToInt(BaseHomeworkProcessResult.OralDetail::getStandardScore).sum();
                    Integer standardScore = new BigDecimal(totalScore).divide(new BigDecimal(studentHomeworkAnswer.getOralScoreDetails().get(0).size()), 2, BigDecimal.ROUND_DOWN).intValue();
                    AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(NewHomeworkUtils.processScoreLevel(
                            uniSoundWOrdTeachSentenceScoreLevels,
                            standardScore));
                    scoreResult.setTotalScore(appOralScoreLevel.getScore());
                    scoreResult.setAppOralScoreLevel(appOralScoreLevel);
                    scoreResult.setActualScore(SafeConverter.toDouble(standardScore));
                } else if (ObjectiveConfigType.ORAL_COMMUNICATION.equals(context.getObjectiveConfigType())) {
                    if (studentHomeworkAnswer == null || CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails()) ||
                            CollectionUtils.isEmpty(studentHomeworkAnswer.getOralScoreDetails().stream().filter(CollectionUtils::isNotEmpty).collect(Collectors.toList()))) {
                        logger.error("HR_CalculateScore_oral_communication studentHomeworkAnswer : {} "
                                , JsonUtils.toJson(studentHomeworkAnswer));
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getUserId(),
                                "mod1", context.getHomeworkId(),
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
                    questionId = studentHomeworkAnswer.getDialogId();
                    double actualScore = SafeConverter.toDouble(studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getMacScore());
                    scoreResult.setActualScore(actualScore);
                    scoreResult.setVoiceEngineScoreType("Vox17");
                    String level = NewHomeworkUtils.getLevelByStar(oralCommunicationSingleLevel, SafeConverter.toInt(studentHomeworkAnswer.getOralScoreDetails().get(0).get(0).getStar()));
                    AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(level);
                    scoreResult.setTotalScore(appOralScoreLevel.getScore());
                    scoreResult.setAppOralScoreLevel(appOralScoreLevel);
                    subScoreResult.setUserAnswer(Collections.singletonList(""));
                    subScoreResult.setStandardAnswer(Collections.singletonList(""));
                    subScoreResult.setIsRight(Collections.singletonList(true));
                    Map<String, String> additions = Maps.newLinkedHashMap();
                    additions.put("dialogId", studentHomeworkAnswer.getDialogId());
                    scoreResult.setAdditions(additions);
                    scoreResult.setSubScoreResults(Collections.singletonList(subScoreResult));
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
                            "mod1", context.getHomeworkId(),
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
            } else {
                //数学口算倒计时结束会有部分提未作答，上传上来的数据是[]，所以需要根据标准答案给用户答案赋值""来统一答案结构进行判分
                if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(context.getObjectiveConfigType())) {
                    List<List<String>> userAnswer = studentHomeworkAnswer.getAnswer();
                    List<List<String>> givenAnswer = new ArrayList<>();
                    // 每个子题都没有作答
                    if (CollectionUtils.isEmpty(userAnswer)) {
                        for (List<String> as : newquestion.getAnswers()) {
                            List<String> subAnswers = new ArrayList<>();
                            for (String subAs : as) {
                                subAnswers.add("");
                            }
                            givenAnswer.add(subAnswers);
                        }
                        studentHomeworkAnswer.setAnswer(givenAnswer);
                    }
                }
                if (StringUtils.isBlank(questionId) || context.getStandardScore().get(questionId) == null || CollectionUtils.isEmpty(studentHomeworkAnswer.getAnswer())) {
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
                //目前除了阅读绘本的题判对错是前后端都有其它应试类的题都需要大小写敏感caseSensitive:true表示需要大小写敏感
                UserAnswerMapper uam = new UserAnswerMapper(questionId, context.getStandardScore().get(questionId), studentHomeworkAnswer.getAnswer(), !ObjectiveConfigType.READING.equals(context.getObjectiveConfigType()));
                // 下面是为了输出日志用的
                uam.setUserAgent(context.getUserAgent());
                uam.setUserId(context.getUserId());
                uam.setHomeworkId(context.getHomeworkId());

                scoreResult = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
                if (scoreResult == null) {
                    Map<String, String> info = new HashMap<>();
                    info.put("questionId", studentHomeworkAnswer.getQuestionId());
                    info.put("errorCode", ErrorCodeConstants.ERROR_CODE_QUESTION_SUBMIT_ANSWERS_ERROR);
                    info.put("userId", SafeConverter.toString(context.getUserId()));
                    info.put("studyType", StudyType.homework.name());
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
            for (SubScoreResult e : scoreResult.getSubScoreResults()) {
                if (e.getTooLong() != null && e.getTooLong()) {
                    context.errorResponse("输入答案超长,请重新输入答案");
                    context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
                    return;
                }
                standardAnswer.add(e.getStandardAnswer());
                subGrasp.add(e.getIsRight());
                subScore.add(e.getScore());
            }

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
