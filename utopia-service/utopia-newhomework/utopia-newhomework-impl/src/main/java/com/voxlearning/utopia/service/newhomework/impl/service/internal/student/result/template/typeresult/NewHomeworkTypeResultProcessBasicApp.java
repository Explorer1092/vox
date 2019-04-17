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
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;

/**
 * @author zhangbin
 * @since 2017/7/3 14:43
 */

@Named
public class NewHomeworkTypeResultProcessBasicApp extends NewHomeworkTypeResultProcessTemplate {

    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.BASIC_APP;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }

        List<String> processResultIds = new ArrayList<>();
        List<String> lessonIds = new ArrayList<>();

        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        if(MapUtils.isNotEmpty(appResult)) {
            for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
                if (MapUtils.isEmpty(nraa.getAnswers())) {
                    logger.error("processHomeworkTypeResult BASIC_APP error, homeworkId:{}, studentId:{}",
                            baseHomeworkResult.getHomeworkId(),
                            baseHomeworkResult.getUserId());
                } else {
                    processResultIds.addAll(nraa.getAnswers().values());
                }
                if (StringUtils.isNotBlank(nraa.getLessonId())) {
                    lessonIds.add(nraa.getLessonId());
                } else {
                    LogCollector.info("backend-general", MapUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "usertoken", baseHomeworkResult.getUserId(),
                            "mod1", JsonUtils.toJson(baseHomeworkResult),
                            "op", "BasicAppResult lessonId is empty"
                    ));
                }
            }
        }


        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        Map<String, List<Map<String, Object>>> lessonCategorysMap = new HashMap<>();
        if(MapUtils.isNotEmpty(appResult)) {
            for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
                String lessonId = nraa.getLessonId();
                PracticeType practiceType = practiceLoaderClient.loadPractice(SafeConverter.toLong(nraa.getPracticeId()));
                if (practiceType != null) {
                    int errorCount = 0;
                    int rightCount = 0;
                    boolean finished = nraa.isFinished();
                    if (MapUtils.isNotEmpty(nraa.getAnswers())) {
                        for (String processResultId : nraa.getAnswers().values()) {
                            NewHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                            if (nhpr == null) {
                                continue;
                            }
                            if (practiceType.getNeedRecord()) {
                                if ((nhpr.getAppOralScoreLevel() != null && !AppOralScoreLevel.D.equals(nhpr.getAppOralScoreLevel()))
                                        || (nhpr.getAppOralScoreLevel() == null && nhpr.getScore() >= 40)) {
                                    rightCount++;
                                } else {
                                    errorCount++;
                                }
                            } else {
                                if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                                    rightCount++;
                                } else {
                                    errorCount++;
                                }
                            }
                        }
                    }
                    List<Map<String, Object>> categorys = lessonCategorysMap.get(lessonId);
                    if (CollectionUtils.isEmpty(categorys)) {
                        categorys = new ArrayList<>();
                    }
                    categorys.add(MapUtils.m(
                            "catetoryName", practiceType.getCategoryName(),
                            "needRecord", practiceType.getNeedRecord(),
                            "rightCount", rightCount,
                            "errorCount", errorCount,
                            "finished", finished
                            )
                    );
                    lessonCategorysMap.put(lessonId, categorys);
                }
            }
        }
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        Map<String, List<String>> unitLessonsMap = new LinkedHashMap<>();
        Map<String, String> lessonUnitMap = NewHomeworkUtils.handleLessonIdToUnitId(lessonMap);
        for (Map.Entry<String, String> lessonUnitEntry : lessonUnitMap.entrySet()) {
            List<String> lids = unitLessonsMap.get(lessonUnitEntry.getValue());
            if (CollectionUtils.isEmpty(lids)) {
                lids = new ArrayList<>();
            }
            lids.add(lessonUnitEntry.getKey());
            unitLessonsMap.put(lessonUnitEntry.getValue(), lids);
        }
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitLessonsMap.keySet());
        List<Map<String, Object>> results = new ArrayList<>();
        handleUnitLessonsMap(unitLessonsMap,
                unitMap,
                lessonMap,
                lessonCategorysMap,
                results);
        return MapMessage.successMessage().add("datas", results);
    }

    private void handleUnitLessonsMap(Map<String, List<String>> unitLessonsMap,
                                      Map<String, NewBookCatalog> unitMap,
                                      Map<String, NewBookCatalog> lessonMap,
                                      Map<String, List<Map<String, Object>>> lessonCategorysMap,
                                      List<Map<String, Object>> results) {
        for (Map.Entry<String, List<String>> unitLessonEntry : unitLessonsMap.entrySet()) {
            NewBookCatalog unit = unitMap.get(unitLessonEntry.getKey());
            List<Map<String, Object>> lessonObjs = new ArrayList<>();
            if (unit == null) {
                continue;
            }
            for (String lessonId : unitLessonEntry.getValue()) {
                NewBookCatalog lesson = lessonMap.get(lessonId);
                List<Map<String, Object>> categorys = lessonCategorysMap.get(lessonId);
                if (lesson == null || categorys == null) {
                    continue;
                }
                lessonObjs.add(MapUtils.m(
                        "lessonName", lesson.getAlias(),
                        "categorys", categorys,
                        "categoryTotalNum",categorys.size()
                        )
                );
            }
            if (CollectionUtils.isNotEmpty(lessonObjs)) {
                results.add(MapUtils.m(
                        "unitName", unit.getAlias(),
                        "lessons", lessonObjs
                        )
                );
            }
        }
    }
}
