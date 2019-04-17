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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AH_CheckClazzGroup extends AbstractAssignHomeworkProcessor {

    @Override
    protected void doProcess(AssignHomeworkContext context) {
        Long teacherId = context.getTeacher().getId();

        String idListText = SafeConverter.toString(context.getSource().get("clazzIds"));
        if (StringUtils.isBlank(idListText)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST,
                    "mod3", JsonUtils.toJson(context.getSource()),
                    "op", "assign homework"
            ));
            context.errorResponse("班级信息错误,请退出重新登录。");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }

        String[] idTextArr = StringUtils.split(idListText, ",");
        List<Long> groupIds = new ArrayList<>();
        for (String idText : idTextArr) {
            String[] text = StringUtils.split(idText, "_");
            if (text.length == 2) {
//                Long clazzId = ConversionUtils.toLong(text[0]);
                Long groupId = ConversionUtils.toLong(text[1]);
                if (groupId != 0) {
                    groupIds.add(groupId);
                }
            }
        }


        //验证每个班组是否属于老师
        Map<Long, GroupTeacherMapper> groupTeacherMapper = groupLoaderClient.loadTeacherGroups(teacherId, false).stream().collect(Collectors.toMap(GroupTeacherMapper::getId, (gt) -> gt));
        for (Long groupId : groupIds) {
            if (groupTeacherMapper.get(groupId) == null) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION,
                        "mod3", JsonUtils.toJson(context.getSource()),
                        "op", "assign homework"
                ));
                context.errorResponse("没有班组{}的操作权限,请退出重新登录。", groupId);
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION);
                context.setTerminateTask(true);
                return;
            }
        }

        context.getGroupIds().addAll(groupIds);
    }
}
