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

package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Setter;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_TEACHER;
import static java.math.BigDecimal.ROUND_UP;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/22
 */
@Named
@Scope("prototype")
public class TeacherCheckHomeworkIntegralCalculator extends NewHomeworkSpringBean {

    @Setter int time; // 表示在本次检查作业行为之前，本周本班级检查作业次数
    @Setter int count; // 在正常时间内完成作业的学生数量
    @Setter String homeworkId;
    @Setter TeacherDetail teacher;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    public CheckHomeworkIntegralDetail calculate() {
        int ti = getBasicTeacherIntegral();
        int ci = getBasicClazzIntegral();

        // 浮动教师应该获得的积分数量，0.9 ~ 1.1
        BigDecimal random = new BigDecimal(RandomUtils.nextInt(90, 110));

        //  计算浮动积分
        ti = new BigDecimal(ti)
                .multiply(random)
                .divide(new BigDecimal(100), 0, ROUND_UP)
                .intValue();

        // 此时的ti可能不是能被十整除的，所以先换算成园丁豆（向上取整）在算成积分
        ti = new BigDecimal(ti).divide(new BigDecimal(10), 0, ROUND_UP).multiply(new BigDecimal(10)).intValue();

        return new CheckHomeworkIntegralDetail(homeworkId, count,
                random.divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY).doubleValue(), ti, ci);
    }

    private int getBasicTeacherIntegral() {
        String key = time > 2 ? "ti_check_count_overflow" : "ti_check_count_" + time;
        String value = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_TEACHER.name(), key);
        return SafeConverter.toInt(value, 9) * count;
    }

    private int getBasicClazzIntegral() {
        String key = time > 2 ? "ci_check_count_overflow" : "ci_check_count_" + time;
        String value = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_TEACHER.name(), key);
        return SafeConverter.toInt(value, 1) * count;
    }
}
