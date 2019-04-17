package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 16/7/7.
 */
@Named
public class NewHomeworkContentProcessExam extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.EXAM;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {

        List<Map> questions = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("questions")), Map.class);

        if (CollectionUtils.isNotEmpty(questions)) {
            Map<String, NewHomeworkQuestion> questionMap = new LinkedHashMap<>();
            List<String> questionIds = new ArrayList<>();
            //key:BookInfoMapper   value:questionIds
            Map<BookInfoMapper, List<String>> bookQuestionIdsMap = new LinkedHashMap<>();

            for (Map question : questions) {
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
                if (question.containsKey("questionBoxName")) {
                    nhq.setQuestionBoxName(SafeConverter.toString(question.get("questionBoxName")));
                }
                if (question.containsKey("courseId")) {
                    nhq.setCourseId(SafeConverter.toString(question.get("courseId")));
                }
                if (question.containsKey("paperId"))
                    nhq.setPaperId(SafeConverter.toString(question.get("paperId")));
                if (question.containsKey("similarQuestionId"))
                    nhq.setSimilarQuestionId(SafeConverter.toString(question.get("similarQuestionId")));
                if (CollectionUtils.isNotEmpty(nhq.getSubmitWay()) && Objects.equals(Boolean.TRUE, nhq.isSubjectiveQuestion())) {
                    context.setIncludeSubjective(true);
                }
                if (question.containsKey("sourceType")) {
                    nhq.setSourceType(SafeConverter.toString(question.get("sourceType")));
                }
                if (question.containsKey("book")) {
                    Map<String, Object> book = (Map) question.get("book");
                    if (book != null) {
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                        bookInfoMapper.setSectionId(SafeConverter.toString(book.get("sectionId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(question.get("objectiveId"), ""));
                        bookQuestionIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(questionId);
                    }
                }
                questionMap.put(questionId, nhq);
                questionIds.add(questionId);
            }

            // 根据题目ID查询题目的原始信息
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            Map<String, Double> qscoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(newQuestionMap.values()), 100.00);
            List<NewHomeworkQuestion> newHomeworkQuestions = new ArrayList<>();
            for (String qid : questionMap.keySet()) {
                if (qscoreMap.containsKey(qid)) {
                    NewHomeworkQuestion newHomeworkQuestion = questionMap.get(qid);
                    // 题目版本号
                    NewQuestion q = newQuestionMap.getOrDefault(qid, null);
                    if (q != null) {
                        if (q.getOlUpdatedAt() != null) {
                            newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt().getTime());
                        } else if (q.getVersion() != null) {
                            newHomeworkQuestion.setQuestionVersion(SafeConverter.toLong(q.getVersion()));
                        }
                    }
                    // 口语习题每题标准分都是100
                    if (ObjectiveConfigType.ORAL_PRACTICE == objectiveConfigType || ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING == objectiveConfigType) {
                        newHomeworkQuestion.setScore(100.0);
                    } else {
                        newHomeworkQuestion.setScore(qscoreMap.get(qid));
                    }
                    newHomeworkQuestions.add(newHomeworkQuestion);
                } else {
                    return contentError(context, objectiveConfigType);
                }
            }
            if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                nhpc.setType(objectiveConfigType);
                // 只有新口算的时候才有限时和奖励的规则
                if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
                    Integer timeLimit = SafeConverter.toInt(practice.get("timeLimit"));
                    nhpc.setTimeLimit(MentalArithmeticTimeLimit.of(timeLimit));
                    Boolean recommend = SafeConverter.toBoolean(practice.get("recommend"));
                    nhpc.setRecommend(recommend);
                    // 题目顺序随机打乱,避免将同一答案的题目连续出现
                    Collections.shuffle(newHomeworkQuestions);
                }
                nhpc.setQuestions(newHomeworkQuestions);
                nhpc.setIncludeSubjective(newHomeworkQuestions.stream().anyMatch(NewHomeworkQuestion::isSubjectiveQuestion));

                for (Long groupId : context.getGroupIds()) {
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                    // 新口算训练奖励
                    if (ObjectiveConfigType.MENTAL_ARITHMETIC.equals(objectiveConfigType)) {
                        Boolean mentalAward = true;
                        Integer timeLimit = SafeConverter.toInt(practice.get("timeLimit"));
                        if (timeLimit <= 0) {
                            mentalAward = false;
                        } else {
                            String key = new GenerateKey(context.getTeacher().getId(), groupId).toString();
                            CacheObject<Long> cacheObject = HomeworkCache.getHomeworkCache().get(key);
                            if (cacheObject != null && cacheObject.getValue() != null) {
                                Long countValue = cacheObject.getValue();
                                if (countValue > 1) {
                                    mentalAward = false;
                                }
                                HomeworkCache.getHomeworkCache().set(key, DateUtils.getCurrentToWeekEndSecond(), ++countValue);
                            } else {
                                HomeworkCache.getHomeworkCache().set(key, DateUtils.getCurrentToWeekEndSecond(), 1L);
                            }
                        }
                        nhpc.setMentalAward(mentalAward);
                    }
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
                        newHomeworkBookInfo.setSectionId(bookInfoMapper.getSectionId());
                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                        newHomeworkBookInfo.setQuestions(entry.getValue());
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

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class GenerateKey {
        private Long teacherId;
        private Long groupId;

        @Override
        public String toString() {
            return "tid=" + teacherId + ",gid=" + groupId + ",week=" + generateWeek() + ",obj=" + ObjectiveConfigType.MENTAL_ARITHMETIC.name();
        }

        static int generateWeek() {
            WeekRange currentWeekRange = WeekRange.current();
            return currentWeekRange.getWeekOfYear();
        }
    }
}
