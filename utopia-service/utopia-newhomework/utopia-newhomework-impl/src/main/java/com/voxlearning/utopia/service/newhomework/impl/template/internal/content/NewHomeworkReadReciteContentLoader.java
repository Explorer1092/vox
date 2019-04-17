package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkSubjectiveContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkReadReciteContentLoader extends NewHomeworkSubjectiveContentLoader {
    @Inject private DoHomeworkProcessor doHomeworkProcessor;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READ_RECITE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        // 主观作业只给题id就行
        List<Map<String, Object>> subjectContent = teachingObjectiveLoaderClient.loadContentByCatalogIdsAndType(mapper.getSectionIds(), getObjectiveConfigType(), Collections.singleton(ObjectiveConfig.QUESTION_ID),
                teacher.getSubject().getId());
        Map<String, EmbedBook> questionBookMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(subjectContent)) {
            subjectContent.forEach(objectiveConfigContent -> {
                List<String> questionIds = conversionService.convert(objectiveConfigContent.get("question_ids"), List.class);
                EmbedBook book = conversionService.convert(objectiveConfigContent.get("book"), EmbedBook.class);
                if (CollectionUtils.isNotEmpty(questionIds)) {
                    questionIds.forEach(id -> {
                        if (!questionBookMap.containsKey(id)) {
                            questionBookMap.put(id, book);
                        }
                    });
                }
            });
        }
        if (questionBookMap.size() > 0) {
            // 老师的使用次数
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), mapper.getBookId());

            List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionBookMap.keySet());
            doHomeworkProcessor.handReadRecite(newQuestionList, true);
            Set<String> allQuestionIdSet = newQuestionList.stream().map(NewQuestion::getId).collect(Collectors.toSet());
            // 总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionIdSet, HomeworkContentType.QUESTION);
            Map<String, NewQuestion> newQuestionMap = new LinkedHashMap<>();
            newQuestionList.forEach(question -> newQuestionMap.put(question.getDocId(), question));
            List<Map<String, Object>> subjectResultContent = new ArrayList<>();
            questionBookMap.forEach((questionId, book) -> {
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
                    subjectiveQuestionMap.put("articleName", subjectiveQuestion.getArticleName());
                    subjectiveQuestionMap.put("paragraph", subjectiveQuestion.getParagraph());
                    subjectiveQuestionMap.put("paragraphCName", doHomeworkProcessor.transferToChineseName(subjectiveQuestion.getParagraph()));
                    subjectiveQuestionMap.put("sentenceIds", subjectiveQuestion.getSentenceIds());
                    subjectResultContent.add(subjectiveQuestionMap);
                }
            });
            content = doHomeworkProcessor.sortHandle(subjectResultContent);
        }
        if (content.isEmpty()) {
            processTeacherLog(teacher, getObjectiveConfigType(), mapper.getUnitId());
        }
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> subjectiveQuestionList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestions(contentIdList);
            List<NewQuestion> newQuestions = new LinkedList<>(newQuestionMap.values());
            newQuestions = doHomeworkProcessor.sortHandle(newQuestions);
            doHomeworkProcessor.handReadRecite(newQuestions, true);
            newQuestions.forEach(newQuestion ->
                    subjectiveQuestionList.add(MapUtils.m(
                            "questionId", newQuestion.getId(),
                            "seconds", newQuestion.getSeconds(),
                            "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型",
                            "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()),
                            "articleName", newQuestion.getArticleName(),
                            "paragraph", newQuestion.getParagraph(),
                            "paragraphCName", doHomeworkProcessor.transferToChineseName(newQuestion.getParagraph())
                    ))
            );
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", subjectiveQuestionList
        );
    }

}
