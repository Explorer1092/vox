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

import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;

/**
 * @author Rui.Bao
 * @since 2014-08-08 11:44 AM
 */
@Named
public class LoadStudentRewardsCard extends AbstractStudentIndexDataLoader {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();
        Long studentId = student.getId();

        // 上次查看到本次查看期间或的的未读评语数量
        int unreadCommentCount = (int) homeworkCommentLoaderClient.studentUnreadHomeworkCommentCount(studentId);
        context.getParam().put("commentCount", unreadCommentCount);

        // 上次查看到本次查看期间获得的学习类（作业，测验，智慧教室）积分数
        List<IntegralHistory> integralHistories = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .StudentRewardCardIntegralHistoryCache_load(student.getId(), false)
                .getUninterruptibly();
        int beanCount = 0;
        if (integralHistories != null) {
            beanCount = integralHistories.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(IntegralHistory::getIntegralValue)
                    .sum();
        }

        context.getParam().put("beanCount", beanCount);
        context.getParam().put("showRewardsCard", unreadCommentCount > 0 || beanCount > 0);
        return context;
    }
}