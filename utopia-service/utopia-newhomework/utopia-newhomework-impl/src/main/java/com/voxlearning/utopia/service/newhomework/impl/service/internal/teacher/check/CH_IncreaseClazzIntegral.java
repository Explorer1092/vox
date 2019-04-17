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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.service.integral.ClazzIntegralService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/24
 */
@Named
public class CH_IncreaseClazzIntegral extends SpringContainerSupport implements CheckHomeworkTask {

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

    @ImportService(interfaceClass = ClazzIntegralService.class) private ClazzIntegralService clazzIntegralService;

    @Override
    public void execute(CheckHomeworkContext context) {
        CheckHomeworkIntegralDetail detail = context.getDetail();
        if (detail == null || detail.getClazzIntegral() <= 0) return;

        ClazzIntegralHistory history = new ClazzIntegralHistory();
        history.setGroupId(context.getGroupId());
        history.setClazzIntegralType(ClazzIntegralType.老师检查作业获得班级学豆.getType());
        history.setIntegral(detail.getClazzIntegral());
        history.setComment(ClazzIntegralType.老师检查作业获得班级学豆.getDescription());
        history.setAddIntegralUserId(context.getTeacherId());
        MapMessage message = clazzIntegralServiceClient.getClazzIntegralService()
                .changeClazzIntegral(history)
                .getUninterruptibly();
        if (!message.isSuccess()) {
            logger.warn("teacher check homework add clazz integral faild. groupId {}", context.getGroupId());
        }
    }
}
