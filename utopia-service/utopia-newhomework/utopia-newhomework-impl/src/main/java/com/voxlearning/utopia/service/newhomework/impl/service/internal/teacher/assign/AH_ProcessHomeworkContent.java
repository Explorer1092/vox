/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template.NewHomeworkContentProcessFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template.NewHomeworkContentProcessTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.TeachingObjective;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AH_ProcessHomeworkContent extends AbstractAssignHomeworkProcessor {

    @Inject private NewHomeworkContentProcessFactory newHomeworkContentProcessFactory;

    @Override
    protected void doProcess(AssignHomeworkContext context) {
        HomeworkSource homeworkSource = context.getSource();
        try {
            //处理作业内容
            processPractices(context, homeworkSource);
            //处理课本相关信息
            processBooks(context, homeworkSource);
            if (homeworkSource.containsKey("remark")) {
                context.setRemark(SafeConverter.toString(homeworkSource.get("remark")));
            }
        } catch (Exception ex) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT,
                    "mod3", JsonUtils.toJson(homeworkSource),
                    "mod4", ex.getMessage(),
                    "op", "assign homework"
            ));
            context.errorResponse("作业内容错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
            context.setTerminateTask(true);
        }
    }

    private void processPractices(AssignHomeworkContext context, HomeworkSource homeworkSource) {
        String practiceJson = JsonUtils.toJson(homeworkSource.get("practices"));
        Map<String, Object> practiceMap = JsonUtils.fromJson(practiceJson);
        if (!practiceMap.isEmpty()) {
            for (String key : practiceMap.keySet()) {
                Map<String, Object> practice = JsonUtils.fromJson(JsonUtils.toJson(practiceMap.get(key)));
                ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.valueOf(key);
                NewHomeworkContentProcessTemplate template = newHomeworkContentProcessFactory.getTemplate(objectiveConfigType.getNewHomeworkContentProcessTemp());
                context = template.processHomeworkContent(context, practice, objectiveConfigType);
                if (context.isTerminateTask()) {
                    return;
                }
            }
            if (MapUtils.isEmpty(context.getGroupPractices())) {
                LogCollector.info("backend-general", MapUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT,
                        "mod3", JsonUtils.toJson(homeworkSource),
                        "op", "assign homework"
                ));
                context.errorResponse("作业内容错误");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
                context.setTerminateTask(true);
            } else {
                // 对practices按照作业形式重新排序，过滤掉不属于这个学科的作业形式
                List<ObjectiveConfigType> subjectTypeList = ObjectiveConfigType.getAssignSubjectTypes(context.getTeacher().getSubject());
                for (Map.Entry<Long, List<NewHomeworkPracticeContent>> entry : context.getGroupPractices().entrySet()) {
                    Long groupId = entry.getKey();
                    List<NewHomeworkPracticeContent> practiceContents = entry.getValue();
                    if (CollectionUtils.isNotEmpty(practiceContents)) {
                        practiceContents = practiceContents
                                .stream()
                                .filter(e -> subjectTypeList.contains(e.getType()))
                                .sorted(Comparator.comparingInt(a -> subjectTypeList.indexOf(a.getType())))
                                .collect(Collectors.toList());
                        context.getGroupPractices().put(groupId, practiceContents);
                    }
                }
            }
        }
    }

    private void processBooks(AssignHomeworkContext context, HomeworkSource homeworkSource) {
        buildBookInfoFromBooks(context, homeworkSource);
        addBookInfoName(context);
    }

    /**
     * 正常布置作业不用解析books字段来获取book信息了，直接在question或者app上
     * 复制作业需要通过books来解析
     */
    @SuppressWarnings("unchecked")
    private void buildBookInfoFromBooks(AssignHomeworkContext context, HomeworkSource homeworkSource) {
        Map<String, List<Map>> practicesBooksMap = JsonUtils.fromJson(JsonUtils.toJson(homeworkSource.get("books")), Map.class);

        if (MapUtils.isNotEmpty(practicesBooksMap)) {
            for (String objectiveConfigTypeStr : practicesBooksMap.keySet()) {
                ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
                List<NewHomeworkBookInfo> bookInfos = new ArrayList<>();
                List<Map> books = JsonUtils.fromJsonToList(JsonUtils.toJson(practicesBooksMap.get(objectiveConfigTypeStr)), Map.class);
                for (Map book : books) {
                    NewHomeworkBookInfo bookInfo = new NewHomeworkBookInfo();
                    if (book.containsKey("bookId")) {
                        String bookId = SafeConverter.toString(book.get("bookId"));
                        bookInfo.setBookId(bookId);
                    }
                    if (book.containsKey("unitId")) {
                        String unitId = SafeConverter.toString(book.get("unitId"));
                        bookInfo.setUnitId(unitId);
                    }

                    if (book.containsKey("lessonId")) {
                        String lessonId = SafeConverter.toString(book.get("lessonId"));
                        bookInfo.setLessonId(lessonId);
                    }

                    if (book.containsKey("unitGroupId")) {
                        String unitGroupId = SafeConverter.toString(book.get("unitGroupId"));
                        bookInfo.setUnitGroupId(unitGroupId);
                    }

                    if (book.containsKey("sectionId")) {
                        String sectionId = SafeConverter.toString(book.get("sectionId"));
                        bookInfo.setSectionId(sectionId);
                    }

                    if (book.containsKey("objectiveId")) {
                        String objectiveId = SafeConverter.toString(book.get("objectiveId"));
                        bookInfo.setObjectiveId(objectiveId);
                    }

                    if (book.containsKey("questions")) {
                        bookInfo.setQuestions(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("questions")), String.class));
                    }

                    if (book.containsKey("includeQuestions")) {
                        bookInfo.setQuestions(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includeQuestions")), String.class));
                    }

                    if (book.containsKey("papers")) {
                        bookInfo.setPapers(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("papers")), String.class));
                    }

                    if (book.containsKey("pictureBooks")) {
                        bookInfo.setPictureBooks(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("pictureBooks")), String.class));
                    }

                    if (book.containsKey("videos")) {
                        bookInfo.setVideos(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("videos")), String.class));
                    }

                    if (book.containsKey("questionBoxIds")) {
                        bookInfo.setQuestionBoxIds(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("questionBoxIds")), String.class));
                    }

                    if (book.containsKey("dubbingIds")) {
                        bookInfo.setDubbingIds(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("dubbingIds")), String.class));
                    }

                    if (book.containsKey("appIds")) {
                        bookInfo.setAppIds(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("appIds")), String.class));
                    }

                    bookInfos.add(bookInfo);
                }
                if (CollectionUtils.isNotEmpty(bookInfos)) {
                    for (Long groupId : context.getGroupIds()) {
                        LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap().getOrDefault(groupId, new LinkedHashMap<>());
                        if (!practiceBooksMap.containsKey(objectiveConfigType)) {
                            practiceBooksMap.put(objectiveConfigType, bookInfos);
                            context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                        }
                    }
                }
            }
        }
    }

    /**
     * 填充name字段
     */
    private void addBookInfoName(AssignHomeworkContext context) {
        Set<String> catalogIdSet = new HashSet<>();
        Set<String> objectiveIdSet = new HashSet<>();
        for (Map<ObjectiveConfigType, List<NewHomeworkBookInfo>> bookMap : context.getGroupPracticesBooksMap().values()) {
            if (MapUtils.isNotEmpty(bookMap)) {
                bookMap.values()
                        .stream()
                        .filter(CollectionUtils::isNotEmpty)
                        .flatMap(Collection::stream)
                        .forEach(bookInfo -> {
                            catalogIdSet.add(bookInfo.getBookId());
                            catalogIdSet.add(bookInfo.getUnitGroupId());
                            catalogIdSet.add(bookInfo.getUnitId());
                            catalogIdSet.add(bookInfo.getLessonId());
                            catalogIdSet.add(bookInfo.getSectionId());
                            objectiveIdSet.add(bookInfo.getObjectiveId());
                        });
            }
        }
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(catalogIdSet);
        Map<String, TeachingObjective> teachingObjectiveMap = teachingObjectiveLoaderClient.loadTeachingObjectsByIds(objectiveIdSet);
        for (Map<ObjectiveConfigType, List<NewHomeworkBookInfo>> bookMap : context.getGroupPracticesBooksMap().values()) {
            if (MapUtils.isNotEmpty(bookMap)) {
                bookMap.values()
                        .stream()
                        .filter(CollectionUtils::isNotEmpty)
                        .flatMap(Collection::stream)
                        .forEach(bookInfo -> {
                            String bookId = bookInfo.getBookId();
                            String unitGroupId = bookInfo.getUnitGroupId();
                            String unitId = bookInfo.getUnitId();
                            String lessonId = bookInfo.getLessonId();
                            String sectionId = bookInfo.getSectionId();
                            String objectiveId = bookInfo.getObjectiveId();

                            if (bookId != null && newBookCatalogMap.containsKey(bookId)) {
                                NewBookCatalog book = newBookCatalogMap.get(bookId);
                                bookInfo.setBookName(book.getName());
                            }

                            if (unitGroupId != null && newBookCatalogMap.containsKey(unitGroupId)) {
                                NewBookCatalog unitGroup = newBookCatalogMap.get(unitGroupId);
                                bookInfo.setUnitGroupName(unitGroup.getName());
                            }

                            if (unitId != null && newBookCatalogMap.containsKey(unitId)) {
                                NewBookCatalog unit = newBookCatalogMap.get(unitId);
                                bookInfo.setUnitName(Objects.equals(Subject.ENGLISH.getId(), unit.getSubjectId()) ? unit.getAlias() : unit.getName());
                            }

                            if (lessonId != null && newBookCatalogMap.containsKey(lessonId)) {
                                NewBookCatalog lesson = newBookCatalogMap.get(lessonId);
                                bookInfo.setLessonName(lesson.getName());
                            }

                            if (sectionId != null && newBookCatalogMap.containsKey(sectionId)) {
                                NewBookCatalog section = newBookCatalogMap.get(sectionId);
                                bookInfo.setSectionName(section.getName());
                            }

                            if (objectiveId != null && teachingObjectiveMap.containsKey(objectiveId)) {
                                TeachingObjective teachingObjective = teachingObjectiveMap.get(objectiveId);
                                bookInfo.setObjectiveName(teachingObjective.getName());
                            }

                            if (StringUtils.equalsIgnoreCase(objectiveId, NewHomeworkConstants.TEACHER_HOME_INDEX_RECOMMEND_OBJECTIVE_ID)) {
                                bookInfo.setObjectiveName("首页推荐");
                            }
                        });
            }
        }
    }
}
