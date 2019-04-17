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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback.*;
import com.voxlearning.utopia.service.user.api.constants.TeacherLevelValueType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLevelServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AH_Callback extends AbstractAssignHomeworkProcessor {
    @Inject private PostAssignNewHomeworkRewardIntegral postAssignNewHomeworkRewardIntegral;
    @Inject private PostAssignNewHomeworkUpdateClazzBook postAssignHomeworkUpdateClazzBook;
    @Inject private PostAssignNewHomeworkSendMobileNotification postAssignHomeworkSendMobileNotification;
    @Inject private PostAssignNewHomeworkSendVendorMessage postAssignHomeworkSendVendorMessage;
    @Inject private PostAssignNewHomeworkParentMessage postAssignHomeworkParentMessage;
    @Inject private PostAssignNewHomeworkTermBegin postAssignHomeworkTermBegin;
    @Inject private PostAssignNewHomeworkPrize postAssignNewHomeworkPrize;
    @Inject private PostAssignNewHomeworkClazzHeadline postAssignNewHomeworkClazzHeadline;
    @Inject private PostAssignNewHomeworkUpdateHomeworkTask postAssignNewHomeworkUpdateHomeworkTask;
    @Inject private PostAssignNewHomeworkPublishMessage postAssignNewHomeworkPublishMessage;
    @Inject private PostAssignNewHomeworkUpdatePictureBookPlusHistory postAssignNewHomeworkUpdatePictureBookPlusHistory;
    @Inject private PostAssignNewHomeworkGroupHomeworkRecord postAssignNewHomeworkGroupHomeworkRecord;
    @Inject private PostAssignMothersDayHomeworkRewardIntegral postAssignMothersDayHomeworkRewardIntegral;
    @Inject private PostAssignKidsDayHomeworkRewardIntegral postAssignKidsDayHomeworkRewardIntegral;
    @Inject private PostAssignNewHomeworkUpdateOralCommunicationRecommend postAssignNewHomeworkUpdateOralCommunicationRecommend;

    @Inject private TeacherLevelServiceClient teacherLevelServiceClient;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;

    private final Collection<PostAssignHomework> assignHomeworkCallbacks = new LinkedHashSet<>();

    @Override
    protected void doProcess(AssignHomeworkContext context) {
        if (context.isSuccessful()) {
            Teacher teacher = context.getTeacher();

            // 布置作业或者测验 给老师加活跃值
            // 包班制支持，如果是副账号，换成主账号
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());

            // 记录老师本周布置作业天数
            newHomeworkCacheService.getAssignHomeworkAndQuizDayCountManager().addAssignDays(Collections.singletonList(teacher.getId()));

            assignHomeworkCallbacks.addAll(Arrays.asList(

                    postAssignNewHomeworkPublishMessage,
                    postAssignNewHomeworkPrize,
                    postAssignHomeworkUpdateClazzBook,//修改班级课本布置作业的课本、单元、课时等信息，主要用于下次布置作业单元、课时的初始化
                    postAssignHomeworkSendMobileNotification,//学生app JPUSH消息
                    postAssignHomeworkSendVendorMessage,//学生app 站内信消息
                    postAssignHomeworkParentMessage,//家长app 布置作业发送站内信和JPUSH
                    postAssignHomeworkTermBegin,//2016开学大礼包
                    postAssignNewHomeworkClazzHeadline,  //发送学生APP作业头条
                    postAssignNewHomeworkUpdateHomeworkTask,  //更新老师作业任务状态
                    postAssignNewHomeworkUpdatePictureBookPlusHistory, //新阅读绘本布置历史
                    postAssignNewHomeworkGroupHomeworkRecord, // 布置普通作业的时候，key groupId组成 value hid ，记录组的最新作业
                    postAssignNewHomeworkRewardIntegral, //布置作业活动根据时间范围、作业类型发送园丁豆奖励
                    postAssignMothersDayHomeworkRewardIntegral, //母亲节布置作业活动送园丁豆奖励
                    postAssignKidsDayHomeworkRewardIntegral, // 儿童节布置作业活动园丁奖励
                    postAssignNewHomeworkUpdateOralCommunicationRecommend //口语交际布置历史
            ));
            for (PostAssignHomework callback : assignHomeworkCallbacks)
                callback.afterHomeworkAssigned(context.getTeacher(), context);
        }
    }

}
