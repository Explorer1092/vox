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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.MiscServiceImpl;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Summer Yang on 2015/11/23.
 * <p>
 * 获取老师首页卡片 （临时活动类）
 */
@Named
public class LoadActivityCard extends AbstractTeacherIndexDataLoader {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;
    @Inject private MiscServiceImpl miscService;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        if (context.isSkipNextAll()) return context;
        // 开学大礼包卡片
        if (NewSchoolYearActivity.isInTermBeginPeriod() || RuntimeMode.le(Mode.STAGING)) {
            context.getParam().put("showTermBeginCard", true);
            context.getParam().put("adjustFlag", asyncBusinessCacheService.TeacherAdjustClazzRemindCacheManager_done(context.getTeacher().getId()).getUninterruptibly());
            context.getParam().put("homeworkFlag",
                    asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                            .unflushable_getUserBehaviorCount(UserBehaviorType.TERM_BEGIN_TEACHER_HOMEWORK_COUNT, context.getTeacher().getId())
                            .getUninterruptibly() > 0);

        }
        if (NewSchoolYearActivity.isInScholarshipPeriod()) {
            context.getParam().put("showScholarship", miscService.showScholarshipEnter(context.getTeacher()));
        }
        return context;
    }
}
