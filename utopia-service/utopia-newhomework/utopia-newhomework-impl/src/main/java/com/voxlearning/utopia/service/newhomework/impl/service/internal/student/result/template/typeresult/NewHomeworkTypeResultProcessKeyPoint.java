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

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Video;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理重难点视频专项该类型的学生作业中间结果页模板
 *
 * @author zhangbin
 * @since 2017/7/2 21:50
 */

@Named
public class NewHomeworkTypeResultProcessKeyPoint extends NewHomeworkTypeResultProcessTemplate {
    @Override
    public NewHomeworkTypeResultProcessTemp getNewHomeworkTypeResultTemp() {
        return NewHomeworkTypeResultProcessTemp.KEY_POINTS;
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
        List<String> videoIds = new ArrayList<>();
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            if (MapUtils.isEmpty(nraa.getAnswers())) {
                logger.error("processHomeworkTypeResult KEY_POINTS error, homeworkId:{}, studentId:{}",
                        baseHomeworkResult.getHomeworkId(),
                        baseHomeworkResult.getUserId());
            } else {
                processResultIds.addAll(nraa.getAnswers().values());
            }
            videoIds.add(nraa.getVideoId());
        }
        Map<String, NewHomeworkProcessResult> nhprMap = newHomeworkProcessResultLoader.loads(baseHomeworkResult.getHomeworkId(), processResultIds);
        Map<String, Video> videoMap = videoLoaderClient.loadVideoIncludeDisabled(videoIds);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalRightCount = 0;
        int totalErrorCount = 0;
        for (NewHomeworkResultAppAnswer nraa : appResult.values()) {
            Video video = videoMap.get(nraa.getVideoId());
            int errorCount = 0;
            int rightCount = 0;
            for (String processResultId : nraa.getAnswers().values()) {
                NewHomeworkProcessResult nhpr = nhprMap.get(processResultId);
                if (nhpr == null) continue;
                if (SafeConverter.toBoolean(nhpr.getGrasp())) {
                    rightCount++;
                    totalRightCount++;
                } else {
                    errorCount++;
                    totalErrorCount++;
                }

            }
            results.add(MapUtils.m("videoName", video != null ? video.getVideoName() : "未知名字",
                    "rightCount", rightCount,
                    "errorCount", errorCount));
        }
        return MapMessage.successMessage().add("datas", results)
                .add("rightCount", totalRightCount)
                .add("errorCount", totalErrorCount);
    }
}
