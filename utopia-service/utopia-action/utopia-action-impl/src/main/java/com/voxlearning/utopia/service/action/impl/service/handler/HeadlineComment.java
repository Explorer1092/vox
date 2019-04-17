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

package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 8/8/2016
 * 用户点赞行为处理
 */
@Named("actionEventHandler.headlineComment")
public class HeadlineComment extends AbstractActionEventHandler {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public ActionEventType getEventType() {
        return ActionEventType.HeadlineComment;
    }

    @Override
    public void handle(ActionEvent event) {
        Long relevantUserId = SafeConverter.toLong(event.getAttributes().get("relevantUserId"), 0);
        if (relevantUserId == 0) {
            return;
        }

        User user = userLoaderClient.loadUser(event.getUserId());
        if (user == null) {
            return;
        }

        if (StringUtils.isBlank(SafeConverter.toString(event.getAttributes().get("comment")))) {
            return;
        }

        if (StringUtils.isBlank(SafeConverter.toString(event.getAttributes().get("message")))) {
            return;
        }

        // 发送提醒
        AppMessage userMessage = new AppMessage();
        userMessage.setMessageType(StudentAppPushType.HEADLINE_COMMENT_REMIND.getType());
        userMessage.setUserId(relevantUserId);
        userMessage.setTitle("评论提醒");
        userMessage.setViewed(false);
        userMessage.setContent(SafeConverter.toString(event.getAttributes().get("comment")));

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("userId", event.getUserId());
        extInfo.put("userName", user.fetchRealname());
        extInfo.put("userImg", user.fetchImageUrl());
        extInfo.put("message", event.getAttributes().get("message"));
        extInfo.put("icon", event.getAttributes().get("icon"));
        userMessage.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(userMessage);
    }

}
