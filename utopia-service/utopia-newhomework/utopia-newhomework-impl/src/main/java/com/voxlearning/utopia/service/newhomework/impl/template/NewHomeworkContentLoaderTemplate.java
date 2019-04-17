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

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.loader.TeacherAssignmentRecordLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.TotalAssignmentRecordLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/1/25
 */
abstract public class NewHomeworkContentLoaderTemplate extends NewHomeworkSpringBean {

    @Inject protected TeacherAssignmentRecordLoaderImpl teacherAssignmentRecordLoader;
    @Inject protected TotalAssignmentRecordLoaderImpl totalAssignmentRecordLoader;

    @Inject private RaikouSDK raikouSDK;

    abstract public ObjectiveConfigType getObjectiveConfigType();

    abstract public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper);

    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        return Collections.emptyMap();
    }

    /**
     * 作业预览 (默认为预览题，如果传进来的参数不为questionId，需要自己实现)
     *
     * @param contentIdList 作业内容id列表，可能为questionId，paperId, 不同作业形式不一样
     */
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> questionList = previewQuestions(contentIdList);
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", questionList
        );
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getPackageContent(TeacherDetail teacher, ObjectiveConfig objectiveConfig, String bookId, String unitId) {
        // 取按题包配置的内容
        List<Map<String, Object>> questionBoxContent = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objectiveConfig.getContents())) {
            for (Map<String, Object> configContent : objectiveConfig.getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.QUESTION_NAME_ID) {
                    questionBoxContent.add(configContent);
                }
            }
        }
        List<Map<String, Object>> questionBoxList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(questionBoxContent)) {
            Set<String> allQuestionDocIds = new HashSet<>();
            questionBoxContent.forEach(map -> allQuestionDocIds.addAll((List<String>) map.get("question_ids")));
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIds).stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity(), (o1, o2) -> o2));
            Set<String> allQuestionIds = allQuestionMap.values()
                    .stream()
                    .map(NewQuestion::getId)
                    .collect(Collectors.toSet());
            Map<String, TotalAssignmentRecord> questionTotalAssignmentRecord = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(teacher.getSubject(), allQuestionIds, HomeworkContentType.QUESTION);
            // 所有题型
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            questionBoxList = questionBoxContent.stream()
                    .filter(questionBox -> CollectionUtils.isNotEmpty((List<String>) questionBox.get("question_ids")))
                    .map(questionBox -> {
                        String id = SafeConverter.toString(questionBox.get("id"));
                        String name = SafeConverter.toString(questionBox.get("name"));
                        Integer difficulty = SafeConverter.toInt(questionBox.get("difficulty"));
                        List<String> questionIds = (List<String>) questionBox.get("question_ids");

                        List<Map<String, Object>> questionMapperList = new ArrayList<>();
                        boolean showAssigned = true;
                        int seconds = 0;
                        for (String docId : questionIds) {
                            NewQuestion question = allQuestionMap.get(docId);
                            if (question != null) {
                                questionMapperList.add(NewHomeworkContentDecorator.decorateNewQuestion(
                                        question,
                                        contentTypeMap,
                                        questionTotalAssignmentRecord,
                                        teacherAssignmentRecord,
                                        book));
                                seconds += SafeConverter.toInt(question.getSeconds());
                                if (showAssigned && (teacherAssignmentRecord == null || teacherAssignmentRecord.getQuestionInfo().getOrDefault(docId, 0) <= 0)) {
                                    showAssigned = false;
                                }
                            }
                        }

                        return MapUtils.m(
                                "id", id,
                                "name", name,
                                "assignTimes", 0,
                                "teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getPackageInfo().getOrDefault(id, 0) : 0,
                                "showAssigned", showAssigned,
                                "seconds", seconds,
                                "difficultyName", QuestionConstants.newDifficultyMap.get(difficulty),
                                "questions", questionMapperList);
                    })
                    .collect(Collectors.toList());
        }
        return MapUtils.m("type", "package", "packages", questionBoxList);
    }

    protected List<Map<String, Object>> previewQuestions(List<String> questionIdList) {
        if (CollectionUtils.isNotEmpty(questionIdList)) {
            Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIdList);
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
            return questionIdList.stream()
                    .filter(questionId -> newQuestionMap.get(questionId) != null)
                    .map(questionId -> {
                        NewQuestion newQuestion = newQuestionMap.get(questionId);
                        return MapUtils.m(
                                "questionId", newQuestion.getId(),
                                "seconds", newQuestion.getSeconds(),
                                "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型",
                                "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()),
                                "upImage", newQuestion.getSubmitWays().stream().flatMap(Collection::stream).anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2))
                        );
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    protected List<Map<String, Object>> previewSpecialExam(List<String> groupIdQuestionIdList) {
        List<Map<String, Object>> previewContent = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupIdQuestionIdList)) {
            Map<Long, List<String>> groupQuestionIdMap = new LinkedHashMap<>();
            for (String groupIdQuestionId : groupIdQuestionIdList) {
                if (StringUtils.isNotBlank(groupIdQuestionId)) {
                    String[] strs = StringUtils.split(groupIdQuestionId, "|");
                    if (strs.length == 2) {
                        Long groupId = SafeConverter.toLong(strs[0]);
                        String questionId = SafeConverter.toString(strs[1]);
                        if (groupId != 0 && StringUtils.isNotBlank(questionId)) {
                            List<String> questionIdList = groupQuestionIdMap.computeIfAbsent(groupId, k -> new ArrayList<>());
                            questionIdList.add(questionId);
                        }
                    }
                }
            }
            if (MapUtils.isNotEmpty(groupQuestionIdMap)) {
                Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(groupQuestionIdMap.keySet(), false);
                Set<Long> clazzIdSet = groupMapperMap.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
                Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzsIncludeDisabled(clazzIdSet)
                        .stream()
                        .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                Set<String> allQuestionIdSet = groupQuestionIdMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIdSet);
                Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
                groupQuestionIdMap.forEach((groupId, questionIds) -> {
                    List<Map<String, Object>> questionMapperList = questionIds.stream()
                            .filter(questionId -> newQuestionMap.get(questionId) != null)
                            .map(questionId -> {
                                NewQuestion newQuestion = newQuestionMap.get(questionId);
                                return MapUtils.m(
                                        "questionId", newQuestion.getId(),
                                        "seconds", newQuestion.getSeconds(),
                                        "questionType", contentTypeMap.get(newQuestion.getContentTypeId()) != null ? contentTypeMap.get(newQuestion.getContentTypeId()).getName() : "无题型",
                                        "difficultyName", QuestionConstants.newDifficultyMap.get(newQuestion.getDifficultyInt()),
                                        "upImage", newQuestion.getSubmitWays().stream().flatMap(Collection::stream).anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2))
                                );
                            })
                            .collect(Collectors.toList());
                    String groupName = "";
                    if (groupMapperMap.containsKey(groupId)) {
                        GroupMapper groupMapper = groupMapperMap.get(groupId);
                        Long clazzId = groupMapper.getClazzId();
                        if (clazzMap.containsKey(clazzId)) {
                            groupName = clazzMap.get(clazzId).formalizeClazzName();
                        }
                    }
                    previewContent.add(MapUtils.m(
                            "groupId", groupId,
                            "groupName", groupName,
                            "questions", questionMapperList
                    ));
                });
            }
        }
        return previewContent;
    }

    protected void processTeacherLog(Teacher teacher, ObjectiveConfigType objectiveConfigType, String unitId) {
        Map<String, String> info = new HashMap<>();
        info.put("userId", SafeConverter.toString(teacher.getId()));
        info.put("subject", teacher.getSubject().name());
        info.put("unitId", unitId);
        info.put("objectiveConfigType", objectiveConfigType.name());
        Mode mode = RuntimeMode.current();
        info.put("env", mode.name());
        info.put("op", "question_empty");
        LogCollector.info("web_teacher_logs", info);
    }

    protected Map<String, String> loadKpName(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Set<String> kpIds = new LinkedHashSet<>();
        Set<String> kpfIds = new LinkedHashSet<>();
        Set<String> tmIds = new LinkedHashSet<>();
        Set<String> smIds = new LinkedHashSet<>();
        for (String id : ids) {
            if (id.startsWith("KP_")) {
                if (id.contains("KPF_")) {
                    String[] splitIds = StringUtils.split(id, ":");
                    if (splitIds.length == 2) {
                        String kpId = splitIds[0];
                        String kpfId = splitIds[1];
                        if (kpId.startsWith("KP_") && kpfId.startsWith("KPF_")) {
                            kpIds.add(kpId);
                            kpfIds.add(kpfId);
                        }
                    }
                } else {
                    kpIds.add(id);
                }
            } else if (id.startsWith("TM_")) {
                tmIds.add(id);
            } else if (id.startsWith("SM_")) {
                smIds.add(id);
            }
        }
        Set<String> allIds = new LinkedHashSet<>(kpIds);
        allIds.addAll(tmIds);
        allIds.addAll(smIds);
        Map<String, String> kpNameMap = testMethodLoaderClient.getNameById(allIds);
        Map<String, KnowledgePointFeature> knowledgePointFeatureMap = featureLoaderClient.loadKnowledgePointFeatures(kpfIds);
        Map<String, String> result = new LinkedHashMap<>();
        for (String id : ids) {
            if (id.startsWith("KP_") && id.contains("KPF_")) {
                String[] splitIds = StringUtils.split(id, ":");
                if (splitIds.length == 2) {
                    String kpId = splitIds[0];
                    String kpfId = splitIds[1];
                    if (kpId.startsWith("KP_") && kpfId.startsWith("KPF_") && kpNameMap.containsKey(kpId) && knowledgePointFeatureMap.containsKey(kpfId)) {
                        String kpName = kpNameMap.get(kpId);
                        String kpfName = knowledgePointFeatureMap.get(kpfId).getName();
                        result.put(id, kpName + "(" + kpfName + ")");
                    }
                }
            } else if (kpNameMap.containsKey(id)) {
                result.put(id, kpNameMap.get(id));
            }
        }
        return result;
    }

    protected void processMathMoreQuestionContent(List<Map<String, Object>> content, Map<String, List<String>> moreQuestions,
                                                  String bookId, String unitId, Map<Integer, NewContentType> contentTypeMap,
                                                  TeacherAssignmentRecord teacherAssignmentRecord, List<Integer> contentTypeList) {
        Set<String> allQuestionDocIds = moreQuestions.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIds)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
        Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                .stream()
                .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
        if (MapUtils.isNotEmpty(allQuestionMap)) {
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            // 总的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(Subject.MATH,
                    allQuestionMap.keySet(), HomeworkContentType.QUESTION);
            // 最后要推出来的题
            Set<String> pushQuestionDocIdSet = new HashSet<>();
            List<Map<String, Object>> questionMapperList = new ArrayList<>();
            moreQuestions.forEach((sectionId, questionDocIdSet) -> {
                book.setSectionId(sectionId);

                List<NewQuestion> newQuestionList = questionDocIdSet.stream()
                        .filter(pushQuestionDocIdSet::add)
                        .filter(docIdQuestionMap::containsKey)
                        .map(docIdQuestionMap::get)
                        .filter(q -> contentTypeList.contains(q.getContentTypeId()))
                        .filter(NewQuestion::supportOnlineAnswer)
                        .filter(q -> !Objects.equals(q.getNotFitMobile(), 1))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(newQuestionList)) {
                    newQuestionList.forEach(newQuestion -> questionMapperList.add(
                            NewHomeworkContentDecorator.decorateNewQuestion(
                                    newQuestion, contentTypeMap, totalAssignmentRecordMap,
                                    teacherAssignmentRecord, book)
                            )
                    );
                }
            });
            // 推送所有题目的题型
            Set<Integer> questionTypeSet = new LinkedHashSet<>();
            questionMapperList.forEach(map -> questionTypeSet.add(SafeConverter.toInt(map.get("questionTypeId"))));
            List<Map<String, Object>> questionTypes = questionTypeSet.stream()
                    .filter(typeId -> contentTypeMap.containsKey(typeId) && contentTypeMap.get(typeId) != null)
                    .map(typeId -> MapUtils.m("id", typeId, "name", contentTypeMap.get(typeId).getName()))
                    .collect(Collectors.toList());
            content.add(MapUtils.m("type", "question", "questions", questionMapperList, "questionTypes", questionTypes));
        }
    }
}
