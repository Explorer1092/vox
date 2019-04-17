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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 学生完成作业的时候缓存下来一些信息用于家长app弹窗显示。有效期7天
 *
 * @author shiwei.liao
 * @since 2017-3-6
 */
@Named
public class PostFinishHomeworkAddPopup extends SpringContainerSupport implements PostFinishHomework {
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        if (context.getUserId() == null || context.getUserId() == 0 || StringUtils.isBlank(context.getHomeworkId())) {
            return;
        }
        //已检查的作业不存这个popup
        if (context.getHomework() == null || (context.getHomework().checked != null && context.getHomework().checked)) {
            return;
        }
        Long studentId = context.getUserId();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        String homeworkId = context.getHomeworkId();
        String userName = context.getUser().fetchRealname();
        String createDate = context.getHomework() == null ? "" : DateUtils.dateToString(context.getHomework().getCreateAt(), "MM月dd日");
        String subject = context.getHomework() == null ? "" : context.getHomework().getSubject().getValue();
        String score = generateHomeworkScoreLevel(context.getResult().processScore(), studentDetail);
        String content = generateContent(userName, createDate, subject, score);
        newHomeworkCacheService.getStudentFinishHomeworkPopupManager().addStudentFinishHomeworkPopup(studentId, context.getHomework().getSubject(), homeworkId, content);
    }

    private String generateContent(String userName, String createDate, String subject, String score) {
        String content = "";
        if (StringUtils.isBlank(userName)) {
            userName = "孩子";
        }
        content += userName + "完成了";
        if (StringUtils.isNotBlank(createDate)) {
            content += createDate + "的";
        }
        if (StringUtils.isNotBlank(subject)) {
            content += subject;
        }
        content += "作业";
        if (StringUtils.isNotBlank(score)) {
            content += "，成绩" + score;
        }
        return content;
    }

    private String generateHomeworkScoreLevel(Integer score, StudentDetail studentDetail) {
        if (score == null) {
            return "";
        }
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList")) {
            return ScoreLevel.processLevel(score).getLevel();
        }
        return String.valueOf(score);
    }
}
