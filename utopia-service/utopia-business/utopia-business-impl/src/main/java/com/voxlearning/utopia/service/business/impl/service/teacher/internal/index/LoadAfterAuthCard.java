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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.service.certification.client.TeacherCertificationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherCertificationReward;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.TeacherPrivilegeCardMapper;
import com.voxlearning.utopia.service.user.base.gray.TeacherGrayFunctionManager;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

/**
 * 认证后一个月以内显示的卡片
 * -- 认证特权卡片
 * -- 带来更多学生卡片
 *
 * @author RuiBao
 * @version 0.1
 * @since 7/17/2015
 */
@Named
public class LoadAfterAuthCard extends AbstractTeacherIndexDataLoader {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private TeacherCertificationServiceClient teacherCertificationServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        if (context.isSkipNextAll()) return context;

        Teacher teacher = context.getTeacher();
        if (teacher.fetchCertificationState() != SUCCESS) return context;

        TeacherCertificationReward tcr = teacherCertificationServiceClient.getTeacherCertificationService()
                .loadTeacherCertificationReward(teacher.getId())
                .getUninterruptibly();
        if (tcr == null) return context;

        context.getParam().put("tcrshow", "DO_NOT_SHOW");

//        else if (new DateRange(date_start, date_end).contains(tcr.getCreateDatetime())) {
//            if (!teacher.isPrimarySchool()) return context;
//            if (teacher.getSubject() != Subject.MATH) return context;
//            if (!manager.isWebGrayFunctionAvailable(teacherDetail, "Certification", "Reward")) return context;
//            if (manager.isWebGrayFunctionAvailable(teacherDetail, "PHONEFEE", "NEVERSHOW")) return context;
//            if (new Date().after(DateUtils.calculateDateDay(tcr.getCreateDatetime(), 30)) || tcr.getPhase() >= 3)
//                return context;
//            context.getParam().put("phase", tcr.getPhase());
//            context.getParam().put("pr", tcr.getPostReward());
//            context.getParam().put("countDown30", 30 - DateUtils.dayDiff(new Date(), tcr.getCreateDatetime()));
//            context.getParam().put("tcrshow", "SHOW_NEW");
//        } else {
//            context.getParam().put("tcrshow", "DO_NOT_SHOW");
//        }

        // 认证特权卡片
        TeacherPrivilegeCardMapper mapper = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherPrivilegeCardCacheManager_load(teacher.getId(), teacher.getSubject())
                .getUninterruptibly();
        context.getParam().put("privilege", mapper != null && !mapper.noNeedToShow());

        return context;
    }
}
