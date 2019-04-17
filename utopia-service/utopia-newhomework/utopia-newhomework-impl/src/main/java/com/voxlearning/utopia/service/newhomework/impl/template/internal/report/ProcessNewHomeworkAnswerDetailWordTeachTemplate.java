package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportPersonalRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.NewQuestionReportBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.rate.QuestionDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.QuestionReportDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.wordteach.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.ImageTextRhymeStarCalculator;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessNewHomeworkAnswerDetailTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.api.entity.TestMethod;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 字词讲练处理模板
 * @author: Mr_VanGogh
 * @date: 2018/12/14 下午2:58
 */
@Named
public class ProcessNewHomeworkAnswerDetailWordTeachTemplate extends ProcessNewHomeworkAnswerDetailTemplate {

    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    private ImageTextRhymeStarCalculator imageTextRhymeStarCalculator;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
    }

    @Override
    public String processStudentPartTypeScore(NewHomework newHomework, NewHomeworkResultAnswer newHomeworkResultAnswer, ObjectiveConfigType type) {
        Integer score = newHomeworkResultAnswer.processScore(type);
        return SafeConverter.toInt(score) + "分";
    }

    @Override
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());
        Map<String, Object> typeResult = new LinkedHashMap<>();
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            typeResult.put("avgScore", 0);
            typeResult.put("avgDuration", 0);
            result.put(type, typeResult);
            return;
        }
        int totalDuration = 0;
        Double totalScore = 0d;
        int finishCount = 0;
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices()) || newHomeworkResult.getPractices().get(type) == null) {
                continue;
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
            Integer duration;
            if (newHomeworkResultAnswer.processDuration() != null) {
                duration = newHomeworkResultAnswer.processDuration();
                totalDuration += SafeConverter.toInt(new BigDecimal(SafeConverter.toInt(duration)).divide(new BigDecimal(60), 0, BigDecimal.ROUND_UP).intValue());
            }
            if (newHomeworkResultAnswer.getScore() != null) {
                totalScore += newHomeworkResultAnswer.getScore();
            }
            finishCount++;
        }
        if (finishCount == 0) {
            typeResult.put("avgScore", 0);
            typeResult.put("avgDuration", 0);
            result.put(type, typeResult);
            return;
        }
        int avgDuration = new BigDecimal(totalDuration).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_UP).intValue();
        int avgScore = new BigDecimal(totalScore).divide(new BigDecimal(finishCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
        typeResult.put("finishCount", finishCount);
        typeResult.put("avgScore", avgScore);
        typeResult.put("avgDuration", avgDuration);
        typeResult.put("type", type);
        typeResult.put("typeName", type.getValue());
        result.put(type, typeResult);
    }

    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        Map<Long, NewHomeworkResult> newHomeworkResultMap = reportRateContext.getNewHomeworkResultMap();
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = reportRateContext.getNewHomeworkProcessResultMap();

        // 题包学生完成人数情况
        Map<String, Integer> stoneIdFinishStudentMap = new HashMap<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResult.getPractices().get(type).getAppAnswers();
            for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
                if (!appAnswer.isFinished()) {
                    continue;
                }
                String stoneId = appAnswer.getStoneId();
                if (stoneIdFinishStudentMap.get(stoneId) != null) {
                    stoneIdFinishStudentMap.put(stoneId, stoneIdFinishStudentMap.get(stoneId) + 1);
                } else {
                    stoneIdFinishStudentMap.put(appAnswer.getStoneId(), 1);
                }
            }
        }

        //章节
        Set<String> sectionIds = target.getApps()
                .stream()
                .filter(n -> n.getSectionId() != null)
                .map(NewHomeworkApp::getSectionId)
                .collect(Collectors.toSet());
        //章节信息
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionIds);
        //汉子文化 课程名称
        Set<String> courseIds = target.getApps()
                .stream()
                .filter(n -> CollectionUtils.isNotEmpty(n.getChineseCharacterCultureCourseIds()))
                .map(NewHomeworkApp::getChineseCharacterCultureCourseIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<String, IntelDiagnosisCourse> courseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);

        LinkedList<WordTeachAndPracticeClazzData> wordTeachAndPracticeClazzDatas = new LinkedList<>();
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            WordTeachAndPracticeClazzData wordTeachAndPracticeClazzData = new WordTeachAndPracticeClazzData();
            wordTeachAndPracticeClazzData.setSectionId(newHomeworkApp.getSectionId());
            wordTeachAndPracticeClazzData.setSectionName(MapUtils.isNotEmpty(newBookCatalogMap) && newBookCatalogMap.get(newHomeworkApp.getSectionId()) != null ?
                    newBookCatalogMap.get(newHomeworkApp.getSectionId()).getName() : "");
            wordTeachAndPracticeClazzData.setStoneId(newHomeworkApp.getStoneDataId());

            // 字词训练
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions())) {
                List<NewHomeworkQuestion> wordExerciseQuestions = newHomeworkApp.getWordExerciseQuestions();
                // 题目信息
                Map<String, QuestionReportDetail> questionReportDetailMap = new LinkedHashMap<>();
                List<QuestionReportDetail> questionsInfo = new LinkedList<>();
                for (NewHomeworkQuestion newHomeworkQuestion : wordExerciseQuestions) {
                    QuestionReportDetail questionReportDetail = new QuestionReportDetail();
                    questionReportDetail.setQuestionId(newHomeworkQuestion.getQuestionId());
                    questionReportDetailMap.put(newHomeworkQuestion.getQuestionId(), questionReportDetail);
                    questionsInfo.add(questionReportDetail);
                }
                for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResultMap.values()) {
                    if (questionReportDetailMap.containsKey(newHomeworkProcessResult.getQuestionId())) {
                        QuestionReportDetail questionReportDetail = questionReportDetailMap.get(newHomeworkProcessResult.getQuestionId());
                        questionReportDetail.setTotalNum(1 + questionReportDetail.getTotalNum());
                        if (SafeConverter.toBoolean(newHomeworkProcessResult.isIntervention())) {
                            questionReportDetail.setHasIntervention(true);
                        }
                        if (SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                            questionReportDetail.setInterventionRightNum(1 + questionReportDetail.getInterventionRightNum());
                        }
                        if (!SafeConverter.toBoolean(newHomeworkProcessResult.isIntervention()) && SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                            questionReportDetail.setRightNum(1 + questionReportDetail.getRightNum());
                        }
                    }
                }
                //每个题的正确率
                questionsInfo.stream()
                        .filter(o -> o.getTotalNum() > 0)
                        .forEach(o -> {
                            int firstProportion = new BigDecimal(100 * o.getRightNum()).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                            o.setFirstProportion(firstProportion);
                            if (o.isHasIntervention()) {
                                int proportion = new BigDecimal(100 * o.getInterventionRightNum()).divide(new BigDecimal(o.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();;
                                o.setProportion(proportion);
                            } else {
                                o.setProportion(firstProportion);
                            }
                        });
                WordExerciseModuleClazzData wordExerciseModuleClazzData = new WordExerciseModuleClazzData();
                wordExerciseModuleClazzData.setWordTeachModuleType(WordTeachModuleType.WORDEXERCISE);
                wordExerciseModuleClazzData.setModuleName(WordTeachModuleType.WORDEXERCISE.getName());
                wordExerciseModuleClazzData.setQuestionReportDetails(questionsInfo);
                wordTeachAndPracticeClazzData.setWordExerciseModuleClazzData(wordExerciseModuleClazzData);
            }

            // 图文入韵
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions())) {
                List<ImageTextRhymeHomework> imageTextRhymeQuestions = newHomeworkApp.getImageTextRhymeQuestions();
                List<ImageTextRhymeModuleClazzData.ImageTextRhymeChapterData> imageTextRhymeChapterDatas = new ArrayList<>();
                // 篇章信息
                for (ImageTextRhymeHomework imageTextRhymeQuestion : imageTextRhymeQuestions) {
                    ImageTextRhymeModuleClazzData.ImageTextRhymeChapterData imageTextRhymeChapterData = new ImageTextRhymeModuleClazzData.ImageTextRhymeChapterData();
                    imageTextRhymeChapterData.setChapterId(imageTextRhymeQuestion.getChapterId());
                    imageTextRhymeChapterData.setTitle(imageTextRhymeQuestion.getTitle());
                    imageTextRhymeChapterData.setFinishNum(stoneIdFinishStudentMap.get(newHomeworkApp.getStoneDataId()));
                    imageTextRhymeChapterDatas.add(imageTextRhymeChapterData);
                }
                ImageTextRhymeModuleClazzData imageTextRhymeModuleClazzData = new ImageTextRhymeModuleClazzData();
                imageTextRhymeModuleClazzData.setModuleName(WordTeachModuleType.IMAGETEXTRHYME.getName());
                imageTextRhymeModuleClazzData.setWordTeachModuleType(WordTeachModuleType.IMAGETEXTRHYME);
                imageTextRhymeModuleClazzData.setImageTextRhymeChapterDatas(imageTextRhymeChapterDatas);
                wordTeachAndPracticeClazzData.setImageTextRhymeModuleClazzData(imageTextRhymeModuleClazzData);
            }

            // 汉字文化
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds())) {
                List<String> chineseCharacterCultureCourseIds = newHomeworkApp.getChineseCharacterCultureCourseIds();
                List<ChineseCharacterCultureModuleClazzData.ChineseCharacterCultureCourseData> courseDatas = new ArrayList<>();
                // 课程信息
                for (String courseId : chineseCharacterCultureCourseIds) {
                    if (courseMap.get(courseId) == null) {
                        continue;
                    }
                    ChineseCharacterCultureModuleClazzData.ChineseCharacterCultureCourseData courseData = new ChineseCharacterCultureModuleClazzData.ChineseCharacterCultureCourseData();
                    courseData.setCourseId(courseId);
                    courseData.setTitle(courseMap.get(courseId).getName());
                    courseData.setFinishNum(stoneIdFinishStudentMap.get(newHomeworkApp.getStoneDataId()));
                    courseDatas.add(courseData);
                }
                ChineseCharacterCultureModuleClazzData cultureModuleClazzData = new ChineseCharacterCultureModuleClazzData();
                cultureModuleClazzData.setWordTeachModuleType(WordTeachModuleType.CHINESECHARACTERCULTURE);
                cultureModuleClazzData.setModuleName(WordTeachModuleType.CHINESECHARACTERCULTURE.getName());
                cultureModuleClazzData.setCourseDatas(courseDatas);
                wordTeachAndPracticeClazzData.setChineseCharacterCultureModuleClazzData(cultureModuleClazzData);
            }
            wordTeachAndPracticeClazzDatas.add(wordTeachAndPracticeClazzData);
        }
        reportRateContext.getResult().put(type.name(), wordTeachAndPracticeClazzDatas);
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportPersonalRateContext reportRateContext) {
        ObjectiveConfigType type = reportRateContext.getType();
        NewHomeworkPracticeContent target = reportRateContext.getNewHomework().findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        NewHomeworkResultAnswer newHomeworkResultAnswer = reportRateContext.getNewHomeworkResult().getPractices().get(type);
        //章节信息
        Set<String> sectionIds = target.getApps()
                .stream()
                .filter(n -> n.getSectionId() != null)
                .map(NewHomeworkApp::getSectionId)
                .collect(Collectors.toSet());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionIds);
        //汉字文化课程
        Map<String, IntelDiagnosisCourse> courseMap = new HashMap<>();
        List<String> courseIds = target.getApps()
                .stream()
                .filter(n -> CollectionUtils.isNotEmpty(n.getChineseCharacterCultureCourseIds()))
                .map(NewHomeworkApp::getChineseCharacterCultureCourseIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(courseIds)) {
            courseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
        }

        List<WordTeachAndPracticeData> wordTeachAndPracticeDataList = new ArrayList<>();
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            String stoneDataId = newHomeworkApp.getStoneDataId();
            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = newHomeworkResultAnswer.getAppAnswers().get(stoneDataId);
            LinkedHashMap<String, String> answers = newHomeworkResultAppAnswer.getAnswers();
            LinkedHashMap<String, String> imageTextRhymeAnswers = newHomeworkResultAppAnswer.getImageTextRhymeAnswers();

            WordTeachAndPracticeData wordTeachAndPracticeData = new WordTeachAndPracticeData();
            wordTeachAndPracticeData.setStoneId(stoneDataId);
            wordTeachAndPracticeData.setSectionId(newHomeworkApp.getSectionId());
            wordTeachAndPracticeData.setSectionName(MapUtils.isNotEmpty(newBookCatalogMap) && newBookCatalogMap.get(newHomeworkApp.getSectionId()) != null ?
                    newBookCatalogMap.get(newHomeworkApp.getSectionId()).getName() : "");
            // 字词训练
            if (MapUtils.isNotEmpty(answers)) {
                Map<String, NewHomeworkProcessResult> processResultMap = answers.values()
                        .stream()
                        .filter(reportRateContext.getNewHomeworkProcessResultMap()::containsKey)
                        .collect(Collectors
                                .toMap(Function.identity(), reportRateContext.getNewHomeworkProcessResultMap()::get));
                Map<String, NewQuestion> newQuestionMap = answers.keySet()
                        .stream()
                        .filter(reportRateContext.getAllNewQuestionMap()::containsKey)
                        .collect(Collectors
                                .toMap(Function.identity(), reportRateContext.getAllNewQuestionMap()::get));

                WordExerciseModuleData wordExerciseModuleData = new WordExerciseModuleData();
                LinkedList<WordExerciseModuleData.WordExerciseQuestionData> wordExerciseQuestionDataList = new LinkedList<>();
                double firstScore = 0d;
                double finalScore = 0d;
                int hasIntervention = 0;
                for (NewHomeworkQuestion question : newHomeworkApp.getWordExerciseQuestions()) {
                    String questionId = question.getQuestionId();
                    NewQuestion newQuestion = newQuestionMap.get(questionId);
                    if (newQuestion == null) {
                        continue;
                    }
                    NewHomeworkProcessResult tempResult = processResultMap.get(answers.get(questionId));
                    if (tempResult == null) {
                        continue;
                    }
                    List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
                    List<List<String>> standardAnswers = subContents
                            .stream()
                            .map(o -> o.getAnswerList(reportRateContext.getNewHomework().getSubject()))
                            .collect(Collectors.toList());
                    WordExerciseModuleData.WordExerciseQuestionData wordExerciseQuestionData = new WordExerciseModuleData.WordExerciseQuestionData();
                    wordExerciseQuestionData.setQid(questionId);
                    wordExerciseQuestionData.setDifficulty(newQuestion.getDifficultyInt());
                    wordExerciseQuestionData.setContentType(reportRateContext.getContentTypeMap().get(newQuestion.getContentTypeId()) != null ?
                            reportRateContext.getContentTypeMap().get(newQuestion.getContentTypeId()).getName() : "无题型");
                    wordExerciseQuestionData.setStandardAnswers(tempResult != null ? NewHomeworkUtils.pressAnswer(subContents, standardAnswers) : "");
                    wordExerciseQuestionData.setUserAnswers(tempResult != null ? NewHomeworkUtils.pressAnswer(subContents, tempResult.getUserAnswers()) : "");
                    if (!SafeConverter.toBoolean(tempResult.isIntervention())) {
                        firstScore += tempResult.getScore();
                    }
                    if (SafeConverter.toBoolean(tempResult.isIntervention())) {
                        hasIntervention ++ ;
                    }
                    finalScore += tempResult.getScore();

                    wordExerciseQuestionDataList.add(wordExerciseQuestionData);
                }
                wordExerciseModuleData.setModuleType(WordTeachModuleType.WORDEXERCISE);
                wordExerciseModuleData.setModuleName(WordTeachModuleType.WORDEXERCISE.getName());
                wordExerciseModuleData.setFirstScore(SafeConverter.toDouble(Math.round(firstScore)));
                wordExerciseModuleData.setFinalScore(SafeConverter.toDouble(Math.round(finalScore)));
                wordExerciseModuleData.setHasIntervention(hasIntervention > 0);
                wordExerciseModuleData.setWordExerciseQuestionData(wordExerciseQuestionDataList);
                wordTeachAndPracticeData.setWordExerciseModuleData(wordExerciseModuleData);
            }

            // 图文入韵
            if (MapUtils.isNotEmpty(imageTextRhymeAnswers)) {
                List<ImageTextRhymeHomework> imageTextRhymeQuestions = newHomeworkApp.getImageTextRhymeQuestions();
                Map<String, NewHomeworkProcessResult> processResultMap = imageTextRhymeAnswers.values()
                        .stream()
                        .filter(reportRateContext.getNewHomeworkProcessResultMap()::containsKey)
                        .collect(Collectors
                                .toMap(Function.identity(), reportRateContext.getNewHomeworkProcessResultMap()::get));

                ImageTextRhymeModuleData imageTextRhymeModuleData = new ImageTextRhymeModuleData();
                List<ImageTextRhymeModuleData.ImageTextRhymeData> imageTextRhymeDataList = new ArrayList<>();
                for (ImageTextRhymeHomework imageTextRhymeHomework : imageTextRhymeQuestions) {
                    ImageTextRhymeModuleData.ImageTextRhymeData imageTextRhymeData = new ImageTextRhymeModuleData.ImageTextRhymeData();
                    imageTextRhymeData.setChapterId(imageTextRhymeHomework.getChapterId());
                    imageTextRhymeData.setTitle(imageTextRhymeHomework.getTitle());
                    int totalScore = 0;
                    for (NewHomeworkQuestion question : imageTextRhymeHomework.getChapterQuestions()) {
                        String questionId = question.getQuestionId();
                        if (processResultMap.get(imageTextRhymeAnswers.get(questionId)) != null) {
                            totalScore += SafeConverter.toDouble(processResultMap.get(imageTextRhymeAnswers.get(questionId)).getActualScore());
                        }
                    }
                    double score = new BigDecimal(totalScore).divide(new BigDecimal(imageTextRhymeHomework.getChapterQuestions().size()), 2, BigDecimal.ROUND_DOWN).doubleValue();
                    int star = imageTextRhymeStarCalculator.calculateImageTextRhymeStar((int)score);
                    imageTextRhymeData.setScore(score);
                    imageTextRhymeData.setStar(star);
                    imageTextRhymeData.setFlashvarsUrl(UrlUtils.buildUrlQuery("/exam/flash/student/imagetextrhyme/detail"
                            + Constants.AntiHijackExt, MiscUtils.m("homeworkId", reportRateContext.getNewHomework().getId(),
                            "studentId", reportRateContext.getUser().getId(), "stoneDataId", stoneDataId, "chapterId", imageTextRhymeHomework.getChapterId())));
                    imageTextRhymeDataList.add(imageTextRhymeData);
                }
                imageTextRhymeModuleData.setModuleType(WordTeachModuleType.IMAGETEXTRHYME);
                imageTextRhymeModuleData.setModuleName(WordTeachModuleType.IMAGETEXTRHYME.getName());
                imageTextRhymeModuleData.setImageTextRhymeDataList(imageTextRhymeDataList);
                wordTeachAndPracticeData.setImageTextRhymeModuleData(imageTextRhymeModuleData);
            }

            // 汉字文化
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds())) {
                ChineseCharacterCultureModuleData chineseCharacterCultureModuleData = new ChineseCharacterCultureModuleData();
                List<ChineseCharacterCultureModuleData.ChineseCharacterCultureData> chineseCultureDataList = new ArrayList<>();
                for (String courseId : newHomeworkApp.getChineseCharacterCultureCourseIds()) {
                    if (courseMap.get(courseId) == null) {
                        continue;
                    }
                    ChineseCharacterCultureModuleData.ChineseCharacterCultureData chineseCharacterCultureData = new ChineseCharacterCultureModuleData.ChineseCharacterCultureData();
                    chineseCharacterCultureData.setCourseId(courseId);
                    chineseCharacterCultureData.setCourseName(courseMap.get(courseId).getName());
                    chineseCharacterCultureData.setFinished(newHomeworkResultAnswer.isFinished());
                    chineseCultureDataList.add(chineseCharacterCultureData);
                }
                chineseCharacterCultureModuleData.setModuleType(WordTeachModuleType.CHINESECHARACTERCULTURE);
                chineseCharacterCultureModuleData.setModuleName(WordTeachModuleType.CHINESECHARACTERCULTURE.getName());
                chineseCharacterCultureModuleData.setChineseCharacterCultureData(chineseCultureDataList);
                wordTeachAndPracticeData.setChineseCultureModuleData(chineseCharacterCultureModuleData);
            }
            wordTeachAndPracticeDataList.add(wordTeachAndPracticeData);
        }
        if (CollectionUtils.isNotEmpty(wordTeachAndPracticeDataList)) {
            reportRateContext.getResultMap().put(reportRateContext.getType(), wordTeachAndPracticeDataList);
        }
    }

    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {
        ObjectiveConfigTypeParameter parameter = context.getParameter();
        String stoneId = parameter.getStoneId();
        if (StringUtils.isEmpty(stoneId)) {
            MapMessage mapMessage = MapMessage.errorMessage("题包数据不存在");
            context.setMapMessage(mapMessage);
            return;
        }
        NewHomeworkPracticeContent target = context.getTarget();
        NewHomework newHomework = context.getNewHomework();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();

        List<String> questionIds = new LinkedList<>();
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            if (newHomeworkApp.getStoneDataId().equals(stoneId) && CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions())) {
                List<NewHomeworkQuestion> wordExerciseQuestions = newHomeworkApp.getWordExerciseQuestions();
                questionIds.addAll(wordExerciseQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            }
        }

        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(type));
        List<NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjs = objectiveConfigTypeListMap.getOrDefault(type, Collections.emptyList());
        for (NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj : newHomeworkQuestionObjs) {
            for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
                subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
            }
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> newHomeworkProcessResultIds = subHomeworkResultAnswerMap.values()
                .stream()
                .map(SubHomeworkResultAnswer::getProcessId)
                .collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(context.getNewHomework().getId(), newHomeworkProcessResultIds);

        //对应每题的信息  NewQuestionReportBO
        List<NewQuestionReportBO> newQuestionReportBOs = target.processNewHomeworkQuestion(false)
                .stream()
                .map(o -> {
                    if (!allQuestionMap.containsKey(o.getQuestionId())) {
                        return null;
                    }
                    NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(o.getQuestionId());
                    newQuestionReportBO.setType(1);
                    NewQuestion question = allQuestionMap.get(o.getQuestionId());
                    List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                    //复合体的结构
                    for (int i = 0; i < subContents.size(); i++) {
                        NewQuestionReportBO.SubQuestion subQuestion = new NewQuestionReportBO.SubQuestion();
                        newQuestionReportBO.getSubQuestions().add(subQuestion);
                    }
                    newQuestionReportBO.setContentType(
                            contentTypeMap.containsKey(question.getContentTypeId()) ?
                                    contentTypeMap.get(question.getContentTypeId()).getName() :
                                    "无题型");
                    newQuestionReportBO.setDifficulty(question.getDifficultyInt());
                    newQuestionReportBO.setDifficultyName(QuestionConstants.newDifficultyMap.get(question.getDifficultyInt()));
                    return newQuestionReportBO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        LinkedHashMap<String, NewQuestionReportBO> newQuestionReportBOMap = newQuestionReportBOs
                .stream()
                .collect(Collectors.toMap(NewQuestionReportBO::getQid, Function.identity(), (t1, t2) -> t2, LinkedHashMap::new));
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!newQuestionReportBOMap.containsKey(p.getQuestionId())) {
                continue;
            }
            if (!allQuestionMap.containsKey(p.getQuestionId())) {
                continue;
            }
            if (!p.getWordTeachModuleType().equals(WordTeachModuleType.WORDEXERCISE)) {
                continue;
            }
            NewQuestion newQuestion = allQuestionMap.get(p.getQuestionId());
            List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
            List<List<String>> userAnswers = p.getUserAnswers();
            if (userAnswers.size() != subContents.size()) {
                continue;
            }
            NewQuestionReportBO newQuestionReportBO = newQuestionReportBOMap.get(p.getQuestionId());
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
            u.setUid(p.getUserId());
            u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
            for (int i = 0; i < subContents.size(); i++) {
                NewQuestionsSubContents newQuestionsSubContents = subContents.get(i);
                List<String> strings = userAnswers.get(i);
                boolean b = p.getSubGrasp().get(i).stream().allMatch(SafeConverter::toBoolean);
                String answer;
                if (b) {
                    answer = "全对学生";
                } else {
                    answer = NewHomeworkUtils.pressAnswer(Collections.singletonList(newQuestionsSubContents), Collections.singletonList(strings));
                    if (!newQuestionReportBO.getSubQuestions().get(i).getUserAnswersMap().containsKey(answer)) {
                        newQuestionReportBO.getSubQuestions().get(i).getUserAnswersMap().put(answer, strings);
                    }
                }
                newQuestionReportBO.getSubQuestions().get(i).getMap().computeIfAbsent(answer, l -> new ArrayList<>()).add(u);
            }
            if (!SafeConverter.toBoolean(p.getGrasp())) {
                newQuestionReportBO.setErrorNum(newQuestionReportBO.getErrorNum() + 1);
            }
        }
        // 失分率和答案的显示
        newQuestionReportBOMap.values()
                .stream()
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    int errorRate = new BigDecimal(100 * o.getErrorNum()).divide(new BigDecimal(o.getNum()), BigDecimal.ROUND_HALF_UP, 0).intValue();
                    o.setErrorRate(errorRate);
                    List<NewQuestionReportBO.SubQuestion> subQuestions = o.getSubQuestions();
                    if (subQuestions.size() > 0) {
                        //复合体信息处理
                        for (NewQuestionReportBO.SubQuestion subQuestion : subQuestions) {
                            Map<String, List<NewQuestionReportBO.UserToQuestion>> map = subQuestion.getMap();
                            if (map.containsKey("全对学生")) {
                                List<NewQuestionReportBO.UserToQuestion> userToQuestions = map.get("全对学生");
                                subQuestion.getAnswer().add(MapUtils.m(
                                        "grasp", true,
                                        "answerWord", "全对学生", "userToQuestions", userToQuestions
                                ));
                                map.remove("全对学生");
                            }
                            for (Map.Entry<String, List<NewQuestionReportBO.UserToQuestion>> entry : map.entrySet()) {
                                subQuestion.getAnswer().add(MapUtils.m(
                                        "grasp", false,
                                        "answerWord", entry.getKey(), "userToQuestions", entry.getValue(),
                                        "userAnswers", subQuestion.getUserAnswersMap().get(entry.getKey())
                                ));
                            }
                            subQuestion.setUserAnswersMap(null);
                            subQuestion.setMap(null);
                        }
                    }
                });
        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        mapMessage.add("questions", newQuestionReportBOMap.values());
    }

    @Override
    public void fetchNewHomeworkSingleQuestionPart(ObjectiveConfigTypePartContext context) {
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        Map<Long, User> userMap = context.getUserMap();
        String questionId = context.getQuestionId();

        MapMessage mapMessage = MapMessage.successMessage();
        context.setMapMessage(mapMessage);
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(Collections.singleton(questionId));
        if (MapUtils.isEmpty(questionMap)) {
            context.setMapMessage(MapMessage.errorMessage());
            return;
        }
        NewHomework newHomework = context.getNewHomework();
        ObjectiveConfigType type = context.getType();
        Map<ObjectiveConfigType, List<NewHomework.NewHomeworkQuestionObj>> objectiveConfigTypeListMap = newHomework.processSubHomeworkResultAnswerIds(Collections.singleton(type));
        Map<String, NewHomework.NewHomeworkQuestionObj> newHomeworkQuestionObjMap = objectiveConfigTypeListMap.getOrDefault(type, Collections.emptyList())
                .stream()
                .filter(n -> n.getJoinKey().contains(context.getStoneDataId()))
                .collect(Collectors.toMap(NewHomework.NewHomeworkQuestionObj::getQuestionId, Function.identity()));
        if (!newHomeworkQuestionObjMap.containsKey(questionId)) {
            return;
        }
        NewHomework.NewHomeworkQuestionObj newHomeworkQuestionObj = newHomeworkQuestionObjMap.get(questionId);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        List<String> subHomeworkResultAnswerIds = new LinkedList<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResultMap.values()) {
            subHomeworkResultAnswerIds.add(newHomeworkQuestionObj.generateSubHomeworkResultAnswerId(day, newHomeworkResult.getUserId()));
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = newHomeworkResultLoader.loadSubHomeworkResultAnswers(subHomeworkResultAnswerIds);
        List<String> processIds = subHomeworkResultAnswerMap.values().stream().map(SubHomeworkResultAnswer::getProcessId).collect(Collectors.toList());
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
        NewQuestion newQuestion = questionMap.get(questionId);

        NewQuestionReportBO newQuestionReportBO = new NewQuestionReportBO(questionId);
        newQuestionReportBO.setType(1);
        List<NewQuestionsSubContents> subContents = newQuestion.getContent().getSubContents();
        for (int i = 0; i < subContents.size(); i++) {
            NewQuestionReportBO.SubQuestion subQuestion = new NewQuestionReportBO.SubQuestion();
            newQuestionReportBO.getSubQuestions().add(subQuestion);
        }
        newQuestionReportBO.setContentType(contentTypeMap.containsKey(newQuestion.getContentTypeId()) ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型");
        newQuestionReportBO.setDifficulty(newQuestion.getDifficultyInt());
        newQuestionReportBO.setDifficultyName(QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()));

        //process的处理
        for (NewHomeworkProcessResult p : newHomeworkProcessResultMap.values()) {
            if (!questionId.equals(p.getQuestionId())) {
                continue;
            }
            List<List<String>> userAnswers = p.getUserAnswers();
            if (userAnswers.size() != subContents.size()) {
                continue;
            }
            if (p.getSubGrasp().size() != subContents.size()) {
                continue;
            }
            newQuestionReportBO.setNum(newQuestionReportBO.getNum() + 1);
            NewQuestionReportBO.UserToQuestion u = new NewQuestionReportBO.UserToQuestion();
            u.setUid(p.getUserId());
            u.setUserName(userMap.containsKey(u.getUid()) ? userMap.get(u.getUid()).fetchRealname() : "");
            for (int i = 0; i < subContents.size(); i++) {
                NewQuestionsSubContents newQuestionsSubContents = subContents.get(i);
                List<String> strings = userAnswers.get(i);
                boolean b = p.getSubGrasp().get(i).stream().allMatch(SafeConverter::toBoolean);
                String answer;
                if (b) {
                    answer = "全对学生";
                } else {
                    answer = NewHomeworkUtils.pressAnswer(Collections.singletonList(newQuestionsSubContents), Collections.singletonList(strings));
                }
                newQuestionReportBO.getSubQuestions().get(i).getMap().computeIfAbsent(answer, l -> new ArrayList<>()).add(u);
            }
            if (!SafeConverter.toBoolean(p.getGrasp())) {
                newQuestionReportBO.setErrorNum(newQuestionReportBO.getErrorNum() + 1);
            }
        }

        if (newQuestionReportBO.getNum() > 0) {
            int errorRate = new BigDecimal(100 * newQuestionReportBO.getErrorNum()).divide(new BigDecimal(newQuestionReportBO.getNum()), BigDecimal.ROUND_HALF_UP, 0).intValue();
            newQuestionReportBO.setErrorRate(errorRate);
            List<NewQuestionReportBO.SubQuestion> subQuestions = newQuestionReportBO.getSubQuestions();
            if (subQuestions.size() > 0) {
                for (NewQuestionReportBO.SubQuestion subQuestion : subQuestions) {
                    Map<String, List<NewQuestionReportBO.UserToQuestion>> map = subQuestion.getMap();
                    if (map.containsKey("全对学生")) {
                        List<NewQuestionReportBO.UserToQuestion> userToQuestions = map.get("全对学生");
                        subQuestion.getAnswer().add(MapUtils.m(
                                "grasp", true,
                                "answerWord", "全对学生", "userToQuestions", userToQuestions
                        ));
                        map.remove("全对学生");
                    }
                    for (Map.Entry<String, List<NewQuestionReportBO.UserToQuestion>> entry : map.entrySet()) {
                        subQuestion.getAnswer().add(MapUtils.m(
                                "grasp", false,
                                "answerWord", entry.getKey(), "userToQuestions", entry.getValue()
                        ));
                    }
                    subQuestion.setMap(null);
                }
            }
        }
        mapMessage.add("newQuestionReportBO", newQuestionReportBO);
    }

    public List<QuestionDetail> newInternalProcessHomeworkAnswerDetail(Map<Long, User> userMap, Map<String, NewQuestion> allNewQuestionMap, Map<Integer, NewContentType> contentTypeMap, NewHomework newHomework, ObjectiveConfigType type, List<String> qids, List<NewHomeworkProcessResult> processResultList) {
        Map<String, NewQuestion> newQuestionMap = qids
                    .stream()
                    .filter(allNewQuestionMap::containsKey)
                    .map(allNewQuestionMap::get)
                    .collect(Collectors
                            .toMap(NewQuestion::getId, Function.identity()));
        Map<String, String> qidToTestMethodId = newQuestionMap.values()
                .stream()
                .filter(q -> CollectionUtils.isNotEmpty(q.testMethodList()))
                .collect(Collectors.toMap(NewQuestion::getId, q -> q.testMethodList().get(0)));
        Map<String, TestMethod> allTestMethodMap = testMethodLoaderClient.loadTestMethodIncludeDisabled(qidToTestMethodId.values());

        Map<String, QuestionDetail> questionDetailMap = new LinkedHashMap<>();
        // 题目基本信息
        for (String qid : qids) {
            if (!newQuestionMap.containsKey(qid)) {
                continue;
            }
            NewQuestion newQuestion = newQuestionMap.get(qid);
            QuestionDetail questionDetail = new QuestionDetail();
            int showType = 0;
            List<Integer> submitWays = newQuestion
                    .getSubmitWays()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(submitWays) && submitWays.contains(1)) {
                showType = 1;
            } else if (CollectionUtils.isNotEmpty(submitWays) && submitWays.contains(2)) {
                showType = 2;
            }
            String testMethodName = "";
            if (qidToTestMethodId.containsKey(qid) && allTestMethodMap.containsKey(qidToTestMethodId.get(qid))) {
                TestMethod testMethod = allTestMethodMap.get(qidToTestMethodId.get(qid));
                testMethodName = testMethod != null ? SafeConverter.toString(testMethod.getName(), "") : "";
            }
            questionDetail.setQid(qid);
            questionDetail.setTestMethodName(testMethodName);
            questionDetail.setShowType(showType);
            questionDetail.setContentType(contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型");
            questionDetail.setDifficulty(newQuestion.getDifficultyInt());
            questionDetail.setAnswerWay("");
            questionDetailMap.put(qid, questionDetail);
        }
        // 题目答题信息
        for (NewHomeworkProcessResult processResult : processResultList) {
            if (!questionDetailMap.containsKey(processResult.getQuestionId())) {
                continue;
            }
            if (!allNewQuestionMap.containsKey(processResult.getQuestionId())) {
                continue;
            }
            if (!userMap.containsKey(processResult.getUserId())) {
                continue;
            }
            User user = userMap.get(processResult.getUserId());
            NewQuestion newQuestion = allNewQuestionMap.get(processResult.getQuestionId());
            QuestionDetail questionDetail = questionDetailMap.get(processResult.getQuestionId());
            questionDetail.setTotalNum(1 + questionDetail.getTotalNum());
            boolean grasp = SafeConverter.toBoolean(processResult.getGrasp());
            if (!grasp) {
                questionDetail.setErrorNum(1 + questionDetail.getErrorNum());
            }
            List<NewQuestionsSubContents> nscs = newQuestion.getContent().getSubContents();
            String answer = pressHomeworkAnswer(nscs, processResult);
            List<String> showPics = processResult
                    .findAllFiles()
                    .stream()
                    .map(NewHomeworkQuestionFileHelper::getFileUrl)
                    .collect(Collectors.toList());
            Boolean review = processResult.getReview();
            Correction correction = processResult.getCorrection();
            QuestionDetail.StudentDetail studentDetail = new QuestionDetail.StudentDetail();
            studentDetail.setAnswer(answer);
            studentDetail.setUserId(user.getId());
            studentDetail.setUserName(user.fetchRealnameIfBlankId());
            studentDetail.setShowPics(showPics);
            studentDetail.setImgUrl(user.fetchImageUrl());
            studentDetail.setReview(review);
            studentDetail.setCorrection(correction);
            studentDetail.setCorrect_des((correction != null) ? correction.getDescription() : "");
            if (grasp) {
                QuestionDetail.Answer rightAnswer;
                if (questionDetail.getRightAnswer() == null) {
                    rightAnswer = new QuestionDetail.Answer();
                    rightAnswer.setAnswer("答案正确");
                    questionDetail.setRightAnswer(rightAnswer);
                } else {
                    rightAnswer = questionDetail.getRightAnswer();
                }
                rightAnswer.getUsers().add(studentDetail);
            } else {
                QuestionDetail.Answer answerDetail;
                if (questionDetail.getErrorAnswerMap().containsKey(answer)) {
                    answerDetail = questionDetail.getErrorAnswerMap().get(answer);
                } else {
                    answerDetail = new QuestionDetail.Answer();
                    answerDetail.setAnswer(answer);
                    questionDetail.getErrorAnswerMap().put(answer, answerDetail);
                    questionDetail.getErrorAnswerList().add(answerDetail);
                }
                answerDetail.getUsers().add(studentDetail);
            }
        }
        //计算错题率：将正确答案的放到回答的list中
        List<QuestionDetail> errorExamList = new LinkedList<>();
        for (String qid : qids) {
            if (!questionDetailMap.containsKey(qid)) {
                continue;
            }
            QuestionDetail questionDetail = questionDetailMap.get(qid);
            if (questionDetail.getTotalNum() > 0) {
                int rate = new BigDecimal(questionDetail.getErrorNum() * 100)
                        .divide(new BigDecimal(questionDetail.getTotalNum()), 0, BigDecimal.ROUND_HALF_UP)
                        .intValue();
                questionDetail.setRate(rate);
            }
            if (questionDetail.getRightAnswer() != null) {
                questionDetail.getErrorAnswerList().add(questionDetail.getRightAnswer());
            }
            questionDetail.setErrorAnswerMap(null);
            errorExamList.add(questionDetail);
        }
        return errorExamList;
    }

    /**
     * 处理答案，各种题型的答案结果会有不同
     *
     * @return 没做的返回无法查看；做完的，返回“,”分隔的答案字符串
     */
    private String pressHomeworkAnswer(List<NewQuestionsSubContents> qscs, NewHomeworkProcessResult processResult) {
        if (SafeConverter.toBoolean(processResult.getGrasp())) {
            return "答案正确";
        }
        List<List<String>> userAnswerList = processResult.getUserAnswers();
        return NewHomeworkUtils.pressAnswer(qscs, userAnswerList);
    }
}
