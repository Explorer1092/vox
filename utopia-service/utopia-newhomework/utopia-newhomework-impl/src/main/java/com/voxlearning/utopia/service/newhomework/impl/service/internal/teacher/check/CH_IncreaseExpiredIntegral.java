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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.TEACHER_EXPIRED_INTEGRAL_FREE;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.DEDUCT_TEACHER_EXPIRED_INTEGRAL;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.TEACHER_EXPIRED_INTEGRAL_RETRIEVE;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/4/22
 */
@Named
public class CH_IncreaseExpiredIntegral extends SpringContainerSupport implements CheckHomeworkTask {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    public void execute(CheckHomeworkContext context) {
//        // 1，2，7，8月份不用执行
        MonthRange mr = MonthRange.current();
        String current = DateUtils.dateToString(mr.getStartDate(), "M");
//        if (Arrays.asList("1", "2", "7", "8").contains(current)) return;

        int boundary = 8;
        if (RuntimeMode.le(Mode.STAGING)) boundary = 1;

        NewAccomplishment accomplishment = context.getAccomplishment();
        if (accomplishment == null || accomplishment.size() < boundary) return;

        // 如果是包班制获得主副账号的id列表
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(context.getTeacherId());
        if(mainTeacherId == null || mainTeacherId == 0L)
            mainTeacherId = context.getTeacherId();

        List<Long> allTeacherIds = new ArrayList<>();
        allTeacherIds.add(mainTeacherId);

        allTeacherIds.addAll(teacherLoaderClient.loadSubTeacherIds(mainTeacherId));

        // 查看是否已经获得到了福利
        String month = DateUtils.dateToString(mr.getStartDate(), DateUtils.FORMAT_SQL_DATE);
        int count = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherExpiredIntegralFreeCacheManager_fetchCount(allTeacherIds, month)
                .getUninterruptibly()
                .values()
                .stream()
                .mapToInt(t -> t)
                .sum();

        // 在正常时间内完成作业的学生数量
        int fc = (int) accomplishment.getDetails().values().stream().filter(detail -> !detail.isRepairTrue()).count();
        if (fc < boundary) return;

        // 增加次数
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherExpiredIntegralFreeCacheManager_increaseCurrentCount(context.getTeacher().getId())
                .awaitUninterruptibly();

        if (count <= 2) return;

        // 如果增加完是4的话，把上个月扣除的再加回来
        // 如果是3月份，查询123月的积分历史，如果是9月份，查询789月的历史，其余只需要查当月的
        Date date = mr.getStartDate();
        if (Arrays.asList("3", "9").contains(current)) date = mr.previous().previous().getStartDate();
        final long timestamp = date.getTime();

        // 查询积分历史
        List<IntegralHistory> histories = integralHistoryLoaderClient.getIntegralHistoryLoader()
                .loadUserIntegralHistories(context.getTeacher().getId())
                .stream()
                .filter(h -> h.getCreatetime().getTime() >= timestamp)
                .filter(h -> h.getIntegralType() == DEDUCT_TEACHER_EXPIRED_INTEGRAL.getType() ||
                        h.getIntegralType() == TEACHER_EXPIRED_INTEGRAL_RETRIEVE.getType())
                .collect(Collectors.toList());

        IntegralHistory del = histories.stream().filter(h -> h.getIntegralType() == DEDUCT_TEACHER_EXPIRED_INTEGRAL.getType())
                .findFirst().orElse(null);
        IntegralHistory add = histories.stream().filter(h -> h.getIntegralType() == TEACHER_EXPIRED_INTEGRAL_RETRIEVE.getType())
                .findFirst().orElse(null);

        // 没有扣除记录，啥都不用干了
        if (del == null) return;

        // 有扣除记录，看看是否有增加记录，如果没有，加
        if (add == null) {
            String str = DateUtils.dateToString(DayRange.newInstance(timestamp).previous().getStartDate(), "M");
            IntegralHistory integralHistory = new IntegralHistory(context.getTeacher().getId(),
                    TEACHER_EXPIRED_INTEGRAL_RETRIEVE, -del.getIntegral());
            integralHistory.setComment("补回" + str + "月扣除的过期园丁豆");
            userIntegralService.changeIntegral(integralHistory);

            // 获得福利，发站内信，包班制的发给主账号
            String message = StringUtils.formatMessage("老师您好，由于您本月布置作业次数已达4次，故您已获得本月园丁豆免过期福利。" +
                            " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【点击查看】</a>",
                    "/teacher/center/mygold.vpage");
            userPopupServiceClient.createPopup(mainTeacherId)
                    .content(message)
                    .type(TEACHER_EXPIRED_INTEGRAL_FREE)
                    .category(LOWER_RIGHT)
                    .create();
        }
    }
}
