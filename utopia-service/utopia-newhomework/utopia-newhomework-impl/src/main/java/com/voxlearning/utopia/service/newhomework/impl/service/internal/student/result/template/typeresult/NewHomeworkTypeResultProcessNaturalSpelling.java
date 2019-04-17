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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

/**
 * @author zhangbin
 * @since 2017/7/3 14:52
 */

@Named
public class NewHomeworkTypeResultProcessNaturalSpelling extends NewHomeworkTypeResultProcessTemplate {

    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.NATURAL_SPELLING;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }

        List<String> lessonIds = new ArrayList<>();
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        Map<String, List<Map<String, Object>>> lessonCategoriesMap = new HashMap<>();

        if (MapUtils.isNotEmpty(appResult)) {
            for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
                String lessonId = nraa.getLessonId();
                if (StringUtils.isNotBlank(lessonId)) {
                    lessonIds.add(nraa.getLessonId());
                }
            }
        }



        if (MapUtils.isNotEmpty(appResult)) {
            for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
                String lessonId = nraa.getLessonId();
                PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(nraa.getPracticeId()));
                if (practiceType != null) {
                    boolean finished = nraa.isFinished();
                    List<Map<String, Object>> categories = lessonCategoriesMap.get(lessonId);
                    if (CollectionUtils.isEmpty(categories)) {
                        categories = new ArrayList<>();
                    }
                    categories.add(MapUtils.m(
                            "categoryName", practiceType.getCategoryName(),
                            "finished", finished
                            )
                    );
                    lessonCategoriesMap.put(lessonId, categories);
                }
            }
        }

        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, List<String>> unitLessonsMap = new LinkedHashMap<>();
        Map<String, String> lessonIdUnitIdMap = NewHomeworkUtils.handleLessonIdToUnitId(lessonMap);
        if (MapUtils.isNotEmpty(lessonIdUnitIdMap)) {
            for (Map.Entry<String, String> entry : lessonIdUnitIdMap.entrySet()) {
                List<String> lessonIdsList = unitLessonsMap.get(entry.getValue());
                if (CollectionUtils.isEmpty(lessonIdsList)) {
                    lessonIdsList = new ArrayList<>();
                }
                lessonIdsList.add(entry.getKey());
                unitLessonsMap.put(entry.getValue(), lessonIdsList);
            }
        }

        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitLessonsMap.keySet());
        List<Map<String, Object>> results = new ArrayList<>();
        handleUnitLessonsMap(
                unitLessonsMap,
                unitMap,
                lessonMap,
                lessonCategoriesMap,
                results);
        return MapMessage.successMessage().add("results", results);
    }

    private void handleUnitLessonsMap(Map<String, List<String>> unitLessonsMap,
                                      Map<String, NewBookCatalog> unitMap,
                                      Map<String, NewBookCatalog> lessonMap,
                                      Map<String, List<Map<String, Object>>> lessonCategorysMap,
                                      List<Map<String, Object>> results) {
        for (Map.Entry<String, List<String>> unitLessonEntry : unitLessonsMap.entrySet()) {
            NewBookCatalog unit = unitMap.get(unitLessonEntry.getKey());
            List<Map<String, Object>> lessons = new ArrayList<>();
            if (unit == null) {
                continue;
            }
            for (String lessonId : unitLessonEntry.getValue()) {
                NewBookCatalog lesson = lessonMap.get(lessonId);
                List<Map<String, Object>> categories = lessonCategorysMap.get(lessonId);
                if (lesson == null || categories == null) {
                    continue;
                }
                lessons.add(MapUtils.m(
                        "lessonName", lesson.getAlias(),
                        "categoryTotalNum", categories.size(),
                        "categories", categories
                        )
                );
            }
            if (CollectionUtils.isNotEmpty(lessons)) {
                results.add(MapUtils.m(
                        "type", ObjectiveConfigType.NATURAL_SPELLING,
                        "typeName", ObjectiveConfigType.NATURAL_SPELLING.getValue(),
                        "unitName", unit.getAlias(),
                        "lessons", lessons
                        )
                );
            }
        }
    }
}
