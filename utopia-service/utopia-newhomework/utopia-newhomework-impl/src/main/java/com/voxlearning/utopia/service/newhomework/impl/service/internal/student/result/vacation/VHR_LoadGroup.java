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
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 获取组信息
 *
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_LoadGroup extends SpringContainerSupport implements VacationHomeworkResultTask {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        Group group = raikouSystem.loadStudentGroups(context.getUserId())
                .stream()
                .filter(groupMapper -> GroupType.TEACHER_GROUP.equals(groupMapper.getGroupType()) && groupMapper.getSubject() == context.getSubject())
                .findFirst()
                .orElse(null);
        if (group == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST,
                    "mod3", context.getSubject(),
                    "mod4", context.getClazzGroupId(),
                    "op", "student vacation homework result"
            ));
//            logger.error("Group {} not found", context.getClazzGroupId());
        }
        if (group != null && !Objects.equals(group.getId(), context.getClazzGroupId())) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION,
                    "mod3", context.getSubject(),
                    "mod4", context.getClazzGroupId(),
                    "mod5", group.getId(),
                    "op", "student vacation homework result"
            ));
//            logger.error("Group {} doesn't belong to student {}", context.getClazzGroupId(), context.getUserId());
        }
        GroupMapper vacationHomeworkClazzGroup = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                .loadGroupDetail(context.getClazzGroupId(), false)
                .firstOrNull();
        if (vacationHomeworkClazzGroup != null) {
            context.setClazzGroup(vacationHomeworkClazzGroup);
        } else {
            context.setClazzGroup(GroupMapper.of(group));
        }
    }
}
