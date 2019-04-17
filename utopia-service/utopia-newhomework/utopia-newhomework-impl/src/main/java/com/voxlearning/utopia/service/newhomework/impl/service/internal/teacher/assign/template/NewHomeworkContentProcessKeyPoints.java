package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
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
 * Created by tanguohong on 2016/11/24.
 */
@Named
public class NewHomeworkContentProcessKeyPoints extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.KEY_POINTS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            Set<String> newHomeworkAppKeySet = new HashSet<>();
            Set<String> allQuestionIdSet = new HashSet<>();
            Map<BookInfoMapper, List<String>> bookVideoIdsMap = new LinkedHashMap<>();
            for (Map app : apps) {
                NewHomeworkApp nha = new NewHomeworkApp();
                String videoId = SafeConverter.toString(app.get("videoId"));
                if (StringUtils.isBlank(videoId)) {
                    return contentError(context, objectiveConfigType);
                }

                List<Map> appQuestions = JsonUtils.fromJsonToList(JsonUtils.toJson(app.get("questions")), Map.class);

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
                    continue;
                }

                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                Map<String, Double> qscoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(newQuestionMap.values()), 100.00);
                List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
                for (String qid : questionMap.keySet()) {
                    if (qscoreMap.containsKey(qid)) {
                        NewHomeworkQuestion newHomeworkQuestion = questionMap.get(qid);
                        //跟读类应用的标准分都是100，因为是由语音引擎打分
                        newHomeworkQuestion.setScore(qscoreMap.get(qid));
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

                newHomeworkQuestions = newHomeworkQuestions.stream()
                        .filter(q -> allQuestionIdSet.add(q.getQuestionId()))
                        .collect(Collectors.toList());

                if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
                    continue;
                }

                nha.setQuestions(newHomeworkQuestions);
                nha.setVideoId(videoId);


                if (newHomeworkAppKeySet.contains(videoId)) {
                    logger.warn("Found duplicate app, appKey : {}, homeworkSource : {}", videoId, JsonUtils.toJson(context.getSource()));
                } else {
                    newHomeworkAppKeySet.add(videoId);
                    newHomeworkApps.add(nha);
                }

                if (app.containsKey("book")) {
                    Map<String, Object> book = (Map) app.get("book");
                    if (book != null) {
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                        bookVideoIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(videoId);
                    }
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
            List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookVideoIdsMap.entrySet()
                    .stream()
                    .map(entry -> {
                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                        BookInfoMapper bookInfoMapper = entry.getKey();
                        newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                        newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                        newHomeworkBookInfo.setVideos(entry.getValue());
                        return newHomeworkBookInfo;
                    })
                    .collect(Collectors.toList());
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
