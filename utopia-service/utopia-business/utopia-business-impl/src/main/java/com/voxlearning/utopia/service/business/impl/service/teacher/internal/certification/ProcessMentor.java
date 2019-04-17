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

import com.voxlearning.utopia.service.business.impl.service.teacher.DeprecatedMentorService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 教师认证后给他的mentor奖励
 *
 * @author RuiBao
 * @version 0.1
 * @since 5/14/2015
 */
@Named
public class ProcessMentor extends AbstractTcpProcessor {
    @Inject private DeprecatedMentorService deprecatedMentorService;

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        if (context.isRewardSkipped()) return context;

        Teacher teacher = context.getUser();
        deprecatedMentorService.addRewardToMentorForAuth(teacher.getId());
        return context;
    }
}
