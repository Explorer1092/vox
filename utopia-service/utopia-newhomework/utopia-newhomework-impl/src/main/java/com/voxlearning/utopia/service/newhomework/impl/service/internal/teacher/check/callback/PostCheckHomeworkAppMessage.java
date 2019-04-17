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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType.HOMEWORK_CHECK_REMIND;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostCheckHomeworkAppMessage extends SpringContainerSupport implements PostCheckHomework {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        // 已检查作业的jpush
        String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/currentmonth/history/detail.vpage",
                MiscUtils.m("subject", context.getTeacher().getSubject(), "homeworkId", context.getHomeworkId()));
        //奖励学豆的jpush

        StringBuilder checkContent = new StringBuilder()
                .append(context.getTeacher().respectfulName())
                .append("已检查")
                .append(DateUtils.dateToString(context.getHomework().getCreateAt(), "MM月dd日"))
                .append("的")
                .append(context.getHomework().getSubject().getValue())
                .append("作业，你都做完了吗？快去看看");

//        List<String> tags = Collections.singletonList("group_" + context.getGroupId());
        List<Long> studentIds = studentLoaderClient.loadGroupStudentIds(context.getGroupId());

        Map<String, Object> extInfo = MiscUtils.m("link", link, "t", "h5", "key", "j");
        extInfo.put("s", HOMEWORK_CHECK_REMIND.getType());
        appMessageServiceClient.sendAppJpushMessageByIds(checkContent.toString(), AppMessageSource.STUDENT, studentIds, extInfo);
        NewAccomplishment accomplishment = context.getAccomplishment();
        Set<Long> fsids = new HashSet<>();
        if (accomplishment != null && accomplishment.size() > 0) {
            fsids.addAll(accomplishment.getDetails().keySet().stream().map(SafeConverter::toLong).collect(Collectors.toSet()));
        }
        NewHomework homework = context.getHomework();

        //读取作业结果
        Map<Long, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loadNewHomeworkResult(homework.toLocation(), fsids, false);

        List<AppMessage> messages = new ArrayList<>();
        for (StudentDetail student : context.getStudents()) {
            Long studentId = student.getId();
            AppMessage message = new AppMessage();
            message.setUserId(student.getId());
            message.setMessageType(StudentAppPushType.HOMEWORK_CHECK_REMIND.getType());
            message.setTitle("作业已检查");
            Boolean showScore = true;
            if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(student, "ShowScoreLevel", "WhiteList")) {
                showScore = false;
            }
            NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(studentId);
            message.setContent(checkHomeworkSummary(showScore, homework.getCreateAt(), homework.getSubject(), fsids.contains(studentId), newHomeworkResult));
            message.setLinkUrl(link);
            message.setLinkType(1); // 站内的相对地址
            messages.add(message);

//            Subject subject = context.getTeacher().getSubject();
//            //英语或者数学 && 作业已完成已检查 && 非深圳地区
//            if (subject.equals(Subject.ENGLISH) || subject.equals(Subject.MATH)) {
//                if (newHomeworkResult != null && newHomeworkResult.isFinished()) {
//                    String integralSummary = "额外学豆：登录家长通查看每次作业的详细诊断，有机会获得额外的学豆奖励哦\n领取方式：登录家长通领取";
//                    //新消息中心用户消息
//                    AppUserMessage messageIntegral = new AppUserMessage();
//                    messageIntegral.setUserId(studentId);
//                    messageIntegral.setMessageType(StudentAppPushType.HOMEWORK_CHECK_REMIND.getType());
//                    messageIntegral.setTitle("发放额外学豆奖励");
//                    messageIntegral.setContent(integralSummary);
//                    messageIntegral.setLinkUrl(integralLink);
//                    messageIntegral.setLinkType(1);//站内的相对地址
//                    messages.add(messageIntegral);
//                }
//            }
        }
        messages.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
    }

    private String checkHomeworkSummary(Boolean showScore, Date createDate, Subject subject, boolean finished, NewHomeworkResult newHomeworkResult) {
        StringBuilder result = new StringBuilder();
        result.append(subject.getValue()).append("作业").append("\n");
        Integer score = newHomeworkResult != null ? newHomeworkResult.processScore() : null;
        if (showScore) {
            if (score != null) {
                result.append("得分：").append(score).append("\n");
            } else {
                result.append("得分：").append("无").append("\n");
            }
        }
        result.append("布置时间:").append(DateUtils.getDateTimeStrC(createDate)).append("\n")
                .append(finished ? "完成情况:已完成" : "完成情况:未完成");
        return result.toString();
    }
}
