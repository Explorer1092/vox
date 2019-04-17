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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 获取班级信息
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_LoadClazz extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(context.getUserId());
        if (clazz == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST,
                    "op", "student vacation homework result"
            ));
            logger.error("Student {} clazz not found", context.getUserId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            return;
        }
        context.setClazz(clazz);
        context.setClazzId(clazz.getId());
    }
}
