package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
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

@Named
public class NewHomeworkContentProcessDictation extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.DICTATION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> questions = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("questions")), Map.class);
        ObjectiveConfigType assignedObjectiveConfigType = SafeConverter.toBoolean(practice.get("ocrDictation")) ? ObjectiveConfigType.OCR_DICTATION : ObjectiveConfigType.ONLINE_DICTATION;
        if (CollectionUtils.isNotEmpty(questions)) {
            Map<String, NewHomeworkQuestion> questionMap = new LinkedHashMap<>();
            List<String> questionIds = new ArrayList<>();
            Map<BookInfoMapper, List<String>> bookQuestionIdsMap = new LinkedHashMap<>();
            for (Map question : questions) {
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                String questionId = SafeConverter.toString(question.get("questionId"));
                nhq.setQuestionId(questionId);
                if (question.containsKey("seconds")) {
                    nhq.setSeconds(SafeConverter.toInt(question.get("seconds")));
                }
                if (question.containsKey("submitWay")) {
                    nhq.setSubmitWay((List<List<Integer>>) question.get("submitWay"));
                }
                if (question.containsKey("questionBoxId")) {
                    nhq.setQuestionBoxId(SafeConverter.toString(question.get("questionBoxId")));
                }
                if (question.containsKey("book")) {
                    Map<String, Object> book = (Map) question.get("book");
                    if (book != null) {
                        nhq.setQuestionBoxId(SafeConverter.toString(book.get("lessonId")));
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                        bookInfoMapper.setLessonId(SafeConverter.toString(book.get("lessonId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(question.get("objectiveId"), ""));
                        bookQuestionIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(questionId);
                    }
                }
                questionMap.put(questionId, nhq);
                questionIds.add(questionId);
            }

            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            Map<String, Double> qscoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(newQuestionMap.values()), 100.0);
            List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
            for (String qid : questionMap.keySet()) {
                if (qscoreMap.containsKey(qid)) {
                    NewHomeworkQuestion newHomeworkQuestion = questionMap.get(qid);
                    NewQuestion q = newQuestionMap.getOrDefault(qid, null);
                    if (q != null) {
                        if (q.getOlUpdatedAt() != null) {
                            newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt().getTime());
                        } else if (q.getVersion() != null) {
                            newHomeworkQuestion.setQuestionVersion(SafeConverter.toLong(q.getVersion()));
                        }
                    }
                    newHomeworkQuestion.setScore(qscoreMap.get(qid));
                    newHomeworkQuestions.add(newHomeworkQuestion);
                } else {
                    return contentError(context, objectiveConfigType);
                }
            }
            if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                // 对相同lesson下的question进行乱序处理
                Map<String, List<NewHomeworkQuestion>> lessonIdQuestionsMap = new LinkedHashMap<>();
                for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                    String lessonId = SafeConverter.toString(newHomeworkQuestion.getQuestionBoxId(), "");
                    lessonIdQuestionsMap.computeIfAbsent(lessonId, k -> new ArrayList<>()).add(newHomeworkQuestion);
                    newHomeworkQuestions = new ArrayList<>();
                }
                for (String lessonId : lessonIdQuestionsMap.keySet()) {
                    List<NewHomeworkQuestion> questionList = lessonIdQuestionsMap.get(lessonId);
                    Collections.shuffle(questionList);
                    newHomeworkQuestions.addAll(questionList);
                }
                NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                nhpc.setType(assignedObjectiveConfigType);
                nhpc.setQuestions(newHomeworkQuestions);
                nhpc.setIncludeSubjective(false);
                for (Long groupId : context.getGroupIds()) {
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                    newHomeworkPracticeContents.add(nhpc);
                    context.getGroupPractices().put(groupId, newHomeworkPracticeContents);
                }
            }
            List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookQuestionIdsMap.entrySet()
                    .stream()
                    .map(entry -> {
                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                        BookInfoMapper bookInfoMapper = entry.getKey();
                        newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                        newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                        newHomeworkBookInfo.setLessonId(bookInfoMapper.getLessonId());
                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                        newHomeworkBookInfo.setQuestions(entry.getValue());
                        return newHomeworkBookInfo;
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                for (Long groupId : context.getGroupIds()) {
                    LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap()
                            .getOrDefault(groupId, new LinkedHashMap<>());
                    practiceBooksMap.put(assignedObjectiveConfigType, newHomeworkBookInfoList);
                    context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                }
            }
        }
        return context;
    }
}