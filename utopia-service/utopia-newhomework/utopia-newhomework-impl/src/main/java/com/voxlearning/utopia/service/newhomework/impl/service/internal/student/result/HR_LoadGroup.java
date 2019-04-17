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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 获取组信息
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/1/14
 */
@Named
public class HR_LoadGroup extends SpringContainerSupport implements HomeworkResultTask {

    @Inject private RaikouSDK raikouSDK;

    @Override
    public void execute(HomeworkResultContext context) {
        processGroup(context);
    }

    private void processGroup(HomeworkResultContext context) {
        GroupMapper homeworkClazzGroup = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                .loadGroupDetail(context.getClazzGroupId(), false)
                .firstOrNull();
        if (homeworkClazzGroup == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST,
                    "mod3", context.getSubject(),
                    "mod4", context.getClazzGroupId(),
                    "op", "student homework result"
            ));
            context.errorResponse("作业班组不存在");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
            return;
//            logger.warn("homeworkGroup {} doesn't belong to student {}, studentGroup:{}, subject:{}", context.getClazzGroupId(), context.getUserId(), group.getId(), context.getSubject());
        }
        context.setClazzGroup(homeworkClazzGroup);
    }
}
