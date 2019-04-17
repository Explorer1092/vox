/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发送app消息
 *
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/11
 */
@Named
public class PostAssignNewHomeworkSendVendorMessage extends NewHomeworkSpringBean implements PostAssignHomework {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        if (context.getAssignedGroupHomework().isEmpty())
            return;
        AlpsThreadPool.getInstance().submit(() -> {
            Map<Long, NewHomework> assigned = context.getAssignedGroupHomework();
            String title = "新作业消息";

            // 布置的作业发送通知
            List<Long> studentIds;
            for (NewHomework newHomework : assigned.values()) {
                String homeworkId = newHomework.getId();
                Long groupId = newHomework.getClazzGroupId();
                studentIds = studentLoaderClient.loadGroupStudentIds(groupId);
                String summary = buildHomeworkSummary(context, teacher,
                        context.getGroupPractices().get(groupId),
                        context.getGroupPracticesBooksMap().get(groupId));
                sendVendorAppMessages(studentIds, homeworkId, title, summary);
            }
        });
    }

    private String buildHomeworkSummary(AssignHomeworkContext context, Teacher teacher,
                                        List<NewHomeworkPracticeContent> practices,
                                        Map<ObjectiveConfigType, List<NewHomeworkBookInfo>> practicesBooksMap) {
        String note = StringUtils.defaultString(context.getRemark());
        String endDate = DateUtils.getDateTimeStrC(context.getHomeworkEndTime());
        int questionCount = 0;
        for (NewHomeworkPracticeContent npc : practices) {
            if (ObjectiveConfigType.BASIC_APP.equals(npc.getType())
                    || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(npc.getType())
                    || ObjectiveConfigType.NATURAL_SPELLING.equals(npc.getType())
                    || ObjectiveConfigType.READING.equals(npc.getType())
                    || ObjectiveConfigType.KEY_POINTS.equals(npc.getType())
                    || ObjectiveConfigType.LEVEL_READINGS.equals(npc.getType())
                    || ObjectiveConfigType.DUBBING.equals(npc.getType())
                    || ObjectiveConfigType.NEW_READ_RECITE.equals(npc.getType())
                    || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(npc.getType())) {
                questionCount += npc.getApps().size();
            } else {
                questionCount += npc.getQuestions().size();
            }
        }
        LinkedHashSet<String> unitNames = new LinkedHashSet<>();
        for (ObjectiveConfigType objectiveConfigType : practicesBooksMap.keySet()) {
            unitNames.addAll(practicesBooksMap.get(objectiveConfigType).stream().map(NewHomeworkBookInfo::getUnitName).collect(Collectors.toList()));
        }
        String units = StringUtils.join(unitNames, "\n");
        String homeworkName = "作业";
        if (context.getNewHomeworkType() == NewHomeworkType.TermReview) {
            homeworkName = "期末作业";
        }
        String summary = teacher.getSubject().getValue() + homeworkName + "\n"
                + "截止时间:" + endDate + "\n"
                + "作业内容:\n" + units + "\n共" + questionCount + "题" + "\n";
        if (StringUtils.isNotBlank(note)) {
            summary += context.getTeacher().fetchRealname() + "老师留言:" + note + "\n";
        }
        if (CollectionUtils.isNotEmpty(context.getGroupIdsWithHomeworkPrize())) {
            summary += "奖励:老师设置了随机学豆奖励";
        }
        return summary;
    }

    private void sendVendorAppMessages(List<Long> studentIds,
                                       String homeworkId, String title, String summary) {
        List<AppMessage> result = new ArrayList<>();
        String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/homework/category/newpracticenum.vpage",
                MiscUtils.m("homeworkId", homeworkId));

        for (Long studentId : studentIds) {
            AppMessage appUserMessage = new AppMessage();
            appUserMessage.setUserId(studentId);
            appUserMessage.setTitle(title);
            appUserMessage.setContent(summary);
            appUserMessage.setLinkUrl(link);
            appUserMessage.setMessageType(StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType());
            appUserMessage.setLinkType(1);
            result.add(appUserMessage);
        }
        result.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
    }
}
