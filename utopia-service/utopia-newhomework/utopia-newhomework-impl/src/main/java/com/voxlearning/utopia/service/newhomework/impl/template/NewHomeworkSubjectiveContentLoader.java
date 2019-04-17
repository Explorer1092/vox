package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
abstract public class NewHomeworkSubjectiveContentLoader extends NewHomeworkContentLoaderTemplate {
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        List<Map<String, Object>> subjectiveContents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.QUESTION_ID) {
                    subjectiveContents.add(configContent);
                }
            }
        }
        Set<String> questionDocIds = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(subjectiveContents)) {
            subjectiveContents.forEach(objectiveConfigContent -> {
                List<String> questionIds = conversionService.convert(objectiveConfigContent.get("question_ids"), List.class);
                if (CollectionUtils.isNotEmpty(questionIds)) {
                    questionDocIds.addAll(questionIds);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(questionDocIds)) {
            // 老师的使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), mapper.getBookId());
            List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionDocIds);
            Set<String> allQuestionIdSet = newQuestionList.stream().map(NewQuestion::getId).collect(Collectors.toSet());
            // 总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionIdSet, HomeworkContentType.QUESTION);
            Map<String, NewQuestion> newQuestionMap = new LinkedHashMap<>();
            newQuestionList.forEach(question -> newQuestionMap.put(question.getDocId(), question));
            List<Map<String, Object>> subjectResultContent = new ArrayList<>();
            EmbedBook book = new EmbedBook();
            book.setBookId(mapper.getBookId());
            book.setUnitId(mapper.getUnitId());
            questionDocIds.forEach(questionId -> {
                Map<String, Object> subjectiveQuestionMap = new LinkedHashMap<>();
                NewQuestion subjectiveQuestion = newQuestionMap.get(questionId);
                if (subjectiveQuestion != null) {
                    subjectiveQuestionMap.put("questionId", subjectiveQuestion.getId());
                    subjectiveQuestionMap.put("assignTimes", totalAssignmentRecordMap.get(subjectiveQuestion.getId()) != null
                            ? totalAssignmentRecordMap.get(subjectiveQuestion.getId()).getAssignTimes() : 0);
                    if (teacherAssignmentRecord != null) {
                        subjectiveQuestionMap.put("teacherAssignTimes", teacherAssignmentRecord.getQuestionInfo()
                                .getOrDefault(TeacherAssignmentRecord.id2DocId(subjectiveQuestion.getId()), 0));
                    }
                    subjectiveQuestionMap.put("seconds", subjectiveQuestion.getSeconds());
                    subjectiveQuestionMap.put("book", book);
                    subjectiveQuestionMap.put("submitWay", subjectiveQuestion.getSubmitWays());
                    subjectResultContent.add(subjectiveQuestionMap);
                }
            });
            content = subjectResultContent;
        }
        return content;
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> subjectiveQuestionList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestions(contentIdList);
            newQuestionMap.forEach((id, newQuestion) -> subjectiveQuestionList.add(MiscUtils.m(
                    "questionId", newQuestion.getId(),
                    "seconds", newQuestion.getSeconds(),
                    "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型",
                    "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt())
            )));
        }
        return MiscUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", subjectiveQuestionList
        );
    }
}
