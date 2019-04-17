/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
abstract public class NewHomeworkQuizContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject private PaperLoaderClient paperLoaderClient;

    private static final List<Integer> UNIT_QUIZ_SUPPORTED_SUB_CONTENT_TYPE_IDS = Arrays.asList(27, 28, 29, 30);

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();

        Set<String> allPaperDocIds = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.PAPER_ID) {
                    List<String> paperDocIds = (List<String>) configContent.get("paper_ids");
                    if (CollectionUtils.isNotEmpty(paperDocIds)) {
                        allPaperDocIds.addAll(paperDocIds);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(allPaperDocIds)) {
            // 获取使用次数数据
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            // 所有题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            List<NewPaper> papers = paperLoaderClient.loadNewPapersByDocIds(allPaperDocIds);
            Map<String, TotalAssignmentRecord> paperAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allPaperDocIds, HomeworkContentType.PAPER);
            if (CollectionUtils.isNotEmpty(papers)) {
                Map<String, List<String>> paperIdQuestionIds = new LinkedHashMap<>();
                Set<String> allQuestionIds = new LinkedHashSet<>();
                for (NewPaper newPaper : papers) {
                    if (CollectionUtils.isNotEmpty(newPaper.getQuestions())) {
                        List<String> questionIds = newPaper.getQuestions().stream().map(XxBaseQuestion::getId).collect(Collectors.toList());
                        allQuestionIds.addAll(questionIds);
                        paperIdQuestionIds.put(newPaper.getId(), questionIds);
                    }
                }
                Map<String, TotalAssignmentRecord> questionAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionIds, HomeworkContentType.QUESTION);
                Map<String, NewQuestion> allQuestionsMap = questionLoaderClient.loadQuestions(allQuestionIds);
                List<Integer> supportedSubContentTypeIds = new ArrayList<>(QuestionConstants.xxSubContentTypeIncludeIds);
                supportedSubContentTypeIds.addAll(UNIT_QUIZ_SUPPORTED_SUB_CONTENT_TYPE_IDS);
                for (NewPaper newPaper : papers) {
                    // 处理试卷题目
                    List<String> questionIdList = paperIdQuestionIds.getOrDefault(newPaper.getId(), Collections.emptyList());
                    Map<String, NewQuestion> questionMap = new LinkedHashMap<>();
                    if (CollectionUtils.isNotEmpty(questionIdList)) {
                        for (String questionId : questionIdList) {
                            NewQuestion question = allQuestionsMap.get(questionId);
                            if (question != null
                                    // 过滤不支持在线作答的题目
                                    && question.supportOnlineAnswer()
                                    // 过滤熔断题目
                                    && !Objects.equals(true, question.getBroken())
                                    // 过滤基础题型
                                    && supportedSubContentTypeIds.containsAll(question.findSubContentTypeIds())
                                    // 过滤移动端不支持的题型
                                    && Objects.equals(question.getNotFitMobile(), 0)) {
                                questionMap.put(questionId, question);
                            }
                        }
                    }
                    // 转换为前端需要的格式
                    if (MapUtils.isNotEmpty(questionMap)) {
                        Map<String, Object> quizMap = new LinkedHashMap<>();
                        quizMap.put("id", newPaper.getId());
                        quizMap.put("assignTimes", paperAssignmentRecordMap.get(newPaper.getDocId()) != null ? paperAssignmentRecordMap.get(newPaper.getDocId()).getAssignTimes() : 0);
                        if (teacherAssignmentRecord != null) {
                            quizMap.put("teacherAssignTimes", teacherAssignmentRecord.getPaperInfo().getOrDefault(newPaper.getDocId(), 0));
                        }
                        quizMap.put("title", newPaper.getTitle());
                        quizMap.put("unitId", unitId);
                        String paperSource = QuestionConstants.paperSourceMap.get(newPaper.getFromId());
                        if (StringUtils.isEmpty(paperSource)) {
                            paperSource = "未知";
                        }
                        quizMap.put("paperSource", paperSource);
                        EmbedBook book = new EmbedBook();
                        book.setUnitId(unitId);
                        book.setBookId(bookId);
                        List<Map<String, Object>> questionList = questionMap.values().stream()
                                .map(q -> NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, questionAssignmentRecordMap, teacherAssignmentRecord, book))
                                .collect(Collectors.toList());
                        quizMap.put("showAssigned", teacherAssignmentRecord != null && questionMap.values().stream().allMatch(q -> teacherAssignmentRecord.getQuestionInfo().getOrDefault(q.getDocId(), 0) > 0));
                        quizMap.put("seconds", questionMap.values().stream().mapToInt(q -> SafeConverter.toInt(q.getSeconds())).sum());
                        quizMap.put("questions", questionList);
                        content.add(quizMap);
                    }
                }
            }
        }
        return content;
    }

    @Override
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> paperList = loadContent(mapper);
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        if (CollectionUtils.isNotEmpty(paperList)) {
            return MapUtils.m(
                    "objectiveConfigId", objectiveConfig.getId(),
                    "type", getObjectiveConfigType().name(),
                    "typeName", getObjectiveConfigType().getValue(),
                    "name", objectiveConfig.getName(),
                    "paperList", paperList
            );
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> quizQuestionList = previewQuestions(contentIdList);
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", quizQuestionList
        );
    }
}
