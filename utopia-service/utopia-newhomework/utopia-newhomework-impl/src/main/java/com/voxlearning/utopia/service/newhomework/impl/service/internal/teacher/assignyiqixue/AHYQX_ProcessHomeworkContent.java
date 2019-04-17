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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.AssignLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template.NewHomeworkContentProcessFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template.NewHomeworkContentProcessTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
public class AHYQX_ProcessHomeworkContent extends SpringContainerSupport implements AssignYiQiXueHomeworkTask {

    @Inject private NewHomeworkContentProcessFactory newHomeworkContentProcessFactory;
    @Inject private NewContentLoaderClient newContentLoaderClient;

    @Override
    public void execute(AssignHomeworkContext context) {
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
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT,
                    "op", "assign 17xue homework"
            ));
            context.errorResponse("17xue homework content is error homeworkSource:{}, errorInfo {}", JsonUtils.toJson(homeworkSource), ex);
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
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT,
                        "op", "assign 17xue homework"
                ));
                context.errorResponse("17xue homework practices is empty homeowrkSource:{}", JsonUtils.toJson(homeworkSource));
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
                context.setTerminateTask(true);
            } else {
                // 对practices按照作业形式重新排序，过滤掉不属于这个学科的作业形式
                List<ObjectiveConfigType> subjectTypeList = ObjectiveConfigType.getSubjectTypes(context.getTeacher().getSubject());
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


    @SuppressWarnings("unchecked")
    private void processBooks(AssignHomeworkContext context, HomeworkSource homeworkSource) {
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

                    if (book.containsKey("includeQuestions")) {
                        bookInfo.setQuestions(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includeQuestions")), String.class));
                    }

                    if (book.containsKey("includePictureBooks")) {
                        bookInfo.setPictureBooks(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includePictureBooks")), String.class));
                    }

                    if (book.containsKey("includeVideos")) {
                        bookInfo.setVideos(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includeVideos")), String.class));
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
        // 填充name字段
        Set<String> catalogIdSet = new HashSet<>();
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
                        });
            }
        }
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(catalogIdSet);
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
                        });
            }
        }
    }
}
