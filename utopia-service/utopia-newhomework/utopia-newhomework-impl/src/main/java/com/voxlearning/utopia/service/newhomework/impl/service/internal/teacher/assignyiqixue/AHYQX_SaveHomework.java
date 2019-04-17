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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.AssignLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
public class AHYQX_SaveHomework extends SpringContainerSupport implements AssignYiQiXueHomeworkTask {

    @Inject private LiveCastHomeworkDao liveCastHomeworkDao;
    @Inject private LiveCastHomeworkBookDao liveCastHomeworkBookDao;


    @Override
    public void execute(AssignHomeworkContext context) {
        Date currentDate = new Date();
        Long teacherId = context.getTeacher().getId();
        String actionId = StringUtils.join(Arrays.asList(teacherId, currentDate.getTime()), "_");
        Subject subject = context.getTeacher().getSubject();
        List<LiveCastHomework> liveCastHomeworks = new ArrayList<>();
        for (Long groupId : context.getGroupIds()) {
            LiveCastHomework liveCastHomework = new LiveCastHomework();
            liveCastHomework.setActionId(actionId);
            liveCastHomework.setTeacherId(teacherId);
            liveCastHomework.setSubject(subject);
            liveCastHomework.setClazzGroupId(groupId);
            liveCastHomework.setStartTime(context.getHomeworkStartTime());
            liveCastHomework.setEndTime(context.getHomeworkEndTime());
            liveCastHomework.setRemark(context.getRemark());
            liveCastHomework.setDuration(context.getDuration());
            liveCastHomework.setCreateAt(currentDate);
            liveCastHomework.setUpdateAt(currentDate);
            liveCastHomework.setSource(context.getHomeworkSourceType());
            liveCastHomework.setDisabled(false);
            liveCastHomework.setChecked(false);
            liveCastHomework.setPractices(context.getGroupPractices().get(groupId));
            liveCastHomework.setIncludeSubjective(context.isIncludeSubjective());
            liveCastHomework.setAdditions(context.getAdditions());
            liveCastHomework.setType(context.getNewHomeworkType());
            liveCastHomework.setHomeworkTag(context.getHomeworkTag());
            liveCastHomeworks.add(liveCastHomework);
        }
        if (CollectionUtils.isNotEmpty(liveCastHomeworks)) {
            liveCastHomeworkDao.inserts(liveCastHomeworks);
            for(LiveCastHomework liveCastHomework : liveCastHomeworks){
                NewHomework newHomework = new NewHomework();
                PropertiesUtils.copyProperties(newHomework, liveCastHomework);
                context.getAssignedGroupHomework().put(liveCastHomework.getClazzGroupId(), newHomework);
            }
            List<LiveCastHomeworkBook> liveCastHomeworkBooks = new ArrayList<>();
            for (Long groupId : context.getGroupIds()) {
                LiveCastHomeworkBook liveCastHomeworkBook = new LiveCastHomeworkBook();
                liveCastHomeworkBook.setId(context.getAssignedGroupHomework().get(groupId).getId());
                liveCastHomeworkBook.setSubject(subject);
                liveCastHomeworkBook.setActionId(actionId);
                liveCastHomeworkBook.setTeacherId(teacherId);
                liveCastHomeworkBook.setClazzGroupId(groupId);
                liveCastHomeworkBook.setPractices(context.getGroupPracticesBooksMap().get(groupId));
                liveCastHomeworkBooks.add(liveCastHomeworkBook);
            }
            if (!liveCastHomeworkBooks.isEmpty()) {
                liveCastHomeworkBookDao.inserts(liveCastHomeworkBooks);
            }
        } else {
            context.errorResponse("17xue homework content is null homeworkSource:{}", JsonUtils.toJson(context.getSource()));
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
            context.setTerminateTask(true);
        }
    }
}
