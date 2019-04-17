package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/3/1
 */
@Named
public class NewHomeworkContentProcessSpecialApp extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.SPECIAL_APP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> groupPractice, ObjectiveConfigType objectiveConfigType) {
        if (MapUtils.isNotEmpty(groupPractice)) {
            List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(groupPractice.get("apps")), Map.class);
            if (CollectionUtils.isNotEmpty(apps)) {
                Map<Long, List<NewHomeworkApp>> groupNewHomeworkApps = new LinkedHashMap<>();
                Map<String, NewHomeworkApp> newHomeworkAppMap = new LinkedHashMap<>();
                Map<Long, Map<BookInfoMapper, List<String>>> groupBookAppIdsMap = new LinkedHashMap<>();
                for (Map app : apps) {
                    Long groupId = SafeConverter.toLong(app.get("groupId"));
                    if (context.getGroupIds().contains(groupId)) {
                        NewHomeworkApp nha = new NewHomeworkApp();
                        String categoryId = SafeConverter.toString(app.get("categoryId"));
                        long practiceId = SafeConverter.toLong(app.get("practiceId"));
                        String lessonId = SafeConverter.toString(app.get("lessonId"));
                        if (StringUtils.isBlank(categoryId) || practiceId == 0 || StringUtils.isBlank(lessonId)) {
                            return contentError(context, objectiveConfigType);
                        }
                        PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                        if (practiceType == null) {
                            return contentError(context, objectiveConfigType);
                        }
                        nha.setPracticeId(practiceId);
                        nha.setLessonId(lessonId);
                        nha.setCategoryId(SafeConverter.toInt(categoryId));

                        if (app.containsKey("book")) {
                            Map<String, Object> bookMap = (Map) app.get("book");
                            if (bookMap != null) {
                                BookInfoMapper bookInfoMapper = new BookInfoMapper();
                                bookInfoMapper.setBookId(SafeConverter.toString(bookMap.get("bookId"), ""));
                                bookInfoMapper.setUnitId(SafeConverter.toString(bookMap.get("unitId"), ""));
                                bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                                String appId = lessonId + "|" + categoryId;
                                groupBookAppIdsMap.computeIfAbsent(groupId, k -> new LinkedHashMap<>()).computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(appId);
                            }
                        }

                        Map<String, NewHomeworkQuestion> questionMap = new LinkedHashMap<>();
                        List<Map> appQuestions = JsonUtils.fromJsonToList(JsonUtils.toJson(app.get("questions")), Map.class);
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
                        }

                        if (MapUtils.isEmpty(questionMap)) {
                            return contentError(context, objectiveConfigType);
                        }

                        String appKey = groupId + "-" + nha.getLessonId() + "-" + nha.getCategoryId();
                        if (newHomeworkAppMap.containsKey(appKey)) {
                            // 听说查缺补漏两个题包里面可能有相同key的app，把这两个相同key的app合成一个
                            nha = newHomeworkAppMap.get(appKey);
                            Map<String, NewHomeworkQuestion> backUpHomeworkQuestionMap = questionMap;
                            questionMap = new LinkedHashMap<>();
                            // 合并时把第一个包的questions放在前面
                            for (NewHomeworkQuestion newHomeworkQuestion : nha.getQuestions()) {
                                questionMap.put(newHomeworkQuestion.getQuestionId(), newHomeworkQuestion);
                            }
                            questionMap.putAll(backUpHomeworkQuestionMap);
                        }

                        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionMap.keySet());
                        Map<String, Double> qScoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(newQuestionMap.values()), 100.00);
                        List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
                        for (String qid : questionMap.keySet()) {
                            if (qScoreMap.containsKey(qid)) {
                                NewHomeworkQuestion newHomeworkQuestion = questionMap.get(qid);
                                //跟读类应用的标准分都是100，因为是由语音引擎打分
                                if (practiceType.getNeedRecord()) {
                                    newHomeworkQuestion.setScore(100.00);
                                } else {
                                    newHomeworkQuestion.setScore(qScoreMap.get(qid));
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
                        if (!newHomeworkAppMap.containsKey(appKey)) {
                            groupNewHomeworkApps.computeIfAbsent(groupId, k -> new ArrayList<>()).add(nha);
                            newHomeworkAppMap.put(appKey, nha);
                        }
                        // else do nothing, just update nha's questions
                    }
                }
                if (MapUtils.isNotEmpty(groupNewHomeworkApps)) {
                    for (Long groupId : context.getGroupIds()) {

                        List<NewHomeworkApp> newHomeworkApps = groupNewHomeworkApps.get(groupId);
                        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                            NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                            nhpc.setType(objectiveConfigType);
                            nhpc.setApps(newHomeworkApps);
                            List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                            newHomeworkPracticeContents.add(nhpc);
                            context.getGroupPractices().put(groupId, newHomeworkPracticeContents);
                        }

                        List<NewHomeworkBookInfo> newHomeworkBookInfoList = groupBookAppIdsMap
                                .getOrDefault(groupId, new LinkedHashMap<>())
                                .entrySet()
                                .stream()
                                .map(entry -> {
                                    NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                                    BookInfoMapper bookInfoMapper = entry.getKey();
                                    newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                                    newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                                    newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                                    newHomeworkBookInfo.setAppIds(entry.getValue());
                                    return newHomeworkBookInfo;
                                })
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap()
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
