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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification;


import com.voxlearning.utopia.service.certification.client.TeacherCertificationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 处理教师认证奖励
 *
 * @author RuiBao
 * @version 0.1
 * @since 4/11/2015
 */
@Named
public class ProcessCertificationReward extends AbstractTcpProcessor {

    @Inject private TeacherCertificationServiceClient teacherCertificationServiceClient;

    @Override
    protected TeacherCertificationContext doProcess(TeacherCertificationContext context) {
        if (context.isRewardSkipped()) return context;

        Teacher teacher = context.getUser();

        // 初始化教师认证奖励
        TeacherCertificationReward tcr = TeacherCertificationReward.newInstance(teacher.getId(), new Date(), new Date(), 0);
        teacherCertificationServiceClient.getTeacherCertificationService()
                .insertTeacherCertificationReward(tcr)
                .awaitUninterruptibly();

        return context;
    }

}
