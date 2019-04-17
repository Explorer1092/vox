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

package com.voxlearning.utopia.service.afenti.impl.service.processor.login;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.afenti.api.context.LoginContext;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * 获取用户基本信息
 *
 * @author Ruib
 * @since 2016/7/11
 */
@Named
public class L_LoadUserBasicInfo extends SpringContainerSupport implements IAfentiTask<LoginContext> {

    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Override
    public void execute(LoginContext context) {
        StudentDetail student = context.getStudent();
        if (null == student || null == student.getClazz()) {
            logger.error("L_LoadUserBasicInfo User {} basic info error", context.getStudent().getId());
            context.errorResponse();
            return;
        }

        Subject subject = context.getSubject();
        if (!AfentiUtils.isSubjectAvailable(subject)) {
            logger.error("L_LoadUserBasicInfo User {} subject {} error", student.getId(), subject);
            context.errorResponse();
            return;
        }

        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("L_LoadUserBasicInfo User {} subject {} error", student.getId(), subject);
            context.errorResponse();
            return;
        }

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(type.name(), student.getId())
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) {
            logger.error("L_LoadUserBasicInfo Cannot find user {} sessionKey.", student.getId());
            context.errorResponse();
            return;
        }
        VendorAppsUserRef ref = (VendorAppsUserRef) message.get("ref");

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(student.getClazz().getSchoolId())
                .getUninterruptibly();
        context.getResult().put("userId", student.getId().toString());
        context.getResult().put("sessionKey", ref.getSessionKey());
        context.getResult().put("userName", student.fetchRealnameIfBlankId());
        context.getResult().put("userImg", student.fetchImageUrl());
        context.getResult().put("clazzId", student.getClazz().getId().toString());
        context.getResult().put("clazzName", student.getClazz().formalizeClazzName());
        context.getResult().put("clazzLevel", student.getClazzLevelAsInteger());
        context.getResult().put("openPreparation", true);
        context.getResult().put("schoolId", student.getClazz().getSchoolId().toString());
        context.getResult().put("schoolName", student.getStudentSchoolName());
        context.getResult().put("schoolShortName", school == null ? "" : school.getShortName());
        context.getResult().put("env", RuntimeMode.current());
        context.getResult().put("subject", subject);
        context.getResult().put("integral", student.getUserIntegral() == null ? "0" : student.getUserIntegral().getUsable());
        context.getResult().put("domain", ProductConfig.getMainSiteBaseUrl());
        DateRange dr = new DateRange(DateUtils.stringToDate("2017-02-13 00:00:00"), DateUtils.stringToDate("2017-02-15 23:59:59"));
        context.getResult().put("awardBubbleEnabled", dr.contains(new Date()));
    }

    private boolean openPreparation(Clazz clazz) {
        int level = ClazzLevel.getLevel(clazz.getClazzLevel());

        if (EduSystemType.P5 == clazz.getEduSystem() && level < ClazzLevel.FIFTH_GRADE.getLevel())
            return true;

        if (EduSystemType.P6 == clazz.getEduSystem() && level < ClazzLevel.SIXTH_GRADE.getLevel())
            return true;

        return false;
    }
}
