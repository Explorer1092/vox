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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;

import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_TeacherCertificated;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author RuiBao
 * @version 0.1
 * @since 4/11/2015
 */
@Named
public class ProcessCertificationLatest extends AbstractTcpProcessor {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        User user = context.getUser();

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(user.getId())
                .getUninterruptibly();
        if (school != null) {
            final Latest_TeacherCertificated detail = new Latest_TeacherCertificated();
            detail.setUserId(user.getId());
            detail.setUserName(user.fetchRealname());
            detail.setUserImg(user.fetchImageUrl());
            userServiceClient.createSchoolLatest(school.getId(), LatestType.TEACHER_CERTIFICATED)
                    .withDetail(detail).send();
        }

        return context;
    }
}
