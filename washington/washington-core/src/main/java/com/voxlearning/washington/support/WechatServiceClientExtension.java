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

package com.voxlearning.washington.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.NewUserTask;
import com.voxlearning.utopia.entity.activity.ActivityMothersDayCard;
import com.voxlearning.utopia.service.campaign.client.MothersDayServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType.ParentMothersDayCardNotice;

/**
 * Wechat service extension.
 *
 * @author Xiaohai Zhang
 * @since Jan 20, 2015
 */
@Named
public class WechatServiceClientExtension extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private MothersDayServiceClient mothersDayServiceClient;

    @Deprecated
    public Long bindUserAndWechat(Long userId, String openId, String source, Integer type) {
        MapMessage message;
        try {
            message = wechatServiceClient.bindUserAndWechat(userId, openId, source, type);
        } catch (Exception ex) {
            logger.error("Failed to bind user and wechat", ex);
            message = MapMessage.errorMessage();
        }
        if (!message.isSuccess()) {
            return null;
        }
        if (Boolean.TRUE.equals(message.get("task")) && type == WechatType.PARENT.getType()) {
            // 清除学生新手任务
            // 注意：有其他场景也需要清除学生新手任务：
            // 1.家长自己注册账号并且绑定微信，然后再去关联跟孩子的关系，需要在关联关系的时候清除学生新手任务
            // 2.家长扫描孩子二维码，但孩子并没有关联家长，系统会自动创建一个家长号，并绑定微信，最后才关联孩子。
            // 2这种情况会导致绑定微信号的时候，家长还并没有跟学生关联，所以下边这堆代码不起作用。需要在其他地方清除
            List<User> children = studentLoaderClient.loadParentStudents(userId);
            for (User child : children) {
                asyncUserCacheServiceClient.getAsyncUserCacheService()
                        .NewUserTaskCacheManager_completeStudentNewUserTask(child, NewUserTask.parentWechatBinded)
                        .awaitUninterruptibly();
            }
        }
        return (Long) message.get("id");
    }

    public boolean unbindUserAndWechat(String openId) {
        MapMessage message;
        try {
            message = wechatServiceClient.unbindUserAndWechat(openId);
        } catch (Exception ex) {
            logger.error("Failed to unbind user and wechat", ex);
            message = MapMessage.errorMessage();
        }
        if (message.isSuccess()) {
            if (Boolean.TRUE.equals(message.get("cleanupNewUserTaskCache"))) {
                Long userId = (Long) message.get("userId");
                List<User> children = studentLoaderClient.loadParentStudents(userId);
                asyncUserCacheServiceClient.getAsyncUserCacheService()
                        .NewUserTaskCacheManager_cleanupStudentNewUserTaskCache(children.stream().map(User::getId).collect(Collectors.toList()))
                        .awaitUninterruptibly();
            }
        }
        return message.isSuccess();
    }

    // FIXME 活动时间过了，是否移除？
    public void sendMothersDayCard(Long studentId, Long parentId, String openId) {
        try {
            User student = raikouSystem.loadUser(studentId);
            if (new Date().before(DateUtils.stringToDate("2015-05-15 23:59:59"))) {
                MapMessage mesg = mothersDayServiceClient.getMothersDayService()
                        .getMothersDayCard(student, false)
                        .getUninterruptibly();
                if (mesg.isSuccess() && mesg.containsKey("card")) {
                    ActivityMothersDayCard card = (ActivityMothersDayCard) mesg.get("card");
                    if (card != null && !Boolean.TRUE.equals(card.getSended())) {
                        Map<String, Object> extensionInfo = MiscUtils.m("studentId", studentId, "studentName", student == null ? "" : student.fetchRealname());
                        wechatServiceClient.processWechatNotice(ParentMothersDayCardNotice, parentId, openId, extensionInfo);
                        mothersDayServiceClient.getMothersDayService()
                                .updateMothersDayCardSended(studentId)
                                .awaitUninterruptibly();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("reward wechat user error,userId:{}", studentId, ex);
        }
    }
}
