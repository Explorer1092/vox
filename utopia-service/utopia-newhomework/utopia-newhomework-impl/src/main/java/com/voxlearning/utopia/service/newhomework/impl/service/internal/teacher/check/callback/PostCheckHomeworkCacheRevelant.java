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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostCheckHomeworkCacheRevelant extends SpringContainerSupport implements PostCheckHomework {
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        NewAccomplishment accomplishment = context.getAccomplishment();
        if (accomplishment == null || accomplishment.size() <= 0) return;
        Set<Long> studentIds = accomplishment.getDetails().keySet()
                .stream()
                .filter(studentId -> !accomplishment.getDetails().get(studentId).isRepairTrue())
                .map(SafeConverter::toLong).collect(Collectors.toSet());

        // 记录每月完成作业人数，检查作业加一部分，补做作业加一部分
        newHomeworkCacheService.getMonthFinishHomeworkCountManager().increase(studentIds, context.getHomeworkType());
    }
}
