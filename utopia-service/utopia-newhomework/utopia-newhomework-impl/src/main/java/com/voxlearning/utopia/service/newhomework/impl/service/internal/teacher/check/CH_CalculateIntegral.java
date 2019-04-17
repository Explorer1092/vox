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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.TeacherCheckHomeworkIntegralCalculator;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/27
 */
@Named
public class CH_CalculateIntegral extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public void execute(CheckHomeworkContext context) {
        NewAccomplishment accomplishment = context.getAccomplishment();
        if (accomplishment == null || accomplishment.size() <= 0) {
            context.setDetail(new CheckHomeworkIntegralDetail(context.getHomeworkId(), 0, 0D, 0, 0));
            return;
        }

        // 在正常时间内完成作业的学生数量
        int count = (int) accomplishment.getDetails().values().stream().filter(detail -> !detail.isRepairTrue()).count();
        if (count <= 0) {
            context.setDetail(new CheckHomeworkIntegralDetail(context.getHomeworkId(), 0, 0D, 0, 0));
            return;
        }

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(context.getTeacherId());

        // 计算本周截止到现在，该老师在这个班级中一共检查了几次作业，time从0开始(用于每周>3次检查的作业都不给学豆奖励)
        int time = newHomeworkCacheService.getCheckHomeworkIntegralCacheManager().weekCheckTime(context.getTeacherId(),
                context.getGroupId(), context.getHomeworkType());

        TeacherCheckHomeworkIntegralCalculator calculator = getBean(TeacherCheckHomeworkIntegralCalculator.class);
        calculator.setTime(time);
        calculator.setCount(count);
        calculator.setTeacher(teacher);
        calculator.setHomeworkId(context.getHomeworkId());

        context.setDetail(calculator.calculate());
        context.setWeekCheckTime(time);
    }
}
