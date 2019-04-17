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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/11
 */
@Named
public class PostAssignNewHomeworkParentMessage extends NewHomeworkSpringBean implements PostAssignHomework {
    private static final String IM_CONTENT_PATTERN = "家长好，在线作业已推荐，请学生于{0}前完成。";
    private static final String IM_CONTENT_PATTERN_TERMREVIEW = "家长好，期末复习作业已推荐，请学生于{0}前完成。";

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        Map<Long, NewHomework> assignedHomeworks = context.getAssignedGroupHomework();
        if (MapUtils.isEmpty(assignedHomeworks)) {
            return;
        }
        String teacherName = teacher.fetchRealname();
        Long teacherId = teacher.getId();
        //这里只是取发送人的ID
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
        //这里才是取所有的学科
        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
        for (NewHomework newHomework : context.getAssignedGroupHomework().values()) {
            String endDate = DateUtils.dateToString(context.getHomeworkEndTime(), "M月d日HH:mm");
            String iMContent;
            if (newHomework.getType() == NewHomeworkType.TermReview) {
                iMContent = MessageFormat.format(IM_CONTENT_PATTERN_TERMREVIEW, endDate);
            } else {
                iMContent = MessageFormat.format(IM_CONTENT_PATTERN, endDate);
            }

            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
            String em_push_title = teacherName + subjectsStr + "：" + iMContent;

            //新的极光push
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("s", ParentAppPushType.HOMEWORK_ASSIGN.name());
            if (NewHomeworkType.OCR == newHomework.getType()) {
                jpushExtInfo.put("url", UrlUtils.buildUrlQuery("/view/mobile/parent/ocrhomework/details",
                        MapUtils.m("homeworkId", newHomework.getId(), "subject", newHomework.getSubject(), "objectiveConfigType", Subject.ENGLISH == newHomework.getSubject() ? ObjectiveConfigType.OCR_DICTATION : ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)));
            } else {
                jpushExtInfo.put("url", "/view/mobile/parent/homework/report_notice?hid=" + newHomework.getId());
            }
            appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                    com.voxlearning.utopia.service.push.api.constant.AppMessageSource.PARENT,
                    Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(newHomework.getClazzGroupId()))),
                    null,
                    jpushExtInfo);
    }
}
}
