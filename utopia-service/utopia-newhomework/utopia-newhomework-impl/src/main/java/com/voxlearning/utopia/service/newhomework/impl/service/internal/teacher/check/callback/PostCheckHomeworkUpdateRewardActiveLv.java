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
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 解冻教师奖品中心
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/2/25
 */
@Named
public class PostCheckHomeworkUpdateRewardActiveLv extends SpringContainerSupport implements PostCheckHomework {
    @Inject private UserServiceClient userServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        Teacher teacher = context.getTeacher();
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
        int teacherRal = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getRewardActiveLevel());
        if (teacherRal == 0) {
            userServiceClient.updateRewardActiveLevel(teacher.getId(), 1);
        }
    }
}
