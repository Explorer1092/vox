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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignlivecast;

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
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template.NewHomeworkContentProcessFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template.NewHomeworkContentProcessTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Named
public class AHL_ProcessHomeworkContent extends SpringContainerSupport implements AssignLiveCastHomeworkTask {

    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private NewHomeworkContentProcessFactory newHomeworkContentProcessFactory;

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
                    "op", "assign homework"
            ));
            context.errorResponse("homework content is error homeworkSource:{}, errorInfo {}", JsonUtils.toJson(homeworkSource), ex);
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
                        "op", "assign homework"
                ));
                context.errorResponse("homework practices is empty homeowrkSource:{}", JsonUtils.toJson(homeworkSource));
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
                context.setTerminateTask(true);
            }
        }
    }


    private void processBooks(AssignHomeworkContext context, HomeworkSource homeworkSource) {
        Map<String, List<Map>> practicesBooksMap = JsonUtils.fromJson(JsonUtils.toJson(homeworkSource.get("books")), Map.class);

        for (String objectiveConfigTypeStr : practicesBooksMap.keySet()) {
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
            List<NewHomeworkBookInfo> bookInfos = new ArrayList<>();
            List<Map> books = JsonUtils.fromJsonToList(JsonUtils.toJson(practicesBooksMap.get(objectiveConfigTypeStr)), Map.class);
            Set<String> catalogIdSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(books)) {
                books.forEach(book -> {
                    catalogIdSet.add(SafeConverter.toString(book.get("bookId")));
                    catalogIdSet.add(SafeConverter.toString(book.get("unitGroupId")));
                    catalogIdSet.add(SafeConverter.toString(book.get("unitId")));
                    catalogIdSet.add(SafeConverter.toString(book.get("lessonId")));
                    catalogIdSet.add(SafeConverter.toString(book.get("sectionId")));
                });
            }
            Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(catalogIdSet);
            for (Map book : books) {
                NewHomeworkBookInfo bookInfo = new NewHomeworkBookInfo();
                if (book.containsKey("bookId")) {
                    String bookId = SafeConverter.toString(book.get("bookId"));
                    bookInfo.setBookId(bookId);
                    if (newBookCatalogMap.get(bookId) != null) {
                        bookInfo.setBookName(newBookCatalogMap.get(bookId).getName());
                    }
                }
                if (book.containsKey("unitId")) {
                    String unitId = SafeConverter.toString(book.get("unitId"));
                    bookInfo.setUnitId(unitId);
                    NewBookCatalog newBookCatalog = newBookCatalogMap.get(unitId);
                    if (newBookCatalog != null) {
                        bookInfo.setUnitName(Objects.equals(Subject.ENGLISH.getId(), newBookCatalog.getSubjectId())
                                ? newBookCatalog.getAlias() : newBookCatalog.getName());
                    }
                }

                if (book.containsKey("lessonId")) {
                    String lessonId = SafeConverter.toString(book.get("lessonId"));
                    bookInfo.setLessonId(lessonId);
                    if (newBookCatalogMap.get(lessonId) != null) {
                        bookInfo.setLessonName(newBookCatalogMap.get(lessonId).getName());
                    }
                }

                if (book.containsKey("unitGroupId")) {
                    String unitGroupId = SafeConverter.toString(book.get("unitGroupId"));
                    bookInfo.setUnitGroupId(unitGroupId);
                    if (newBookCatalogMap.get(unitGroupId) != null) {
                        bookInfo.setUnitGroupName(newBookCatalogMap.get(unitGroupId).getName());
                    }
                }

                if (book.containsKey("sectionId")) {
                    String sectionId = SafeConverter.toString(book.get("sectionId"));
                    bookInfo.setSectionId(sectionId);
                    if (newBookCatalogMap.get(sectionId) != null) {
                        bookInfo.setSectionName(newBookCatalogMap.get(sectionId).getName());
                    }
                }

                if (book.containsKey("includeQuestions")) {
                    bookInfo.setQuestions(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includeQuestions")), String.class));
                }

                if (book.containsKey("includePictureBooks")) {
                    bookInfo.setPictureBooks(JsonUtils.fromJsonToList(JsonUtils.toJson(book.get("includePictureBooks")), String.class));
                }
                bookInfos.add(bookInfo);

            }
            if (CollectionUtils.isNotEmpty(bookInfos)) {
                for (Long groupId : context.getGroupIds()) {
                    LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap().getOrDefault(groupId, new LinkedHashMap<>());
                    practiceBooksMap.put(objectiveConfigType, bookInfos);
                    context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                }
            }
        }
    }
}
