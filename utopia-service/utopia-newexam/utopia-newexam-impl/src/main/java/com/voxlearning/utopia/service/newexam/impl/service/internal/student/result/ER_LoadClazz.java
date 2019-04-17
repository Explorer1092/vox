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

package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_LoadClazz extends SpringContainerSupport implements NewExamResultTask {
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;

    @Override
    public void execute(NewExamResultContext context) {
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(context.getUserId());
        if (clazz == null) {
            logger.error("Student {} clazz not found", context.getUserId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            return;
        }
        context.setClazz(clazz);
        context.setClazzId(clazz.getId());
    }
}
