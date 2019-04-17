package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.stone.data.ImageTextRhyme;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.WordsPractice;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/11/26 下午6:22
 */
@Named
public class NewHomeworkContentProcessWordTeachAndPractice extends NewHomeworkContentProcessTemplate{

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.WORD_TEACH_AND_PRACTICE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkAppList = new ArrayList<>();
            Set<String> newHomeworkAppKeySet = new HashSet<>();
            Map<BookInfoMapper, List<String>> bookStoneIdsMap = new LinkedHashMap<>();

            Set<Long> groupIds = context.getGroupIds();
            for (Map app : apps) {
                String stoneDataId = SafeConverter.toString(app.get("stoneDataId"));
                if (StringUtils.isBlank(stoneDataId)) {
                    return contentError(context, objectiveConfigType);
                }
                List<Map> practiceTypes = (List<Map>) app.get("practiceTypes");
                if (CollectionUtils.isEmpty(practiceTypes)) {
                    return contentError(context, objectiveConfigType);
                }

                Set<WordTeachModuleType> practiceTypesSet = new HashSet<>();
                for (Map practiceTypeMap : practiceTypes) {
                    String type = SafeConverter.toString(practiceTypeMap.get("type"));
                    if (type == null) {
                        continue;
                    }
                    WordTeachModuleType practiceType = WordTeachModuleType.of(type);
                    if (practiceType != null) {
                        practiceTypesSet.add(practiceType);
                    }
                }
                if (CollectionUtils.isEmpty(practiceTypesSet)) {
                    return contentError(context, objectiveConfigType);
                }

                // 获取题包
                List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneDataId));
                if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
                    return contentError(context, objectiveConfigType);
                }

                WordsPractice wordsPractice = stoneBufferedDataList.get(0).getWordsPractice();
                Set<String> questionIds = new HashSet<>();
                //字词训练
                boolean containWordExercise = practiceTypesSet.contains(WordTeachModuleType.WORDEXERCISE) && wordsPractice.getWordExercise() != null;
                if (containWordExercise) {
                    questionIds.addAll(wordsPractice.getWordExercise().getQuestionIds());
                }
                //图文入韵
                boolean containImageText = practiceTypesSet.contains(WordTeachModuleType.IMAGETEXTRHYME) && wordsPractice.getImageText() != null;
                if (containImageText) {
                    for (ImageTextRhyme imageTextRhyme : wordsPractice.getImageText().getImageTextRhymes()) {
                        questionIds.addAll(imageTextRhyme.getQuestionIds());
                    }
                }
                boolean containChineseCharacterCulture = practiceTypesSet.contains(WordTeachModuleType.CHINESECHARACTERCULTURE) && wordsPractice.getChineseCharacterCulture() != null;
                // 题目信息
                Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionByDocIds(questionIds)
                        .stream()
                        .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
                if (MapUtils.isEmpty(questionMap) && !containChineseCharacterCulture) {
                    return contentError(context, objectiveConfigType);
                }

                NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                //字词训练模块
                if (containWordExercise) {
                    List<String> wordExerciseQuestionIds = wordsPractice.getWordExercise().getQuestionIds();
                    List<NewQuestion> wordExerciseQuestions = new ArrayList<>();
                    for (String questionId : wordExerciseQuestionIds) {
                        if (questionMap.get(questionId) != null) {
                            wordExerciseQuestions.add(questionMap.get(questionId));
                        }
                    }

                    // 计算每题的标准分
                    Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(wordExerciseQuestions, 100.00);
                    List<NewHomeworkQuestion> newHomeworkQuestions = buildQuestions(wordExerciseQuestionIds, questionMap, scoreMap);
                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                        newHomeworkApp.setWordExerciseQuestions(newHomeworkQuestions);
                    }
                }
                //图文入韵模块
                List<ImageTextRhymeHomework> imageTextRhymeQuestionList = new ArrayList<>();
                if (containImageText) {
                    for (ImageTextRhyme imageTextRhyme : wordsPractice.getImageText().getImageTextRhymes()) {
                        ImageTextRhymeHomework imageTextRhymeHomework = new ImageTextRhymeHomework();
                        String chapterId = imageTextRhyme.getUuid();
                        List<NewHomeworkQuestion> chapterQuestions = buildQuestions(imageTextRhyme.getQuestionIds(), questionMap, null);
                        if (CollectionUtils.isNotEmpty(chapterQuestions)) {
                            imageTextRhymeHomework.setChapterId(chapterId);
                            imageTextRhymeHomework.setTitle(imageTextRhyme.getTitle());
                            imageTextRhymeHomework.setImageUrl(imageTextRhyme.getImageUrl());
                            imageTextRhymeHomework.setChapterQuestions(chapterQuestions);
                            imageTextRhymeQuestionList.add(imageTextRhymeHomework);
                        }
                    }
                    newHomeworkApp.setImageTextRhymeQuestions(imageTextRhymeQuestionList);
                }
                // 汉字文化模块
                if (containChineseCharacterCulture) {
                    List<String> chineseCharacterCultureCourseIdList = wordsPractice.getChineseCharacterCulture().getCourseIds();
                    newHomeworkApp.setChineseCharacterCultureCourseIds(CollectionUtils.isNotEmpty(chineseCharacterCultureCourseIdList) ? chineseCharacterCultureCourseIdList : null);
                }

                Map<String, Object> book = (Map) app.get("book");
                String lessonId = SafeConverter.toString(app.get("lessonId"));
                String sectionId = "";
                if (book != null) {
                    sectionId = SafeConverter.toString(book.get("sectionId"));
                    BookInfoMapper bookInfoMapper = new BookInfoMapper();
                    bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId")));
                    bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId")));
                    bookInfoMapper.setLessonId(SafeConverter.toString(book.get("lessonId")));
                    lessonId = bookInfoMapper.getLessonId();
                    bookInfoMapper.setSectionId(sectionId);
                    bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId")));
                    bookStoneIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(stoneDataId);
                }
                newHomeworkApp.setLessonId(lessonId);
                newHomeworkApp.setSectionId(sectionId);
                newHomeworkApp.setStoneDataId(stoneDataId);
                if (newHomeworkAppKeySet.contains(stoneDataId)) {
                    logger.warn("Found duplicate app, appKey : {}, homeworkSource : {}", stoneDataId, JsonUtils.toJson(context.getSource()));
                } else {
                    newHomeworkAppKeySet.add(stoneDataId);
                    newHomeworkAppList.add(newHomeworkApp);
                }
            }
            //拼装groupPractices信息
            if (CollectionUtils.isNotEmpty(newHomeworkAppList)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setApps(newHomeworkAppList);
                newHomeworkPracticeContent.setType(objectiveConfigType);
                newHomeworkPracticeContent.setIncludeSubjective(false);

                if (CollectionUtils.isNotEmpty(groupIds)) {
                    for (Long groupId : groupIds) {
                        Map<Long, List<NewHomeworkPracticeContent>> groupPractices = context.getGroupPractices();
                        List<NewHomeworkPracticeContent> newHomeworkPracticeContents = groupPractices
                                .getOrDefault(groupId, new ArrayList<>());
                        newHomeworkPracticeContents.add(newHomeworkPracticeContent);
                        groupPractices.put(groupId, newHomeworkPracticeContents);
                    }
                }
            }
            //拼装groupPracticesBooksMap信息
            if (MapUtils.isNotEmpty(bookStoneIdsMap)) {
                List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookStoneIdsMap.entrySet()
                        .stream()
                        .map(entry -> {
                            NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                            BookInfoMapper bookInfoMapper = entry.getKey();
                            newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                            newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                            newHomeworkBookInfo.setLessonId(bookInfoMapper.getLessonId());
                            newHomeworkBookInfo.setSectionId(bookInfoMapper.getSectionId());
                            newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                            newHomeworkBookInfo.setStoneIds(entry.getValue());
                            return newHomeworkBookInfo;
                        })
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                    if (CollectionUtils.isNotEmpty(groupIds)) {
                        for (Long groupId : groupIds) {
                            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context
                                    .getGroupPracticesBooksMap()
                                    .getOrDefault(groupId, new LinkedHashMap<>());
                            practiceBooksMap.put(objectiveConfigType, newHomeworkBookInfoList);
                            context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                        }
                    }
                }
            }
        }
        return context;
    }

    private List<NewHomeworkQuestion> buildQuestions(List<String> qids, Map<String, NewQuestion> questionMap, Map<String, Double> scoreMap) {
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
                if (MapUtils.isNotEmpty(scoreMap)) {
                    nhq.setScore(scoreMap.get(newQuestion.getId()));
                } else {
                    nhq.setScore(100D);
                }
                nhq.setSeconds(newQuestion.getSeconds());
                nhq.setSubmitWay(newQuestion.getSubmitWays());
                result.add(nhq);
            }
        }
        return result;
    }
}
