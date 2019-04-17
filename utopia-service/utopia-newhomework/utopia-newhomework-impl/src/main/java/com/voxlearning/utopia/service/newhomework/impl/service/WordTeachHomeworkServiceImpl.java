package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.NewContentLoader;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.WordTeachHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/12/3 下午2:22
 */
@Named
@Service(interfaceClass = WordTeachHomeworkService.class)
@ExposeService(interfaceClass = WordTeachHomeworkService.class)
public class WordTeachHomeworkServiceImpl implements WordTeachHomeworkService {

    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    private NewContentLoader newContentLoader;

    @Override
    public List<Map> getWordTeachSummaryInfo(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        List<Map> practices = new ArrayList<>();
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyList();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }
        List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE);
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        if (newHomeworkResult == null) {
            return Collections.emptyList();
        }
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE) != null) {
            appAnswers = newHomeworkResult.getPractices().get(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE).getAppAnswers();
        }

        //题包所属课程名称
        Set<String> lessonIds = newHomeworkApps
                .stream()
                .map(NewHomeworkApp::getLessonId)
                .collect(Collectors.toSet());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoader.loadBookCatalogByCatalogIds(lessonIds);

        for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
            LinkedHashMap<String, Object> practiceMap = new LinkedHashMap<>();
            String stoneDataId = newHomeworkApp.getStoneDataId();
            practiceMap.put("stoneDataId", stoneDataId);
            boolean stoneDataFinished = false;
            int wordExerciseFinishedQuestionCount = 0;
            int imageTextRhymeFinishedQuestionCount = 0;
            int chineseCharacterCultureQuestionCount = 0;
            if (newBookCatalogMap.get(newHomeworkApp.getLessonId()) != null) {
                practiceMap.put("lessonName", newBookCatalogMap.get(newHomeworkApp.getLessonId()).getName());
            }
            if (MapUtils.isNotEmpty(appAnswers) && appAnswers.get(stoneDataId) != null) {
                NewHomeworkResultAppAnswer appAnswer = appAnswers.get(stoneDataId);
                stoneDataFinished = appAnswers.get(stoneDataId).isFinished();
                if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                    wordExerciseFinishedQuestionCount = appAnswer.getAnswers().size();
                }
                if (MapUtils.isNotEmpty(appAnswer.getImageTextRhymeAnswers())) {
                    imageTextRhymeFinishedQuestionCount = appAnswer.getImageTextRhymeAnswers().size();
                }
                if (MapUtils.isNotEmpty(appAnswer.getChineseCourses())) {
                    chineseCharacterCultureQuestionCount = appAnswer.getChineseCourses().size();
                }
            }
            practiceMap.put("finished", stoneDataFinished);

            // 字词训练模块
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions())) {
                Map<String, Object> wordExerciseMap = new HashMap<>();
                wordExerciseMap.put("wordTeachModuleType", WordTeachModuleType.WORDEXERCISE);
                wordExerciseMap.put("questionCount", newHomeworkApp.getWordExerciseQuestions().size());
                wordExerciseMap.put("doCount", wordExerciseFinishedQuestionCount);
                wordExerciseMap.put("finished", wordExerciseFinishedQuestionCount == newHomeworkApp.getWordExerciseQuestions().size());
                wordExerciseMap.put("doHomeworkUrl", UrlUtils.buildUrlQuery("/appdata/obtain/wordteach/module"
                        + Constants.AntiHijackExt, MiscUtils.m("homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", WordTeachModuleType.WORDEXERCISE, "sid", studentId)));
                practiceMap.put("wordExerciseInfo", wordExerciseMap);
            }
            // 图文入韵模块
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions())) {
                Map<String, Object> imageTextRhymeMap = new HashMap<>();
                // 题目数量
                int practicesCount = 0;
                for (ImageTextRhymeHomework imageTextRhymeHomework : newHomeworkApp.getImageTextRhymeQuestions()) {
                    practicesCount += imageTextRhymeHomework.getChapterQuestions().size();
                }
                imageTextRhymeMap.put("wordTeachModuleType", WordTeachModuleType.IMAGETEXTRHYME);
                imageTextRhymeMap.put("chapterCount", newHomeworkApp.getImageTextRhymeQuestions().size());
                imageTextRhymeMap.put("questionCount", practicesCount);
                imageTextRhymeMap.put("doCount", imageTextRhymeFinishedQuestionCount);
                imageTextRhymeMap.put("finished", practicesCount == imageTextRhymeFinishedQuestionCount);
                imageTextRhymeMap.put("doHomeworkUrl", UrlUtils.buildUrlQuery("/appdata/obtain/wordteach/module"
                        + Constants.AntiHijackExt, MiscUtils.m("homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", WordTeachModuleType.IMAGETEXTRHYME, "sid", studentId)));
                practiceMap.put("imageTextRhymeInfo", imageTextRhymeMap);
            }
            // 汉字文化模块
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds())) {
                Map<String, Object> chineseCharacterCultureMap = new HashMap<>();
                chineseCharacterCultureMap.put("wordTeachModuleType", WordTeachModuleType.CHINESECHARACTERCULTURE);
                chineseCharacterCultureMap.put("questionCount", newHomeworkApp.getChineseCharacterCultureCourseIds().size());
                chineseCharacterCultureMap.put("finished", chineseCharacterCultureQuestionCount == newHomeworkApp.getChineseCharacterCultureCourseIds().size());
                chineseCharacterCultureMap.put("doCount", chineseCharacterCultureQuestionCount);
                chineseCharacterCultureMap.put("doHomeworkUrl", UrlUtils.buildUrlQuery("/appdata/obtain/wordteach/module"
                        + Constants.AntiHijackExt, MiscUtils.m("homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", WordTeachModuleType.CHINESECHARACTERCULTURE, "sid", studentId)));
                practiceMap.put("chineseCharacterCultureInfo", chineseCharacterCultureMap);
            }
            practices.add(practiceMap);
        }
        return practices;
    }

    @Override
    public List<Map> getModuleSummaryInfo(String homeworkId, Long studentId, String stoneDataId, WordTeachModuleType wordTeachModuleType) {
        if (StringUtils.isBlank(homeworkId) || wordTeachModuleType == null) {
            return Collections.emptyList();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return Collections.emptyList();
        }

        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        Map<String, NewHomeworkResultAppAnswer> appAnswerMap = new HashMap<>();
        if (newHomeworkResult != null
                && MapUtils.isNotEmpty(newHomeworkResult.getPractices())
                && newHomeworkResult.getPractices().get(objectiveConfigType) != null) {
            appAnswerMap = newHomeworkResult.getPractices().get(objectiveConfigType).getAppAnswers();
        }
        Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = new HashMap<>();
        int wordExerciseDoCount = 0;
        if (MapUtils.isNotEmpty(appAnswerMap) && appAnswerMap.get(stoneDataId) != null) {
            NewHomeworkResultAppAnswer appAnswer = appAnswerMap.get(stoneDataId);
            Set<String> processResultIds = new HashSet<>();
            if (wordTeachModuleType.equals(WordTeachModuleType.WORDEXERCISE)) {
                if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                    wordExerciseDoCount = appAnswer.getAnswers().size();
                    processResultIds.addAll(appAnswer.getAnswers().values());
                }
            } else if (wordTeachModuleType.equals(WordTeachModuleType.IMAGETEXTRHYME)) {
                if (MapUtils.isNotEmpty(appAnswer.getImageTextRhymeAnswers())) {
                    processResultIds.addAll(appAnswer.getImageTextRhymeAnswers().values());
                }
            } else if (wordTeachModuleType.equals(WordTeachModuleType.CHINESECHARACTERCULTURE)) {
                if (MapUtils.isNotEmpty(appAnswer.getChineseCourses())) {
                    processResultIds.addAll(appAnswer.getChineseCourses().values());
                }
            }
            newHomeworkProcessResultMap = newHomeworkProcessResultLoader.loads(homeworkId, processResultIds);
        }
        // 作业原信息
        List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
        NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
        for (NewHomeworkApp homeworkApp : newHomeworkApps) {
            if (homeworkApp.getStoneDataId().equals(stoneDataId)) {
                newHomeworkApp = homeworkApp;
            }
        }
        List<Map> resultList = new LinkedList<>();
        LinkedHashMap<String, Object> resultMap;
        if (wordTeachModuleType.equals(WordTeachModuleType.WORDEXERCISE)) {
            resultMap = new LinkedHashMap<>();
            resultMap.put("doCount", wordExerciseDoCount);
            resultMap.put("questionCount", newHomeworkApp.getWordExerciseQuestions().size());
            resultMap.put("processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
            resultMap.put("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions"
                    + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", wordTeachModuleType, "sid", studentId)));
            resultMap.put("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer"
                    + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", wordTeachModuleType, "sid", studentId)));
            resultList.add(resultMap);
        } else if (wordTeachModuleType.equals(WordTeachModuleType.IMAGETEXTRHYME)) {
            for (ImageTextRhymeHomework imageTextRhyme : newHomeworkApp.getImageTextRhymeQuestions()) {
                resultMap = new LinkedHashMap<>();
                resultMap.put("chapterId", imageTextRhyme.getChapterId());
                resultMap.put("title", imageTextRhyme.getTitle());
                resultMap.put("imageUrl", imageTextRhyme.getImageUrl() != null ? imageTextRhyme.getImageUrl() : NewHomeworkConstants.WORD_TEACH_IMAGE_TEXT_RHYME_DEFAULT_IMG);
                resultMap.put("processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
                resultMap.put("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions"
                        + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", wordTeachModuleType, "sid", studentId)));
                resultMap.put("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer"
                        + Constants.AntiHijackExt, MiscUtils.m("objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "stoneDataId", stoneDataId, "wordTeachModuleType", wordTeachModuleType, "sid", studentId)));
                resultMap.put("shareUrl", UrlUtils.buildUrlQuery("/exam/flash/student/imagetextrhyme/detail"
                        + Constants.AntiHijackExt, MiscUtils.m("homeworkId", homeworkId, "studentId", studentId, "stoneDataId", stoneDataId, "chapterId", imageTextRhyme.getChapterId(), "sid", studentId)));
                resultMap.put("questionCount", imageTextRhyme.getChapterQuestions().size());
                double score = 0;
                int doCount = 0;
                Set<String> questionIds = imageTextRhyme.getChapterQuestions()
                        .stream()
                        .map(NewHomeworkQuestion::getQuestionId)
                        .collect(Collectors.toSet());
                if (MapUtils.isNotEmpty(newHomeworkProcessResultMap)) {
                    for (NewHomeworkProcessResult processResult : newHomeworkProcessResultMap.values()) {
                        if (questionIds.contains(processResult.getQuestionId())) {
                            score += processResult.getActualScore();
                            doCount++;
                        }
                    }
                }

                if (doCount == imageTextRhyme.getChapterQuestions().size()) {
                    double finalScore = new BigDecimal(score).divide(new BigDecimal(imageTextRhyme.getChapterQuestions().size()), 2, BigDecimal.ROUND_DOWN).doubleValue();
                    resultMap.put("score", finalScore);
                    resultMap.put("finished", "done");
                } else if (doCount == 0) {
                    resultMap.put("finished", "todo");
                } else {
                    resultMap.put("finished", "doing");
                }
                resultMap.put("doCount", doCount);
                resultList.add(resultMap);
            }
        } else if (wordTeachModuleType.equals(WordTeachModuleType.CHINESECHARACTERCULTURE)) {
            if (CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds())) {
                Set<String> allCourseIds = new HashSet<>(newHomeworkApp.getChineseCharacterCultureCourseIds());
                // 课程完成情况
                Map<String, Boolean> courseFinishMap = new HashMap<>();
                if (MapUtils.isNotEmpty(newHomeworkProcessResultMap)) {
                    for (NewHomeworkProcessResult processResult : newHomeworkProcessResultMap.values()) {
                        if (allCourseIds.contains(processResult.getCourseId())) {
                            courseFinishMap.put(processResult.getCourseId(), true);
                        }
                    }
                }

                Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(allCourseIds);
                for (IntelDiagnosisCourse course : intelDiagnosisCourseMap.values()) {
                    resultMap = new LinkedHashMap<>();
                    resultMap.put("courseId", course.getId());
                    resultMap.put("courseName", course.getName());
                    resultMap.put("backgroundImage", course.getCover() != null ? course.getCover().getFileUrl() : NewHomeworkConstants.WORD_TEACH_CHARACTER_CULTURE_DEFAULT_IMG);
                    resultMap.put("url", "exam/flash/light/interaction/v2/course.api");
                    resultMap.put("processResultUrl", UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", studentId)));
                    resultMap.put("finished", courseFinishMap.get(course.getId()) != null ? courseFinishMap.get(course.getId()) : false);
                    resultList.add(resultMap);
                }
            }
        }
        return resultList;
    }
}
