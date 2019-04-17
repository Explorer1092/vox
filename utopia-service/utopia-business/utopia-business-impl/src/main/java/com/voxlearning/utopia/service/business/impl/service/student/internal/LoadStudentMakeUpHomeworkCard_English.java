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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.ENGLISH;

/**
 * 生成英语补做作业卡
 *
 * @author Rui.Bao
 * @author changyuan.liu
 * @author Xiaohai Zhang
 * @since Oct 10, 2015
 */
@Named
public class LoadStudentMakeUpHomeworkCard_English extends AbstractStudentIndexDataLoader {

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        if (context.__ignoreMakeUpEnglishHomework) {
            return context;
        }
        NewHomework.Location location = context.__englishNormalHomeworkLocations.stream()
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
        if (Objects.nonNull(accomplishment) && MapUtils.isNotEmpty(accomplishment.getDetails())) {
            finishedStudentIds = accomplishment.getDetails().keySet().stream()
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }
        if (!finishedStudentIds.contains(context.getStudent().getId())) {
            Map<String, Object> englishhwMap = new HashMap<>();
            englishhwMap.put("homeworkId", location.getId());
            englishhwMap.put("homeworkType", ENGLISH);
            context.__makeUpHomeworkCards.add(englishhwMap);
        }
        return context;
    }
}
