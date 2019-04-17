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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理新语文读背练习该类型的学生作业中间结果页模板
 *
 * @author zhangbin
 * @since 2017/7/2 21:55
 */

@Named
public class NewHomeworkTypeResultProcessNewReadRecite extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp()  {
        return NewHomeworkTypeResultProcessTemp.NEW_READ_RECITE;
    }

    @Override
    public MapMessage processHomeworkTypeResult(ObjectiveConfigType objectiveConfigType, BaseHomeworkResult baseHomeworkResult) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> resultMap = baseHomeworkResult.getPractices();
        if (resultMap == null || resultMap.get(objectiveConfigType) == null) {
            return MapMessage.errorMessage().setInfo("未完成该作业类型").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_RESULT_NOT_EXIST);
        }
        NewHomeworkResultAnswer resultAnswer = resultMap.get(objectiveConfigType);
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appResult = resultAnswer.getAppAnswers();
        List<String> processResultIds = new ArrayList<>();
        Set<String> lessonIds = new HashSet<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            if (MapUtils.isEmpty(nraa.getAnswers())) {
                logger.error("processHomeworkTypeResult NEW_READ_RECITE error, homeworkId:{}, studentId:{}",
                        baseHomeworkResult.getHomeworkId(),
                        baseHomeworkResult.getUserId());
            } else {
                processResultIds.addAll(nraa.getAnswers().values());
            }
            lessonIds.add(nraa.getLessonId());
        }
        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        List<Map> readList = new ArrayList<>();
        List<Map> reciteList = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            Map<String, Object> map = new HashMap<>();
            NewBookCatalog newBookCatalog = lessonMap.get(nraa.getLessonId());
            if (newBookCatalog == null)
                continue;
            String correction;
            if (nraa.getCorrection() != null) {
                correction = nraa.getCorrection().getDescription();
            } else if (nraa.getReview() != null) {
                correction = "阅";
            } else {
                correction = null;
            }
            List<String> audios = new LinkedList<>();

            //将音频的播放顺序按照学生答题录音时的顺序展示
            List<String> processIds = nraa.getAnswers()
                    .values()
                    .stream()
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
            for (String processResultId : processIds) {
                NewHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) continue;
                if (CollectionUtils.isNotEmpty(nhpr.getFiles())) {
                    audios.addAll(nhpr
                            .getFiles()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(NewHomeworkQuestionFileHelper::getFileUrl)
                            .collect(Collectors.toList()));
                }
            }
            map.put("lessonName", SafeConverter.toString(newBookCatalog.getName()));
            map.put("correction", correction);
            map.put("audios", audios);
            if (QuestionBoxType.READ.equals(nraa.getQuestionBoxType())) {
                readList.add(map);
            } else {
                reciteList.add(map);
            }
        }
        return MapMessage.successMessage()
                .add("readList", readList)
                .add("reciteList", reciteList);
    }
}
