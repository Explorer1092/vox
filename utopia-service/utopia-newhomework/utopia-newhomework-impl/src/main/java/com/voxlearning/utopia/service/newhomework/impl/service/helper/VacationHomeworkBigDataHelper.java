package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 组装假期作业数据
 *
 * @author xuesong.zhang
 * @since 2016/12/5
 */
@Named
@SuppressWarnings("unchecked")
public class VacationHomeworkBigDataHelper extends NewHomeworkSpringBean {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    public void generateVacationHomework(VacationHomework vacationHomework, VacationHomeworkBook vacationHomeworkBook,
                                         WinterDayPlan winterDayPlan, String bookId, Integer cityCode) {
        Set<ObjectiveConfigType> objectiveConfigTypes = new HashSet<>();
        vacationHomework.setPractices(new ArrayList<>());
        vacationHomeworkBook.setPractices(new LinkedHashMap<>());

        List<Map<String, Object>> winterDayPlanElements = winterDayPlan.getElements();
        if (CollectionUtils.isNotEmpty(winterDayPlanElements)) {
            for (Map<String, Object> element : winterDayPlanElements) {
                ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(SafeConverter.toString(element.get("objectiveConfigType")));
                if (objectiveConfigType != null && !objectiveConfigTypes.contains(objectiveConfigType)) {
                    objectiveConfigTypes.add(objectiveConfigType);
                    switch (objectiveConfigType) {
                        case BASIC_APP:  //基础练习
                            processBasicApp(vacationHomework, element, bookId, cityCode, winterDayPlan.getDayRank());
                            break;
                        case NATURAL_SPELLING:  //自然拼读
                            processNaturalSpelling(vacationHomework, element);
                            break;
                        case DUBBING:  //趣味配音
                        case DUBBING_WITH_SCORE:   //新版趣味配音
                            processDubbing(vacationHomework, element, objectiveConfigType);
                            break;
                        case READING:  //绘本阅读
                            processReading(vacationHomework, element, objectiveConfigType);
                            break;
                        case LEVEL_READINGS:   //新版绘本阅读
                            processLevelReadings(vacationHomework, element, objectiveConfigType);
                            break;
                        case EXAM:  //同步习题
                        case INTELLIGENCE_EXAM:
                            processExam(vacationHomework, vacationHomeworkBook, element, bookId, objectiveConfigType);
                            break;
                        case NEW_READ_RECITE:  //课文读背
                        case READ_RECITE_WITH_SCORE:  //新版课文读背
                            processNewReadRecite(vacationHomework, element, objectiveConfigType);
                            break;
                        case INTERESTING_PICTURE:  //趣味绘本
                        case MENTAL:  //口算
                        case MENTAL_ARITHMETIC:  //新口算
                        case BASIC_KNOWLEDGE:  //基础知识
                        case CHINESE_READING:  //阅读
                            processOthers(vacationHomework, element, objectiveConfigType);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * 处理基础训练的作业信息
     */
    private void processBasicApp(VacationHomework vacationHomework, Map<String, Object> element, String bookId, Integer cityCode, Integer dayRank) {
        // 哈尔滨地区(230100)，过滤相关教材作业(第四周，带有预习的)
        if (NewHomeworkConstants.VACATION_HOMEWORK_FILTER_BOOK.contains(bookId) && NewHomeworkConstants.VACATION_HOMEWORK_FILTER_CITY.contains(cityCode) && 16 <= dayRank && dayRank<= 20) {
            return;
        }

        long basicAppDuration = 0;
        List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
        List<Map<String, Object>> practices = (List<Map<String, Object>>) element.get("practices");
        if (CollectionUtils.isNotEmpty(practices)) {
            Set<String> appKeySet = new HashSet<>();
            List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
            Set<String> allLessonIdSet = practices.stream()
                    .filter(Objects::nonNull)
                    .map(practice -> SafeConverter.toString(practice.get("lessonId")))
                    .collect(Collectors.toSet());
            Map<String, NewBookCatalog> allLessonsMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIdSet);
            Map<String, List<Sentence>> allSentencesList = newEnglishContentLoaderClient.loadEnglishLessonSentences(allLessonIdSet);
            for (Map<String, Object> practice : practices) {
                String lessonId = SafeConverter.toString(practice.get("lessonId"));
                NewBookCatalog lesson = allLessonsMap.get(lessonId);
                List<Sentence> sentenceList = allSentencesList.get(lessonId);
                List<Integer> categoryIds = (List<Integer>) practice.get("category_ids");
                if (lesson != null && CollectionUtils.isNotEmpty(sentenceList) && CollectionUtils.isNotEmpty(categoryIds)) {
                    List<Long> sentenceIds = sentenceList.stream().map(Sentence::getId).collect(Collectors.toList());
                    // 调取题接口
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadRandomQuestionBySentenceIdsAndCategoryIds(sentenceIds, categoryIds, true);
                    for (Integer categoryId : categoryIds) {
                        PracticeType practiceType = allPracticeList.stream()
                                .filter(p -> Objects.equals(p.getCategoryId(), categoryId))
                                .findFirst()
                                .orElse(null);
                        List<NewQuestion> questions = sentenceIds.stream()
                                .map(sid -> questionMap.get(sid + "_" + categoryId))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        if (practiceType != null && CollectionUtils.isNotEmpty(questions) && appKeySet.add(lessonId + "|" + categoryId)) {
                            List<NewHomeworkQuestion> newHomeworkQuestions = buildBasicAppQuestions(questions, practiceType);
                            basicAppDuration += newHomeworkQuestions.stream()
                                    .mapToInt(NewHomeworkQuestion::getSeconds)
                                    .sum();

                            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                            newHomeworkApp.setCategoryId(categoryId);
                            newHomeworkApp.setPracticeId(practiceType.getId());
                            newHomeworkApp.setLessonId(lessonId);
                            newHomeworkApp.setQuestions(newHomeworkQuestions);
                            newHomeworkApps.add(newHomeworkApp);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            NewHomeworkPracticeContent content = new NewHomeworkPracticeContent();
            content.setType(ObjectiveConfigType.BASIC_APP);
            content.setApps(newHomeworkApps);
            content.setIncludeSubjective(false);
            vacationHomework.getPractices().add(content);
            vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + basicAppDuration);
        }
    }

    /**
     * 处理自然拼读的作业信息
     */
    private void processNaturalSpelling(VacationHomework vacationHomework, Map<String, Object> element) {
        long naturalSpellingDuration = 0;
        List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
        List<Map<String, Object>> practices = (List<Map<String, Object>>) element.get("practices");
        if (CollectionUtils.isNotEmpty(practices)) {
            Set<String> appKeySet = new HashSet<>();
            List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
            for (Map<String, Object> practice : practices) {
                List<Map<String, Object>> categoryGroupMapList = (List<Map<String, Object>>) practice.get("category_groups");
                String lessonId = SafeConverter.toString(practice.get("lesson_id"));
                NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
                if (lesson != null && CollectionUtils.isNotEmpty(categoryGroupMapList)) {
                    for (Map<String, Object> map : categoryGroupMapList) {
                        List<Integer> categoryIdsList = (List) map.get("category_ids");
                        Map<String, Object> questionsMap = (Map) map.get("questions_map");
                        if (CollectionUtils.isNotEmpty(categoryIdsList) && MapUtils.isNotEmpty(questionsMap)) {
                            for (Integer categoryId : categoryIdsList) {
                                List<String> questionIdsList = (List<String>) questionsMap.get(SafeConverter.toString(categoryId));
                                if (CollectionUtils.isNotEmpty(questionIdsList)) {
                                    List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIdsList);
                                    PracticeType practiceType = allPracticeList.stream()
                                            .filter(p -> Objects.equals(p.getCategoryId(), categoryId))
                                            .findFirst()
                                            .orElse(null);
                                    if (practiceType != null && CollectionUtils.isNotEmpty(questions) && appKeySet.add(lessonId + "|" + categoryId)) {
                                        List<NewHomeworkQuestion> newHomeworkQuestions = buildBasicAppQuestions(questions, practiceType);
                                        naturalSpellingDuration += newHomeworkQuestions.stream()
                                                .mapToInt(NewHomeworkQuestion::getSeconds)
                                                .sum();

                                        NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                                        newHomeworkApp.setCategoryId(categoryId);
                                        newHomeworkApp.setPracticeId(practiceType.getId());
                                        newHomeworkApp.setLessonId(lessonId);
                                        newHomeworkApp.setQuestions(newHomeworkQuestions);
                                        newHomeworkApps.add(newHomeworkApp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            NewHomeworkPracticeContent content = new NewHomeworkPracticeContent();
            content.setType(ObjectiveConfigType.NATURAL_SPELLING);
            content.setApps(newHomeworkApps);
            content.setIncludeSubjective(false);
            vacationHomework.getPractices().add(content);
            vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + naturalSpellingDuration);
        }
    }

    /**
     * 处理趣味配音的作业信息
     */
    private void processDubbing(VacationHomework vacationHomework, Map<String, Object> element, ObjectiveConfigType objectiveConfigType) {
        List<String> dubbingIds = (List<String>) element.get("dubbingIds");
        if (CollectionUtils.isNotEmpty(dubbingIds)) {
            List<Dubbing> dubbingList = new ArrayList<>(dubbingLoaderClient.loadDubbingByDocIds(dubbingIds).values());
            if (CollectionUtils.isNotEmpty(dubbingList)) {
                long dubbingDuration = 0;
                List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
                for (Dubbing dubbing : dubbingList) {
                    List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(dubbing.getPracticeQuestions());
                    if (CollectionUtils.isNotEmpty(questions)) {
                        NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                        newHomeworkApp.setQuestions(buildReadingQuestions(questions, false));
                        newHomeworkApp.setDubbingId(dubbing.getId());
                        newHomeworkApps.add(newHomeworkApp);
                        dubbingDuration += SafeConverter.toLong(dubbing.getVideoSeconds());
                    }
                }
                if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                    NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                    newHomeworkPracticeContent.setApps(newHomeworkApps);
                    newHomeworkPracticeContent.setType(objectiveConfigType);
                    newHomeworkPracticeContent.setIncludeSubjective(false);
                    vacationHomework.getPractices().add(newHomeworkPracticeContent);
                    vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getUpdateAt()) + dubbingDuration);
                }
            }
        }
    }

    /**
     * 处理同步习题的作业信息
     */
    private void processExam(VacationHomework vacationHomework,
                             VacationHomeworkBook vacationHomeworkBook,
                             Map<String, Object> element,
                             String bookId,
                             ObjectiveConfigType objectiveConfigType) {
        Set<String> questionDocIds = new HashSet<>();
        List<String> questionIds = (List<String>) element.get("questionIds");
        if (CollectionUtils.isNotEmpty(questionIds)) {
            questionDocIds = new LinkedHashSet<>(questionIds);
        }
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionByDocIds0(questionDocIds);
        if (MapUtils.isEmpty(questionMap)) {
            logger.error("同步习题题目不存在，题目信息[" + StringUtils.join(questionDocIds, ",") + "]");
            return;
        }
        List<NewQuestion> questions = new ArrayList<>(questionMap.values());
        List<NewHomeworkQuestion> newHomeworkQuestions = buildExamQuestions(objectiveConfigType, questions);
        if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
            newHomeworkPracticeContent.setQuestions(newHomeworkQuestions);
            newHomeworkPracticeContent.setType(objectiveConfigType);
            newHomeworkPracticeContent.setIncludeSubjective(false);
            long examDurations = questions.stream().mapToInt(NewQuestion::getSeconds).sum();
            vacationHomework.getPractices().add(newHomeworkPracticeContent);
            vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + examDurations);

            NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
            newHomeworkBookInfo.setBookId(bookId);
            newHomeworkBookInfo.setQuestions(newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            List<NewHomeworkBookInfo> newHomeworkBookInfoList = new ArrayList<>();
            newHomeworkBookInfoList.add(newHomeworkBookInfo);
            vacationHomeworkBook.getPractices().put(objectiveConfigType, newHomeworkBookInfoList);
        }
    }

    /**
     * 处理绘本的作业信息
     */
    private void processReading(VacationHomework vacationHomework, Map<String, Object> element, ObjectiveConfigType objectiveConfigType) {
        Map<String, Object> reading = (Map<String, Object>) element.get("reading");
        if (MapUtils.isNotEmpty(reading)) {
            String readingDocId = SafeConverter.toString(reading.get("id"));
            PictureBook pictureBook = pictureBookLoaderClient.loadPictureBookByDocIds(Collections.singleton(readingDocId)).get(readingDocId);
            if (pictureBook != null) {
                long readingDuration = SafeConverter.toLong(pictureBook.getSeconds());
                NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                // 跟读题部分
                if (CollectionUtils.isNotEmpty(pictureBook.getOralQuestions())) {
                    List<String> oralQuestionIds = pictureBook.getOralQuestions();
                    // 组装跟读题
                    List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(CollectionUtils.toLinkedHashSet(oralQuestionIds));
                    if (CollectionUtils.isNotEmpty(questions)) {
                        newHomeworkApp.setOralQuestions(buildReadingQuestions(questions, true));
                    }
                }

                // 应试题部分
                List<String> practiceQuestionIds = pictureBook.getPracticeQuestions();
                List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(CollectionUtils.toLinkedHashSet(practiceQuestionIds));
                // 应试题不能为空
                if (CollectionUtils.isEmpty(questions)) {
                    return;
                }
                newHomeworkApp.setQuestions(buildReadingQuestions(questions, false));
                newHomeworkApp.setPictureBookId(pictureBook.getId());
                List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
                newHomeworkApps.add(newHomeworkApp);
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setApps(newHomeworkApps);
                newHomeworkPracticeContent.setType(objectiveConfigType);
                newHomeworkPracticeContent.setIncludeSubjective(false);
                vacationHomework.getPractices().add(newHomeworkPracticeContent);
                vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + readingDuration);
            }
        }
    }

    /**
     * 处理新绘本的作业信息
     */
    private void processLevelReadings(VacationHomework vacationHomework, Map<String, Object> element, ObjectiveConfigType objectiveConfigType) {
        Map<String, Object> reading = (Map<String, Object>) element.get("reading");
        if (MapUtils.isNotEmpty(reading)) {
            String readingId = SafeConverter.toString(reading.get("id"));
            PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadByIds(Collections.singleton(readingId)).get(readingId);
            if (pictureBookPlus != null) {
                Set<String> questionIds = new HashSet<>();
                boolean containExam = CollectionUtils.isNotEmpty(pictureBookPlus.getPracticeQuestions());
                boolean containOral = CollectionUtils.isNotEmpty(pictureBookPlus.getOralQuestions());
                // 习题
                if (containExam) {
                    questionIds.addAll(pictureBookPlus.getPracticeQuestions());
                }
                // 跟读
                if (containOral) {
                    questionIds.addAll(pictureBookPlus.getOralQuestions());
                }
                Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                if (MapUtils.isEmpty(questionMap)) {
                    return;
                }
                // 计算每题的标准分
                Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(questionMap.values()), 40.00);
                NewHomeworkApp nha = new NewHomeworkApp();
                nha.setContainsDubbing(false);
                // 阅读模块时长加上高频词模块时长
                long levelReadingsDuration = SafeConverter.toInt(pictureBookPlus.getRecommendTime(), 300) + 10 * pictureBookPlus.allOftenUsedWords().size();

                if (containExam) {
                    List<NewHomeworkQuestion> newHomeworkQuestions = buildLevelReadingsQuestions(pictureBookPlus.getPracticeQuestions(), questionMap, scoreMap);
                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                        nha.setQuestions(newHomeworkQuestions);
                        levelReadingsDuration = 20 * pictureBookPlus.getPracticeQuestions().size();
                    }
                }

                if (containOral) {
                    List<NewHomeworkQuestion> newHomeworkQuestions = buildLevelReadingsQuestions(pictureBookPlus.getOralQuestions(), questionMap, scoreMap);
                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                        nha.setOralQuestions(newHomeworkQuestions);
                        levelReadingsDuration += SafeConverter.toInt(pictureBookPlus.getOralSeconds(), 300);
                    }
                }

                nha.setPictureBookId(pictureBookPlus.getId());
                List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
                newHomeworkApps.add(nha);
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setApps(newHomeworkApps);
                newHomeworkPracticeContent.setType(objectiveConfigType);
                newHomeworkPracticeContent.setIncludeSubjective(false);
                vacationHomework.getPractices().add(newHomeworkPracticeContent);
                vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + levelReadingsDuration);
            }
        }
    }

    /**
     * 处理重难点视频的作业信息
     */
    private void processKeyPoints(VacationHomework vacationHomework, Map<String, Object> element) {
        String videoId = SafeConverter.toString(element.get("videoId"));
        List<String> questionIds = (List<String>) element.get("questionIds");
        Video video = videoLoaderClient.loadVideoByDocIds(Collections.singleton(videoId)).get(videoId);
        List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionIds);
        if (video != null && CollectionUtils.isNotEmpty(newQuestionList)) {
            NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
            newHomeworkApp.setQuestions(buildExamQuestions(ObjectiveConfigType.KEY_POINTS, newQuestionList));
            newHomeworkApp.setVideoId(video.getId());
            newHomeworkApps.add(newHomeworkApp);
            newHomeworkPracticeContent.setApps(newHomeworkApps);
            newHomeworkPracticeContent.setType(ObjectiveConfigType.KEY_POINTS);
            newHomeworkPracticeContent.setIncludeSubjective(false);
            int keyPointsDuration = newQuestionList.stream().mapToInt(NewQuestion::getSeconds).sum();
            vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + keyPointsDuration);
            vacationHomework.getPractices().add(newHomeworkPracticeContent);
        }
    }

    /**
     * 处理课文读本题的作业信息
     */
    private void processReadRecite(VacationHomework vacationHomework, Map<String, Object> element) {
        List<String> questionIds = (List<String>) element.get("questionIds");
        List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionIds);
        int answerWay = SafeConverter.toInt(element.get("answerWay"));
        if (CollectionUtils.isNotEmpty(newQuestionList)) {
            doHomeworkProcessor.handReadRecite(newQuestionList, true);
            newQuestionList.sort(Comparator.comparingInt(a -> SafeConverter.toInt(a.getParagraph())));
            Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestionList, 100.00);
            List<NewHomeworkQuestion> result = new ArrayList<>();
            for (NewQuestion q : newQuestionList) {
                NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
                newHomeworkQuestion.setQuestionId(q.getId());
                newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                newHomeworkQuestion.setScore(scoreMap.get(q.getId()));
                newHomeworkQuestion.setSeconds(q.getSeconds());
                newHomeworkQuestion.setSubmitWay(q.getSubmitWays());
                List<List<Integer>> answerWays = new ArrayList<>();
                answerWays.add(Collections.singletonList(answerWay));
                newHomeworkQuestion.setAnswerWay(answerWays);
                result.add(newHomeworkQuestion);
            }
            int duration = newQuestionList.stream().mapToInt(NewQuestion::getSeconds).sum();
            NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
            newHomeworkPracticeContent.setQuestions(result);
            newHomeworkPracticeContent.setIncludeSubjective(true);
            newHomeworkPracticeContent.setType(ObjectiveConfigType.READ_RECITE);
            vacationHomework.getPractices().add(newHomeworkPracticeContent);
            vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + duration);
        }
    }

    /**
     * 处理新课文读本的作业信息
     */
    private void processNewReadRecite(VacationHomework vacationHomework, Map<String, Object> element, ObjectiveConfigType objectiveConfigType) {
        List<Map<String, Object>> packages = (List<Map<String, Object>>) element.get("packages");
        if (CollectionUtils.isNotEmpty(packages)) {
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            long newReadReciteDuration = 0;
            for (Map<String, Object> questionPackage : packages) {
                String questionBoxId = SafeConverter.toString(questionPackage.get("id"));
                String lessonId = SafeConverter.toString(questionPackage.get("lessonId"));
                List<String> questionIds = (List<String>) questionPackage.get("questionIds");
                List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                if (StringUtils.isNotEmpty(questionBoxId) && CollectionUtils.isNotEmpty(questions)) {
                    NewQuestion firstQuestion = questions.iterator().next();
                    QuestionBoxType questionBoxType = null;
                    int contentTypeId = SafeConverter.toInt(firstQuestion.getContentTypeId());
                    if (contentTypeId == 1010014) {
                        questionBoxType = QuestionBoxType.READ;
                    } else if (contentTypeId == 1010015) {
                        questionBoxType = QuestionBoxType.RECITE;
                    }
                    if (questionBoxType != null) {
                        Map<String, List<Long>> qidSentenceIdsMap = questions
                                .stream()
                                .collect(Collectors.toMap(NewQuestion::getDocId, NewQuestion::getSentenceIds));

                        List<Long> chineseSentenceIds = questions
                                .stream()
                                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                                .map(NewQuestion::getSentenceIds)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
                        //句子所在的章节号
                        Map<Long, Integer> sentenceIdSection = chineseSentences
                                .stream()
                                .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraphContinuous));
                        //句子所在章节的段落号
                        Map<Long, Integer> sentenceIdParagraph = chineseSentences
                                .stream()
                                .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraph));
                        //题的章节号
                        Map<String, Integer> qidSectionMap = new HashMap<>();
                        //题的段落号
                        Map<String, Integer> qidParagraphMap = new HashMap<>();

                        if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                            for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                                String questionDocId = entry.getKey();
                                List<Long> sentenceIds = entry.getValue();
                                Long sentenceId = 0L;
                                if (CollectionUtils.isNotEmpty(sentenceIds)) {
                                    sentenceId = sentenceIds.iterator().next();
                                }
                                qidSectionMap.put(questionDocId, sentenceIdSection.get(sentenceId));
                                qidParagraphMap.put(questionDocId, sentenceIdParagraph.get(sentenceId));
                            }
                        }

                        Comparator<Map<String, Object>> comparator = Comparator.comparingInt(a -> SafeConverter.toInt(a.get("sectionNumber")));
                        comparator = comparator.thenComparingInt(a -> SafeConverter.toInt(a.get("paragraphNumber")));
                        List<Map<String, Object>> questionMapperList = questions.stream()
                                .map(newQuestion -> {
                                    Map<String, Object> question = new HashMap<>();
                                    question.put("questionId", newQuestion.getId());
                                    question.put("sectionNumber", qidSectionMap.get(newQuestion.getDocId()));
                                    question.put("paragraphNumber", qidParagraphMap.get(newQuestion.getDocId()));
                                    return question;
                                })
                                .sorted(comparator)
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(questionMapperList)) {
                            Map<String, NewQuestion> questionMap = questions
                                    .stream()
                                    .collect(Collectors.toMap(NewQuestion::getId, Function.identity(), (u, v) -> v));
                            questions = questionMapperList
                                    .stream()
                                    .map(map -> questionMap.get(SafeConverter.toString(map.get("questionId"))))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            List<NewHomeworkQuestion> newQuestionList = buildExamQuestions(objectiveConfigType, questions);
                            newReadReciteDuration += newQuestionList.stream().mapToInt(NewHomeworkQuestion::getSeconds).sum();
                            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                            newHomeworkApp.setQuestionBoxId(questionBoxId);
                            newHomeworkApp.setQuestionBoxType(questionBoxType);
                            newHomeworkApp.setLessonId(lessonId);
                            newHomeworkApp.setQuestions(newQuestionList);
                            newHomeworkApps.add(newHomeworkApp);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setApps(newHomeworkApps);
                newHomeworkPracticeContent.setIncludeSubjective(true);
                newHomeworkPracticeContent.setType(objectiveConfigType);
                vacationHomework.getPractices().add(newHomeworkPracticeContent);
                vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + newReadReciteDuration);
            }
        }
    }

    /**
     * 处理其它类型的作业信息
     */
    private void processOthers(VacationHomework vacationHomework, Map<String, Object> element, ObjectiveConfigType objectiveConfigType) {
        List<String> questionIds = (List<String>) element.get("questionIds");
        if (objectiveConfigType == ObjectiveConfigType.INTERESTING_PICTURE) {
            Map<String, Object> reading = (Map<String, Object>) element.get("reading");
            if (MapUtils.isNotEmpty(reading)) {
                questionIds = (List<String>) reading.get("questionIds");
            }
        }
        List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionIds);
        if (CollectionUtils.isNotEmpty(newQuestionList)) {
            List<NewHomeworkQuestion> newHomeworkQuestions = buildExamQuestions(objectiveConfigType, newQuestionList);
            NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
            newHomeworkPracticeContent.setQuestions(newHomeworkQuestions);
            newHomeworkPracticeContent.setType(objectiveConfigType);
            newHomeworkPracticeContent.setIncludeSubjective(false);
            //判断类型是否为口算
            if (objectiveConfigType == ObjectiveConfigType.MENTAL_ARITHMETIC) {
                newHomeworkPracticeContent.setTimeLimit(MentalArithmeticTimeLimit.ZERO);
                newHomeworkPracticeContent.setMentalAward(false);
                newHomeworkPracticeContent.setRecommend(false);
            }
            int examDuration = newQuestionList.stream().mapToInt(NewQuestion::getSeconds).sum();
            vacationHomework.getPractices().add(newHomeworkPracticeContent);
            vacationHomework.setDuration(SafeConverter.toLong(vacationHomework.getDuration()) + examDuration);
        }
    }

    /**
     * 处理阅读绘本的题目部分
     */
    private List<NewHomeworkQuestion> buildReadingQuestions(List<NewQuestion> questions, boolean isOral) {
        Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(questions, 100.00);
        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (NewQuestion q : questions) {
            NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
            newHomeworkQuestion.setQuestionId(q.getId());
            newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
            newHomeworkQuestion.setSeconds(q.getSeconds());
            newHomeworkQuestion.setSubmitWay(q.getSubmitWays());
            if (Objects.equals(isOral, Boolean.TRUE)) {
                newHomeworkQuestion.setScore(100D);
            } else {
                newHomeworkQuestion.setScore(scoreMap.get(q.getId()));
            }
            result.add(newHomeworkQuestion);
        }
        return result;
    }

    /**
     * 处理新绘本阅读的题目部分
     */
    private List<NewHomeworkQuestion> buildLevelReadingsQuestions(List<String> qids, Map<String, NewQuestion> questionMap, Map<String, Double> scoreMap) {
        if (CollectionUtils.isEmpty(qids)) {
            return Collections.emptyList();
        }

        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (String qid : qids) {
            NewQuestion newQuestion = questionMap.get(qid);
            if (newQuestion != null) {
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                nhq.setQuestionId(newQuestion.getId());
                // 题目版本号
                nhq.setQuestionVersion(newQuestion.getOlUpdatedAt() != null ? newQuestion.getOlUpdatedAt().getTime() : newQuestion.getVersion());
                nhq.setScore(scoreMap.get(newQuestion.getId()));
                nhq.setSeconds(newQuestion.getSeconds());
                nhq.setSubmitWay(newQuestion.getSubmitWays());
                result.add(nhq);
            }
        }
        return result;
    }

    /**
     * 同步习题类型的作业形式
     */
    private List<NewHomeworkQuestion> buildExamQuestions(ObjectiveConfigType objectiveConfigType, List<NewQuestion> questions) {
        // 计算分数
        Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(questions, 100.00);
        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (NewQuestion q : questions) {
            NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
            newHomeworkQuestion.setQuestionId(q.getId());
            newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
            if (ObjectiveConfigType.ORAL_PRACTICE == objectiveConfigType) {
                newHomeworkQuestion.setScore(100.0);
            } else {
                newHomeworkQuestion.setScore(scoreMap.get(q.getId()));
            }
            newHomeworkQuestion.setScore(scoreMap.get(q.getId()));
            newHomeworkQuestion.setSeconds(q.getSeconds());
            newHomeworkQuestion.setSubmitWay(q.getSubmitWays());
            result.add(newHomeworkQuestion);
        }
        return result;
    }

    private List<NewHomeworkQuestion> buildBasicAppQuestions(List<NewQuestion> questions, PracticeType practiceType) {
        Map<String, Double> qScoreMap = questionLoaderClient.parseExamScoreByQuestions(questions, 100.00);
        // 组装基础训练题的部分 NewHomeworkQuestion
        List<NewHomeworkQuestion> homeworkQuestions = new ArrayList<>();
        for (NewQuestion q : questions) {
            NewHomeworkQuestion homeworkQuestion = new NewHomeworkQuestion();
            if (q != null) {
                homeworkQuestion.setQuestionId(q.getId());
                homeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                homeworkQuestion.setSeconds(q.getSeconds());
                //跟读类应用的标准分都是100，因为是由语音引擎打分
                if (practiceType.getNeedRecord()) {
                    homeworkQuestion.setScore(100.00);
                } else {
                    homeworkQuestion.setScore(qScoreMap.get(q.getId()));
                }
                homeworkQuestion.setSubmitWay(q.getSubmitWays());
                homeworkQuestions.add(homeworkQuestion);
            }
        }
        return homeworkQuestions;
    }
}
