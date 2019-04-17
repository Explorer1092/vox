package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 生字认读
 * @author: Mr_VanGogh
 * @date: 2018/7/17 下午6:11
 */
@Named
public class NewHomeworkContentProcessWordRecognitionAndReading extends NewHomeworkContentProcessTemplate {

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.WORD_RECOGNITION_AND_READING;
    }

    @Override
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkAppList = new ArrayList<>();
            Set<String> newHomeworkAppKeySet = new HashSet<>();
            Map<BookInfoMapper, List<String>> bookQuestionBoxIdsMap = new LinkedHashMap<>();

            Set<Long> groupIds = context.getGroupIds();
            for (Map app : apps) {
                Map<String, Object> book = (Map) app.get("book");
                String lessonId = SafeConverter.toString(app.get("lessonId"));
                String questionBoxId = SafeConverter.toString(app.get("questionBoxId"));
                if (StringUtils.isBlank(questionBoxId)) {
                    return contentError(context, objectiveConfigType);
                }
                // 拼装Book相关信息
                if (book != null) {
                    BookInfoMapper bookInfoMapper = new BookInfoMapper();
                    bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                    bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                    bookInfoMapper.setLessonId(SafeConverter.toString(book.get("lessonId"), ""));
                    bookInfoMapper.setSectionId(SafeConverter.toString(book.get("sectionId"), ""));
                    bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                    bookQuestionBoxIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(questionBoxId);
                    lessonId = bookInfoMapper.getLessonId();
                }

                List<Map> questionsList = JsonUtils.fromJsonToList(JsonUtils.toJson(app.get("questions")), Map.class);
                Map<String, NewHomeworkQuestion> newHomeworkQuestionMap = new LinkedHashMap<>();
                List<String> questionIdList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(questionsList)) {
                    for (Map appQuestion : questionsList) {
                        NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
                        String questionId = SafeConverter.toString(appQuestion.get("questionId"));
                        newHomeworkQuestion.setQuestionId(questionId);
                        if (appQuestion.containsKey("seconds")) {
                            newHomeworkQuestion.setSeconds(SafeConverter.toInt(appQuestion.get("seconds")));
                        }
                        if (appQuestion.containsKey("answerType")) {
                            newHomeworkQuestion.setAnswerType(SafeConverter.toInt(appQuestion.get("answerType")));
                        }
                        if (appQuestion.containsKey("submitWay")) {
                            newHomeworkQuestion.setSubmitWay((List<List<Integer>>) appQuestion.get("submitWay"));
                        }
                        context.setIncludeSubjective(false);
                        newHomeworkQuestionMap.put(questionId, newHomeworkQuestion);
                        questionIdList.add(questionId);
                    }
                }
                if (CollectionUtils.isEmpty(questionIdList)) {
                    continue;
                }

                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIdList);
                List<NewHomeworkQuestion> newHomeworkQuestionList = new ArrayList<>();
                for (String questionId : questionIdList) {
                    NewHomeworkQuestion newHomeworkQuestion = newHomeworkQuestionMap.get(questionId);
                    // 题目版本号
                    NewQuestion newQuestion = newQuestionMap.getOrDefault(questionId, null);
                    //生字认读的标准分都是100
                    if (newHomeworkQuestion != null) {
                        newHomeworkQuestion.setScore(100.00);
                        if (newQuestion != null) {
                            newHomeworkQuestion.setQuestionVersion(
                                    newQuestion.getOlUpdatedAt() != null ?
                                            newQuestion.getOlUpdatedAt().getTime() :
                                            newQuestion.getVersion());
                        }
                    }
                    newHomeworkQuestionList.add(newHomeworkQuestion);
                }

                if (CollectionUtils.isEmpty(newHomeworkQuestionList)) {
                    continue;
                }

                NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                newHomeworkApp.setLessonId(lessonId);
                newHomeworkApp.setQuestionBoxId(questionBoxId);
                newHomeworkApp.setQuestions(newHomeworkQuestionList);

                if (newHomeworkAppKeySet.contains(questionBoxId)) {
                    logger.warn("Found duplicate app, appKey : {}, homeworkSource : {}", questionBoxId, JsonUtils.toJson(context.getSource()));
                } else {
                    newHomeworkAppKeySet.add(questionBoxId);
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
            if (MapUtils.isNotEmpty(bookQuestionBoxIdsMap)) {
                List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookQuestionBoxIdsMap.entrySet()
                        .stream()
                        .map(entry -> {
                            NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                            BookInfoMapper bookInfoMapper = entry.getKey();
                            newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                            newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                            newHomeworkBookInfo.setLessonId(bookInfoMapper.getLessonId());
                            newHomeworkBookInfo.setSectionId(bookInfoMapper.getSectionId());
                            newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                            newHomeworkBookInfo.setQuestionBoxIds(entry.getValue());
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
}
