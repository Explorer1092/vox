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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignlivecast;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Named
public class AHL_SaveHomework extends SpringContainerSupport implements AssignLiveCastHomeworkTask {
    @Inject private NewHomeworkServiceImpl newHomeworkService;

    @Override
    public void execute(AssignHomeworkContext context) {
        Date currentDate = new Date();
        Long teacherId = context.getTeacher().getId();
        String actionId = StringUtils.join(Arrays.asList(teacherId, currentDate.getTime()), "_");
        Subject subject = context.getTeacher().getSubject();

        for (Long groupId : context.getGroupIds()) {
            List<NewHomeworkPracticeContent> practiceContents = context.getGroupPractices().get(groupId);
            if (CollectionUtils.isNotEmpty(practiceContents)) {
                NewHomework newHomework = new NewHomework();
                newHomework.setActionId(actionId);
                newHomework.setTeacherId(teacherId);
                newHomework.setSubject(subject);
                newHomework.setClazzGroupId(groupId);
                newHomework.setStartTime(context.getHomeworkStartTime());
                newHomework.setEndTime(context.getHomeworkEndTime());
                newHomework.setRemark(context.getRemark());
                newHomework.setDuration(context.getDuration());
                newHomework.setCreateAt(currentDate);
                newHomework.setUpdateAt(currentDate);
                newHomework.setSource(context.getHomeworkSourceType());
                newHomework.setDisabled(false);
                newHomework.setChecked(false);
                newHomework.setPractices(practiceContents);
                newHomework.setIncludeSubjective(context.isIncludeSubjective());
                newHomework.setAdditions(context.getAdditions());
                newHomework.setType(context.getNewHomeworkType());
                newHomework.setHomeworkTag(context.getHomeworkTag());
                context.getAssignedGroupHomework().put(groupId, newHomework);
            }
        }
        if (!context.getAssignedGroupHomework().isEmpty()) {
            newHomeworkService.inserts(context.getAssignedGroupHomework().values());
            List<NewHomeworkBook> newHomeworkBookList = new ArrayList<>();
            for (Long groupId : context.getGroupIds()) {
                NewHomeworkBook newHomeworkBook = new NewHomeworkBook();
                newHomeworkBook.setId(context.getAssignedGroupHomework().get(groupId).getId());
                newHomeworkBook.setSubject(subject);
                newHomeworkBook.setActionId(actionId);
                newHomeworkBook.setTeacherId(teacherId);
                newHomeworkBook.setClazzGroupId(groupId);
                newHomeworkBook.setPractices(context.getGroupPracticesBooksMap().get(groupId));
                newHomeworkBookList.add(newHomeworkBook);
            }
            if (!newHomeworkBookList.isEmpty()) {
                newHomeworkService.insertNewHomeworkBooks(newHomeworkBookList);
            }
        } else {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK,
                    "op", "assign homework"
            ));
            context.errorResponse("LiveCast homework content is null homeworkSource:{}", JsonUtils.toJson(context.getSource()));
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
        }
    }
}
