package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
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

/**
 * Created by tanguohong on 16/7/7.
 */
@Named
public class NewHomeworkContentProcessBasicApp extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.BASIC_APP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            Set<String> newHomeworkAppKeySet = new HashSet<>();
            Map<BookInfoMapper, List<String>> bookAppIdsMap = new LinkedHashMap<>();
            for (Map app : apps) {
                NewHomeworkApp nha = new NewHomeworkApp();
                String categoryId = SafeConverter.toString(app.get("categoryId"));
                long practiceId = SafeConverter.toLong(app.get("practiceId"));
                String lessonId = SafeConverter.toString(app.get("lessonId"));
                if (StringUtils.isBlank(categoryId) || practiceId == 0 || StringUtils.isBlank(lessonId)) {
                    return contentError(context, objectiveConfigType);
                }
                NatureSpellingType natureSpellingType = NatureSpellingType.of(SafeConverter.toInt(categoryId));
                // 校验自然拼读categoryId是否错误(不属于自然拼读练习类型)
                if (ObjectiveConfigType.NATURAL_SPELLING == objectiveConfigType && natureSpellingType == null) {
                    logger.error("NaturalSpelling content error, categoryId:{}", categoryId);
                    return contentError(context, objectiveConfigType);
                }
                // 校验基础练习categoryId是否错误(属于自然拼读练习类型)
                else if (ObjectiveConfigType.BASIC_APP == objectiveConfigType && natureSpellingType != null) {
                    logger.error("BasicApp content error, categoryId:{}", categoryId);
                    return contentError(context, objectiveConfigType);
                }
                PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                if (practiceType == null) {
                    return contentError(context, objectiveConfigType);
                }
                List<Map> appQuestions = JsonUtils.fromJsonToList(JsonUtils.toJson(app.get("questions")), Map.class);
                if (app.containsKey("book")) {
                    Map<String, Object> bookMap = (Map) app.get("book");
                    if (bookMap != null) {
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(bookMap.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(bookMap.get("unitId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                        String appId = lessonId + "|" + categoryId;
                        bookAppIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(appId);
                    }
                }

                Map<String, NewHomeworkQuestion> questionMap = new LinkedHashMap<>();
                List<String> questionIds = new ArrayList<>();
                for (Map appQuestion : appQuestions) {
                    NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                    String questionId = SafeConverter.toString(appQuestion.get("questionId"));
                    nhq.setQuestionId(questionId);
                    if (appQuestion.containsKey("seconds"))
                        nhq.setSeconds(SafeConverter.toInt(appQuestion.get("seconds")));
                    if (appQuestion.containsKey("answerType"))
                        nhq.setAnswerType(SafeConverter.toInt(appQuestion.get("answerType")));
                    if (appQuestion.containsKey("submitWay"))
                        nhq.setSubmitWay((List<List<Integer>>) appQuestion.get("submitWay"));
                    questionMap.put(questionId, nhq);
                    questionIds.add(questionId);
                }

                if (CollectionUtils.isEmpty(questionIds)) {
                    return contentError(context, objectiveConfigType);
                }

                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                Map<String, Double> qscoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(newQuestionMap.values()), 100.00);
                List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
                for (String qid : questionMap.keySet()) {
                    if (qscoreMap.containsKey(qid)) {
                        NewHomeworkQuestion newHomeworkQuestion = questionMap.get(qid);
                        //跟读类应用的标准分都是100，因为是由语音引擎打分
                        if (practiceType.getNeedRecord()) {
                            newHomeworkQuestion.setScore(100.00);
                        } else {
                            newHomeworkQuestion.setScore(qscoreMap.get(qid));
                        }
                        // 题目版本号
                        NewQuestion q = newQuestionMap.getOrDefault(qid, null);
                        if (q != null) {
                            newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                        }
                        newHomeworkQuestions.add(newHomeworkQuestion);
                    } else {
                        return contentError(context, objectiveConfigType);
                    }
                }
                nha.setQuestions(newHomeworkQuestions);
                nha.setPracticeId(practiceId);
                nha.setLessonId(lessonId);
                nha.setCategoryId(SafeConverter.toInt(categoryId));

                String appKey = nha.getLessonId() + "-" + nha.getCategoryId();
                if (newHomeworkAppKeySet.contains(appKey)) {
                    logger.warn("Found duplicate app, appKey : {}, homeworkSource : {}", appKey, JsonUtils.toJson(context.getSource()));
                } else {
                    newHomeworkAppKeySet.add(appKey);
                    newHomeworkApps.add(nha);
                }
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                nhpc.setType(objectiveConfigType);
                nhpc.setApps(newHomeworkApps);
                for (Long groupId : context.getGroupIds()) {
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                    newHomeworkPracticeContents.add(nhpc);
                    context.getGroupPractices().put(groupId, newHomeworkPracticeContents);
                }
            }
            // 将bookAppMap转为NewHomeworkBookInfo
            List<NewHomeworkBookInfo> newHomeworkBookInfoList = new ArrayList<>();
            bookAppIdsMap.forEach((book, appIds) -> {
                NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                newHomeworkBookInfo.setBookId(book.getBookId());
                newHomeworkBookInfo.setUnitId(book.getUnitId());
                newHomeworkBookInfo.setObjectiveId(book.getObjectiveId());
                newHomeworkBookInfo.setAppIds(appIds);
                newHomeworkBookInfoList.add(newHomeworkBookInfo);
            });
            if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                for (Long groupId : context.getGroupIds()) {
                    LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap()
                            .getOrDefault(groupId, new LinkedHashMap<>());
                    practiceBooksMap.put(objectiveConfigType, newHomeworkBookInfoList);
                    context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                }
            }
        }
        return context;
    }
}
