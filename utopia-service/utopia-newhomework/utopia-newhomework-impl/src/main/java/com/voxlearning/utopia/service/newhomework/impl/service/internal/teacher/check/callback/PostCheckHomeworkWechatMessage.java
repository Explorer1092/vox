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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.FinishHomeworkMapper;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostCheckHomeworkWechatMessage extends SpringContainerSupport implements PostCheckHomework {

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(context.getTeacherId());

        if (!grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "Wechat", "BlackList")) {
            Map<String, Object> map = new HashMap<>();
            map.put("homeworkId", context.getHomeworkId());
            map.put("clazzId", context.getClazzId());
            map.put("clazzName", context.getClazz().formalizeClazzName());
            map.put("clazzLevel", context.getClazz().getClazzLevel().getLevel());
            map.put("startDate", context.getHomework().getStartTime());
            map.put("endDate", context.getHomework().getEndTime());
            map.put("finishStudents", finished(context.getAccomplishment()));

            WechatNoticeProcessorType wechatNoticeProcessorType = WechatNoticeProcessorType.HomeworkCheckNotice;

            switch (context.getTeacher().getSubject()) {
                case MATH:
                    wechatNoticeProcessorType = WechatNoticeProcessorType.MathHomeworkCheckNotice;
                    break;
                case CHINESE:
                    wechatNoticeProcessorType = WechatNoticeProcessorType.ChineseHomeworkCheckNotice;
                    break;
                default:
                    break;
            }
            wechatServiceClient.processWechatNotice(wechatNoticeProcessorType,
                    context.getClazzId(), context.getGroupId(), context.getTeacher(), map, WechatType.PARENT);
        }
    }

    private List<FinishHomeworkMapper> finished(NewAccomplishment accomplishment) {
        List<FinishHomeworkMapper> result = new ArrayList<>();
        if (accomplishment == null || accomplishment.size() <= 0) return result;
        for (String studentId : accomplishment.getDetails().keySet()) {
            FinishHomeworkMapper mapper = new FinishHomeworkMapper();
            mapper.setUserId(SafeConverter.toLong(studentId));
            result.add(mapper);
        }
        return result;
    }
}