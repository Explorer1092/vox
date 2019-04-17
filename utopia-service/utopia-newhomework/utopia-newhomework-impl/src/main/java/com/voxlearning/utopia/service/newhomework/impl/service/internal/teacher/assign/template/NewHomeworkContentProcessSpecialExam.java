package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
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
 * @author guoqiang.li
 * @since 2017/1/10
 */
@Named
public class NewHomeworkContentProcessSpecialExam extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.SPECIAL_EXAM;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> groupPractice, ObjectiveConfigType objectiveConfigType) {
        if (MapUtils.isNotEmpty(groupPractice)) {
            List<Map> questions = JsonUtils.fromJsonToList(JsonUtils.toJson(groupPractice.get("questions")), Map.class);
            if (CollectionUtils.isNotEmpty(questions)) {
                Map<String, NewHomeworkQuestion> questionMap = new LinkedHashMap<>();
                List<String> questionIds = new ArrayList<>();
                Map<Long, List<String>> groupIdQids = new LinkedHashMap<>();
                Map<Long, Map<BookInfoMapper, List<String>>> groupBookQuestionIdsMap = new LinkedHashMap<>();

                for (Map question : questions) {
                    Long groupId = SafeConverter.toLong(question.get("groupId"));
                    if (context.getGroupIds().contains(groupId)) {
                        NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                        String questionId = SafeConverter.toString(question.get("questionId"));
                        nhq.setQuestionId(questionId);
                        if (question.containsKey("seconds"))
                            nhq.setSeconds(SafeConverter.toInt(question.get("seconds")));
                        if (question.containsKey("answerType"))
                            nhq.setAnswerType(SafeConverter.toInt(question.get("answerType")));
                        if (question.containsKey("submitWay"))
                            nhq.setSubmitWay((List<List<Integer>>) question.get("submitWay"));
                        if (question.containsKey("answerWay"))
                            nhq.setAnswerWay((List<List<Integer>>) question.get("answerWay"));
                        if (question.containsKey("knowledgePointId"))
                            nhq.setKnowledgePointId(SafeConverter.toString(question.get("knowledgePointId")));
                        if (question.containsKey("questionBoxId"))
                            nhq.setQuestionBoxId(SafeConverter.toString(question.get("questionBoxId")));
                        if (CollectionUtils.isNotEmpty(nhq.getSubmitWay()) && Objects.equals(Boolean.TRUE, nhq.isSubjectiveQuestion())) {
                            context.setIncludeSubjective(true);
                        }

                        if (question.containsKey("book")) {
                            Map<String, Object> book = (Map) question.get("book");
                            if (book != null) {
                                BookInfoMapper bookInfoMapper = new BookInfoMapper();
                                bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                                bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                                bookInfoMapper.setSectionId(SafeConverter.toString(book.get("sectionId"), ""));
                                bookInfoMapper.setObjectiveId(SafeConverter.toString(question.get("objectiveId"), ""));
                                groupBookQuestionIdsMap.computeIfAbsent(groupId, k -> new LinkedHashMap<>()).computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(questionId);
                            }
                        }

                        List<String> groupQids = groupIdQids.computeIfAbsent(groupId, k -> new ArrayList<>());
                        groupQids.add(questionId);

                        questionMap.put(groupId + "#" + questionId, nhq);
                        questionIds.add(questionId);
                    }
                }

                if (MapUtils.isNotEmpty(groupIdQids)) {
                    Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                    for (Map.Entry<Long, List<String>> entry : groupIdQids.entrySet()) {
                        Long groupId = entry.getKey();
                        List<String> qids = entry.getValue();

                        List<NewQuestion> newQuestionList = qids.stream()
                                .filter(newQuestionMap::containsKey)
                                .map(newQuestionMap::get)
                                .collect(Collectors.toList());
                        Map<String, Double> qScoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestionList, 100.00);
                        List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
                        for (String qid : qids) {
                            if (qScoreMap.containsKey(qid)) {
                                NewHomeworkQuestion newHomeworkQuestion = questionMap.get(groupId + "#" + qid);
                                // 题目版本号
                                NewQuestion q = newQuestionMap.getOrDefault(qid, null);
                                if (q != null) {
                                    newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                                }
                                newHomeworkQuestion.setScore(qScoreMap.get(qid));
                                newHomeworkQuestions.add(newHomeworkQuestion);
                            } else {
                                return contentError(context, objectiveConfigType);
                            }
                        }

                        if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                            NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                            nhpc.setType(objectiveConfigType);
                            nhpc.setQuestions(newHomeworkQuestions);
                            nhpc.setIncludeSubjective(newHomeworkQuestions.stream().anyMatch(NewHomeworkQuestion::isSubjectiveQuestion));
                            List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                            newHomeworkPracticeContents.add(nhpc);
                            context.getGroupPractices().put(groupId, newHomeworkPracticeContents);
                        }

                        Map<BookInfoMapper, List<String>> bookQuestionIdsMap = groupBookQuestionIdsMap.get(groupId);
                        if (MapUtils.isNotEmpty(bookQuestionIdsMap)) {
                            List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookQuestionIdsMap.entrySet()
                                    .stream()
                                    .map(bookEntry -> {
                                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                                        BookInfoMapper bookInfoMapper = bookEntry.getKey();
                                        newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                                        newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                                        newHomeworkBookInfo.setSectionId(bookInfoMapper.getSectionId());
                                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                                        newHomeworkBookInfo.setQuestions(bookEntry.getValue());
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
        }
        return context;
    }
}
