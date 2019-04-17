/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.MATH;

/**
 * 学生App数学作业补做卡
 * Created by Shuai Huan on 2016/1/20.
 */
@Named
public class LoadStudentAppMakeUpHomeworkCard_Math extends AbstractStudentAppIndexDataLoader {

    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {

        if (VersionUtil.compareVersion(context.ver, "1.9.0") < 0) {
            return context;
        }

        if (context.__ignoreMakeUpMathHomework) {
            return context;
        }

        NewHomework.Location location = context.__mathNormalHomeworkLocations.stream()
                .filter(t -> t.getClazzGroupId() == 0 || context.__groupIds.contains(t.getClazzGroupId()))
                .filter(t -> t.getStartTime() != 0 && t.getStartTime() <= context.timestamp)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .findFirst()
                .orElse(null);
        if (location == null) {
            return context;
        }

        Set<Long> finishedStudentIds = new HashSet<>();
        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
        if (accomplishment != null && accomplishment.getDetails() != null) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }

        if (!finishedStudentIds.contains(context.getStudent().getId())) {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(location.getId());
            List<String> types = newHomework.getPractices().stream().map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().name()).collect(Collectors.toList());
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("homeworkId", location.getId());
            resultMap.put("homeworkType", MATH);
            resultMap.put("endDate", location.getEndTime());
            resultMap.put("desc", "数学补做");
            resultMap.put("makeup", true);
            resultMap.put("types", types);
            resultMap.put("startComment", "开始作业");
            context.__makeUpHomeworkCards.add(resultMap);
        }

        return context;
    }
}
