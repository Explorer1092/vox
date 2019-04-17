package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.support.ImageTextRhymeStarCalculator;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/12/4 下午9:43
 */
@Named
public class NewHomeworkResultUpdate_WordTeachAndPractice extends NewHomeworkResultUpdateTemplate {

    @Inject
    private ImageTextRhymeStarCalculator imageTextRhymeStarCalculator;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.WORD_TEACH_AND_PRACTICE;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();
        WordTeachModuleType wordTeachModuleType = context.getWordTeachModuleType();
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        LinkedHashMap<String, String> courses = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processResultMap)) {
            for (NewHomeworkProcessResult npr : processResultMap.values()) {
                if (wordTeachModuleType == WordTeachModuleType.CHINESECHARACTERCULTURE) {
                    courses.put(npr.getCourseId(), npr.getId());
                } else {
                    answers.put(npr.getQuestionId(), npr.getId());
                }
            }
        }

        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        nhraa.setStoneId(context.getStoneId());
        if (WordTeachModuleType.WORDEXERCISE.equals(wordTeachModuleType)) {
            nhraa.setAnswers(answers);
        } else if (WordTeachModuleType.IMAGETEXTRHYME.equals(wordTeachModuleType)) {
            nhraa.setImageTextRhymeAnswers(answers);
        } else if (WordTeachModuleType.CHINESECHARACTERCULTURE.equals(wordTeachModuleType)) {
            nhraa.setChineseCourses(courses);
        }

        String key = context.getStoneId();
        NewHomeworkResult newHomeworkResult = newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                nhraa);
        context.setNewHomeworkResult(newHomeworkResult);
        context.setIsOneByOne(true);
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {
        NewHomework newHomework = context.getHomework();
        NewHomeworkResult newHomeworkResult = context.getNewHomeworkResult();
        if (newHomeworkResult == null) {
            return;
        }
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();
        String stoneId = context.getStoneId();
        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByStoneDataId(stoneId, objectiveConfigType, null);
        if (CollectionUtils.isEmpty(processIds)) {
            return;
        }
        List<NewHomeworkApp> newHomeworkApps = context.getHomework().findNewHomeworkApps(objectiveConfigType);
        NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
        for (NewHomeworkApp app : newHomeworkApps) {
            if (app.getStoneDataId().equals(stoneId)) {
                newHomeworkApp = app;
                break;
            }
        }

        boolean containsWordExercise = CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions());
        boolean containsImageTextRhyme = CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions());
        boolean containsChineseCharacterCulture = CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds());
        // 模块数量
        int moduleNum = 0;
        if (containsWordExercise) {
            moduleNum ++;
        }
        if (containsImageTextRhyme) {
            moduleNum ++;
        }
        if (containsChineseCharacterCulture) {
            moduleNum ++;
        }
        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        Map<String, Integer> chapterScoreMap = new HashMap<>();
        Map<String, Double> imageTextRhymeQuestionScoreMap = new HashMap<>();
        Long duration = 0L;
        Double totalScore;
        Double wordExerciseScore = 0D;
        Double imageTextRhymeQuestionScore = 0D;
        Double finalImageTextRhymeScore = 0D;
        NewHomeworkResultAppAnswer appAnswer = newHomeworkResult
                .getPractices()
                .get(objectiveConfigType)
                .getAppAnswers()
                .get(stoneId);

        // 只有汉字文化模块
        if (moduleNum == 1 && containsChineseCharacterCulture) {
            if (newHomeworkApp.getChineseCharacterCultureCourseIds().size() == processIds.size()) {
                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                    duration += npr.getDuration();
                }
                totalScore = 100.00;
                newHomeworkResultService.finishHomeworkWordTeachAndPractice(
                        newHomework.toLocation(),
                        context.getUserId(),
                        objectiveConfigType,
                        stoneId,
                        totalScore,
                        duration,
                        wordExerciseScore,
                        finalImageTextRhymeScore);
            }
        } else {
            // 计算分数&答题时长
            for (NewHomeworkProcessResult npr : processResultMap.values()) {
                duration += npr.getDuration();
                if (npr.getWordTeachModuleType().equals(WordTeachModuleType.WORDEXERCISE)) {
                    wordExerciseScore += SafeConverter.toDouble(npr.getScore());
                }
                if (npr.getWordTeachModuleType().equals(WordTeachModuleType.IMAGETEXTRHYME)) {
                    imageTextRhymeQuestionScoreMap.put(npr.getQuestionId(), npr.getActualScore());
                }
            }
            // 图文入韵部分分数计算特殊处理(图文入韵模块基准分60)
            if (containsImageTextRhyme) {
                for (ImageTextRhymeHomework imageTextRhymeHomework : newHomeworkApp.getImageTextRhymeQuestions()) {
                    for (NewHomeworkQuestion question : imageTextRhymeHomework.getChapterQuestions()) {
                        if (imageTextRhymeQuestionScoreMap.get(question.getQuestionId()) != null) {
                            imageTextRhymeQuestionScore += imageTextRhymeQuestionScoreMap.get(question.getQuestionId());
                        }
                    }
                    // 篇章的分数：题目的平均分
                    Integer chapterScore = new BigDecimal(imageTextRhymeQuestionScore).divide(new BigDecimal(imageTextRhymeHomework.getChapterQuestions().size()), BigDecimal.ROUND_HALF_UP).intValue();
                    Integer star = imageTextRhymeStarCalculator.calculateImageTextRhymeStar(chapterScore);
                    Integer score = imageTextRhymeStarCalculator.calculateImageTextRhymeScore(star);    //百分制分数
                    chapterScoreMap.put(imageTextRhymeHomework.getChapterId(), score != 0 ? score : 60);
                }
                int totalChapterScore = chapterScoreMap.values().stream().mapToInt(Integer::intValue).sum();
                finalImageTextRhymeScore = new BigDecimal(totalChapterScore).divide(new BigDecimal(newHomeworkApp.getImageTextRhymeQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            // 没有汉字文化模块
            if (!containsChineseCharacterCulture && validatePracticeFinished(context.getHomework(), appAnswer, stoneId)) {
                totalScore = new BigDecimal(wordExerciseScore + finalImageTextRhymeScore).divide(new BigDecimal(moduleNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                newHomeworkResultService.finishHomeworkWordTeachAndPractice(
                        newHomework.toLocation(),
                        context.getUserId(),
                        objectiveConfigType,
                        stoneId,
                        totalScore,
                        duration,
                        wordExerciseScore,
                        finalImageTextRhymeScore);
            } else if (containsChineseCharacterCulture && validatePracticeFinished(context.getHomework(), appAnswer, stoneId)) {
                List<String> chineseCourseProcessIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByStoneDataId(stoneId, objectiveConfigType, WordTeachModuleType.CHINESECHARACTERCULTURE);
                if (newHomeworkApp.getChineseCharacterCultureCourseIds().size() == chineseCourseProcessIds.size()) {
                    // 汉字文化模块最后提交
                    if (WordTeachModuleType.CHINESECHARACTERCULTURE.equals(context.getWordTeachModuleType())) {
                        totalScore = new BigDecimal(wordExerciseScore + finalImageTextRhymeScore + 100D).divide(new BigDecimal(moduleNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        newHomeworkResultService.finishHomeworkWordTeachAndPractice(
                                newHomework.toLocation(),
                                context.getUserId(),
                                objectiveConfigType,
                                stoneId,
                                totalScore,
                                duration,
                                wordExerciseScore,
                                finalImageTextRhymeScore);
                    }
                }
            }
        }
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(NewHomework newHomework,
                                             NewHomeworkResultAppAnswer appAnswer,
                                             String stoneId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkWordTeachQuestions(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE, stoneId, null);
        if (CollectionUtils.isNotEmpty(questionList)) {
            Set<String> homeworkQids = questionList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQids = new HashSet<>();
            if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                resultQids.addAll(appAnswer.getAnswers().keySet());
            }
            if (MapUtils.isNotEmpty(appAnswer.getImageTextRhymeAnswers())) {
                resultQids.addAll(appAnswer.getImageTextRhymeAnswers().keySet());
            }

            result = CollectionUtils.isEqualCollection(homeworkQids, resultQids);
        }
        return result;
    }
}
