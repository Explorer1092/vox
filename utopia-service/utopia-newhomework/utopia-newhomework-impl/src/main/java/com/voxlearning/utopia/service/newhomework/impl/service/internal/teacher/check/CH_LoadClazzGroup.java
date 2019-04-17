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

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Named
public class CH_LoadClazzGroup extends SpringContainerSupport implements CheckHomeworkTask {

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private RaikouSDK raikouSDK;

    @Override
    public void execute(CheckHomeworkContext context) {
        Long groupId = context.getHomework().getClazzGroupId();

        Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupsByTeacherId(context.getTeacherId(), false)
                .stream().collect(Collectors.toMap(GroupMapper::getId, Function.identity()));
        GroupMapper group = groups.get(groupId);
        if (group == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST,
                    "op", "teacher check homework"
            ));
            logger.error("Teacher {} has no permission to check homework of clazz {}", context.getTeacherId(), context.getHomework().getClazzGroupId());
            context.errorResponse("没有班组{}操作权限", context.getHomework().getClazzGroupId());
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
            return;
        }

        Long clazzId = group.getClazzId();
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST,
                    "op", "teacher check homework"
            ));
            logger.error("clazz {} does not exist when checking homework {}.", clazzId, context.getHomework().getId());
            context.errorResponse("班级{}不存在", clazzId);
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            return;
        }

        context.setClazzId(clazzId);
        context.setClazz(clazz);
        context.setGroupId(groupId);
        context.setGroup(group);
    }
}
