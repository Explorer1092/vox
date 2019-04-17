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
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 8/8/2016
 * 用户点赞行为处理
 */
@Named("actionEventHandler.like")
public class Like extends AbstractActionEventHandler {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;

    @Override
    public ActionEventType getEventType() {
        return ActionEventType.Like;
    }

    @Override
    public void handle(ActionEvent event) {
        UserLikeType type = UserLikeType.of(SafeConverter.toString(event.getAttributes().get("type"), ""));
        if (null == type) {
            return;
        }

        Long likedId = SafeConverter.toLong(event.getAttributes().get("likedId"), 0);
        if (0 == likedId) {
            return;
        }

        Long clazzId = SafeConverter.toLong(event.getAttributes().get("clazzId"), 0);
        if (0 == clazzId) {
            return;
        }

        long c = actionEventDayRangeCounter_Like.increase(event);
        if (c > 1) {
            return; //每个同学同一天只能给一个同学点赞一次
        }

        // 新的点赞策略
        String recordId = SafeConverter.toString(event.getAttributes().get("recordId"));
        if (StringUtils.isBlank(recordId)) {
            return;
        }

        String likerName = SafeConverter.toString(event.getAttributes().get("likerName"));

        userLikeServiceClient.like(recordId, type, event.getUserId(), likerName, likedId, event.getAttributes());

        // 发送点赞提醒
        sendLikeMessage(type, event.getUserId(), likedId, event.getAttributes());
    }

    private void sendLikeMessage(UserLikeType type, Long userId, Long likedId, Map<String, Object> attributes) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) return;

        //发送点赞提醒
        AppMessage messages = new AppMessage();
        switch (type) {
            case ACHIEVEMENT_SHARE_HEADLINE:
                messages.setMessageType(StudentAppPushType.ACHIEVEMENT_ENCOURAGE_HEAD_LINE_REMIND.getType());
                messages.setUserId(likedId);
                messages.setTitle("鼓励提醒");
                messages.setViewed(false);
                messages.setContent(user.fetchRealname() + "在\"" + type.getSource() + "\"中鼓励了你一下，继续加油哦");
                break;
            case BIRTHDAY_BLESS_HEADLINE:
                messages.setMessageType(StudentAppPushType.BIRTHDAY_BLESS_HEAD_LINE_REMIND.getType());
                messages.setUserId(likedId);
                messages.setTitle("祝福提醒");
                messages.setViewed(false);
                messages.setContent(user.fetchRealname() + "为你送上生日祝福，祝你生日快乐！");
                break;
            case CLAZZ_JOURNAL:
            case COMPETITION_LEVEL_UP:
            case COMPETITION_SEASON_CLASS_TOP_3:
            case MEDAL_LEVEL_UP:
            case CLASS_BOSS_TOP_3:
            case PET_LEVEL_UP:
            case NEW_PET:
            case NEW_MEDAL:
                messages.setMessageType(StudentAppPushType.LIKE_REMIND.getType());
                messages.setUserId(likedId);
                messages.setTitle("点赞提醒");
                messages.setViewed(false);
                break;
            default:
                messages.setMessageType(StudentAppPushType.LIKE_REMIND.getType());
                messages.setUserId(likedId);
                messages.setTitle("点赞提醒");
                messages.setViewed(false);
                messages.setContent(user.fetchRealname() + "给你在\"" + type.getSource() + "\"点了个赞");
        }

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("likerId", userId);
        extInfo.put("likerName", user.fetchRealname());
        extInfo.put("likerImg", user.fetchImageUrl());
        extInfo.put("likeType", type.name());

        // 如果是成就榜点赞,需要记录成就类型和等级
        if (type == UserLikeType.ACHIEVEMENT_RANK) {
            if (!attributes.containsKey("achievementType") || !attributes.containsKey("achievementLevel")) {
                return; //没有成就类型与成就等级参数就不发消息了
            }
            extInfo.put("achievementType", attributes.get("achievementType"));
            extInfo.put("achievementLevel", attributes.get("achievementLevel"));
        }

        // 加入小图标
        if (attributes.containsKey("icon")) {
            extInfo.put("icon", attributes.get("icon"));
        }

        messages.setExtInfo(extInfo);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(messages);
    }

}
