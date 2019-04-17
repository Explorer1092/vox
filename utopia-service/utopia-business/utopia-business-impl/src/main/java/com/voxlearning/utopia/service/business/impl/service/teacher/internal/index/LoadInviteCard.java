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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Summer Yang on 2015/9/14.
 * 邀请卡片
 */
@Named
public class LoadInviteCard extends AbstractTeacherIndexDataLoader {

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        if (context.isSkipNextAll()) return context;
        //仅在非1、2、7、8的月份展示
        if (Arrays.asList("1", "2", "7", "8").contains(DateUtils.dateToString(new Date(), "M"))) {
            return context;
        }
        Teacher teacher = context.getTeacher();
        //查询已经邀请的人数
        int size = (int) asyncInvitationServiceClient.loadByInviter(teacher.getId()).count();
        context.getParam().put("inviteCount", size);
        //显示假的数
        long totalCount = 34806;
        Date startDate = DateUtils.stringToDate("2015-09-14 00:00:00");
        long diff = DateUtils.dayDiff(new Date(), startDate);
        totalCount = totalCount + diff * 71;
        long totalFee = totalCount * 25;
        context.getParam().put("totalInviteCount", totalCount);
        context.getParam().put("totalFee", totalFee);
        return context;
    }
}