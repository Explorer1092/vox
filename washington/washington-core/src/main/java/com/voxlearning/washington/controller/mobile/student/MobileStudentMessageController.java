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

package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.AppMessageServiceClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.mapper.StudentRemindMessageMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType.*;

/**
 * @author xinxin
 * @since 4/8/2016
 */
@Controller
@RequestMapping(value = "/studentMobile/message")
public class MobileStudentMessageController extends AbstractMobileController {

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    private static final List<Integer> remindMessageTypes = Arrays.asList(
            LIKE_REMIND.getType(),
            ACHIEVEMENT_HEAD_LINE_REMIND.getType(),
            ACHIEVEMENT_WALL_HEAD_LINE_REMIND.getType(),
            BIRTHDAY_BLESS_HEAD_LINE_REMIND.getType(),
            ACHIEVEMENT_ENCOURAGE_HEAD_LINE_REMIND.getType(),
            HEADLINE_COMMENT_REMIND.getType()
    );

    /**
     * 查询未读消息数量
     * 注:此接口只查询点赞通知、班级头条通知的数量
     */
    @RequestMapping(value = "/unread/count.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unreadCount() {
        try {
            if (studentUnLogin()) {
                return MapMessage.errorMessage("请重新登录");
            }

            List<AppMessage.Location> messageLocations = getRemindMessageLocation();
            long count = 0L;
            if (CollectionUtils.isNotEmpty(messageLocations)) {
                count = messageLocations.stream().filter(m -> null == m.getViewed() || !m.getViewed()).count();
            }

            return MapMessage.successMessage().add("unread_count", count);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询班级提醒消息列表
     */
    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            List<AppMessage> messages = getRemindMessage();
            if (CollectionUtils.isEmpty(messages)) {
                return MapMessage.successMessage();
            }

            long count = messages.stream().filter(m -> null == m.getViewed() || !m.getViewed()).count();

            List<StudentRemindMessageMapper> mappers = new ArrayList<>();
            for (AppMessage msg : messages) {
                if (msg.getMessageType() == StudentAppPushType.ACHIEVEMENT_HEAD_LINE_REMIND.getType()
                        || msg.getMessageType() == StudentAppPushType.ACHIEVEMENT_WALL_HEAD_LINE_REMIND.getType()) {
                    StudentRemindMessageMapper mapper = generateAchievementRemind(msg);
                    if (null == mapper) continue;

                    mappers.add(mapper);
                } else if (msg.getMessageType() == LIKE_REMIND.getType()) {
                    StudentRemindMessageMapper mapper = generateLikeRemind(msg);
                    if (null == mapper) continue;

                    mappers.add(mapper);
                    //个人成就分享鼓励消息|生日祝福消息
                } else if (msg.getMessageType() == StudentAppPushType.ACHIEVEMENT_ENCOURAGE_HEAD_LINE_REMIND.getType()
                        || msg.getMessageType() == StudentAppPushType.BIRTHDAY_BLESS_HEAD_LINE_REMIND.getType()) {
                    StudentRemindMessageMapper mapper = generateContentRemind(msg);
                    if (null == mapper) continue;
                    mappers.add(mapper);
                } else if (msg.getMessageType() == HEADLINE_COMMENT_REMIND.getType()) {
                    StudentRemindMessageMapper mapper = generateCommentRemind(msg);
                    if (null == mapper) continue;

                    mappers.add(mapper);
                }

                if (!msg.getViewed()) {
                    appMessageServiceClient.getAppMessageService().updateMessageViewed(msg.getId(), currentUserId(), "APP_USER_MSG");
                }
            }

            mappers = mappers.stream().sorted((m1, m2) -> m2.getDateTime().compareTo(m1.getDateTime())).collect(Collectors.toList());

            return MapMessage.successMessage().add("unread_count", count).add("messages", mappers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private StudentRemindMessageMapper generateAchievementRemind(AppMessage msg) {
        if (!msg.getExtInfo().containsKey("userName") || !msg.getExtInfo().containsKey("userImg")
                || !msg.getExtInfo().containsKey("achievementType") || !msg.getExtInfo().containsKey("level")) {
            return null;
        }

        StudentAppPushType messageType = StudentAppPushType.of(msg.getMessageType());
        if (null == messageType) return null;

        AchievementType achievementType = AchievementType.of(msg.getExtInfo().get("achievementType").toString());
        if (null == achievementType) {
            if (!(msg.getExtInfo().get("achievementType") instanceof Map)) {
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) msg.getExtInfo().get("achievementType");
            if (!map.containsKey("name")) {
                return null;
            }

            achievementType = AchievementType.of(map.get("name").toString());
        }

        StudentRemindMessageMapper mapper = new StudentRemindMessageMapper();
        mapper.setId(msg.getId());
        mapper.setDateTime(msg.getCreateTime());
        mapper.setType(messageType.name());
        mapper.setAchievementType(achievementType == null ? "" : achievementType.name());
        mapper.setAchievementTitle(achievementType == null ? "" : achievementType.getTitle());
        mapper.setAchievementLevel(SafeConverter.toInt(msg.getExtInfo().get("level")));
        mapper.setUserImg(msg.getExtInfo().get("userImg").toString());
        mapper.setUserName(msg.getExtInfo().get("userName").toString());
        mapper.setIcon(SafeConverter.toString(msg.getExtInfo().get("icon")));
        return mapper;
    }

    private StudentRemindMessageMapper generateContentRemind(AppMessage msg) {
        StudentAppPushType messageType = StudentAppPushType.of(msg.getMessageType());
        if (null == messageType) return null;


        StudentRemindMessageMapper mapper = new StudentRemindMessageMapper();
        mapper.setId(msg.getId());
        mapper.setDateTime(msg.getCreateTime());
        mapper.setType(messageType.name());
        mapper.setUserImg(msg.getExtInfo().get("likerImg").toString());
        mapper.setUserName(msg.getExtInfo().get("likerName").toString());
        mapper.setContent(msg.getContent());
        mapper.setIcon(SafeConverter.toString(msg.getExtInfo().get("icon")));
        return mapper;
    }

    private StudentRemindMessageMapper generateLikeRemind(AppMessage msg) {
        StudentAppPushType messageType = StudentAppPushType.of(msg.getMessageType());
        if (null == messageType) return null;

        if (!msg.getExtInfo().containsKey("likeType") || !msg.getExtInfo().containsKey("likerId")
                || !msg.getExtInfo().containsKey("likerImg") || !msg.getExtInfo().containsKey("likerName")) {
            return null;
        }

        UserLikeType likeType = UserLikeType.of(msg.getExtInfo().get("likeType").toString());
        if (null == likeType) return null;

        StudentRemindMessageMapper mapper = new StudentRemindMessageMapper();
        mapper.setId(msg.getId());
        mapper.setDateTime(msg.getCreateTime());
        mapper.setType(likeType.name());
        mapper.setUserImg(msg.getExtInfo().get("likerImg").toString());
        mapper.setUserName(msg.getExtInfo().get("likerName").toString());
        mapper.setHeadWear(getHeadWear(SafeConverter.toLong(msg.getExtInfo().get("likerId"))));

        //成就榜点赞消息要附带成就信息
        if (UserLikeType.ACHIEVEMENT_RANK == likeType) {
            if (!msg.getExtInfo().containsKey("achievementType") || !msg.getExtInfo().containsKey("achievementLevel")) {
                return null;
            }

            AchievementType achievementType = AchievementType.of(msg.getExtInfo().get("achievementType").toString());
            if (null == achievementType) {
                return null;
            }
            Integer achievementLevel = SafeConverter.toInt(msg.getExtInfo().get("achievementLevel"), 0);
            if (0 == achievementLevel) {
                return null;
            }

            mapper.setAchievementType(achievementType.name());
            mapper.setAchievementTitle(achievementType.getTitle());
            mapper.setAchievementLevel(achievementLevel);
        }

        mapper.setIcon(SafeConverter.toString(msg.getExtInfo().get("icon")));

        return mapper;
    }

    private StudentRemindMessageMapper generateCommentRemind(AppMessage msg) {
        StudentAppPushType messageType = StudentAppPushType.of(msg.getMessageType());
        if (null == messageType) return null;

        if (!msg.getExtInfo().containsKey("userId") || !msg.getExtInfo().containsKey("userName")
                || !msg.getExtInfo().containsKey("userImg") || !msg.getExtInfo().containsKey("message")
                || !msg.getExtInfo().containsKey("icon")) {
            return null;
        }

        StudentRemindMessageMapper mapper = new StudentRemindMessageMapper();
        mapper.setId(msg.getId());
        mapper.setDateTime(msg.getCreateTime());
        mapper.setType(messageType.name());
        mapper.setUserImg(SafeConverter.toString(msg.getExtInfo().get("userImg")));
        mapper.setUserName(SafeConverter.toString(msg.getExtInfo().get("userName")));
        mapper.setHeadWear(getHeadWear(SafeConverter.toLong(msg.getExtInfo().get("userId"))));

        mapper.setContent(msg.getContent());
        mapper.setAchievementTitle(SafeConverter.toString(msg.getExtInfo().get("message")));
        mapper.setIcon(SafeConverter.toString(msg.getExtInfo().get("icon")));

        return mapper;
    }

    /**
     * 获取用户消息 （只查询点赞/评论通知、班级头条通知的数量 新增生日祝福，个人成就分享鼓励）
     */
    private List<AppMessage.Location> getRemindMessageLocation() {
        return messageLoaderClient.getMessageLoader().loadAppMessageLocations(currentUserId())
                .stream()
                .filter(m -> remindMessageTypes.contains(SafeConverter.toInt(m.getMessageType())))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户消息 （只查询点赞/评论通知、班级头条通知的数量 新增生日祝福，个人成就分享鼓励）
     */
    private List<AppMessage> getRemindMessage() {
        List<AppMessage.Location> locations = getRemindMessageLocation();
        if (CollectionUtils.isEmpty(locations)) {
            return new ArrayList<>();
        }
        Set<String> ids = locations.stream()
                .map(AppMessage.Location::getId)
                .collect(Collectors.toSet());
        return new ArrayList<>(messageLoaderClient.getMessageLoader().loadAppMessageByIds(ids).values());
    }

    /**
     * 获取头饰
     */
    private String getHeadWear(Long userId) {
        if (userId == null || userId <= 0L) return null;

        StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(userId);
        if (studentInfo == null) return null;

        Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
        return (headWearPrivilege != null) ? headWearPrivilege.getImg() : null;
    }
}
