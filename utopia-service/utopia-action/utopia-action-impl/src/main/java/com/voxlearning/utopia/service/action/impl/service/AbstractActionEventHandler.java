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

package com.voxlearning.utopia.service.action.impl.service;

import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.action.api.document.*;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.dao.*;
import com.voxlearning.utopia.service.action.impl.service.support.ActionEventDayRangeCounter;
import com.voxlearning.utopia.service.action.impl.service.support.ActionEventDayRangeCounter_Like;
import com.voxlearning.utopia.service.action.impl.service.support.ActionEventMonthRangeCounter_Like;
import com.voxlearning.utopia.service.action.impl.service.support.ActionEventWeekRangeCounter;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeManagerClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstraction {@link ActionEventHandler} implementation.
 *
 * @author Xiaohai Zhang
 * @since Aug 4, 2016
 */
abstract public class AbstractActionEventHandler implements ActionEventHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject private ClazzAchievementCountDao clazzAchievementCountDao;
    @Inject private ClazzAchievementLogDao clazzAchievementLogDao;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private UserAchievementLogDao userAchievementLogDao;
    @Inject private UserGrowthDao userGrowthDao;
    @Inject private UserGrowthLogDao userGrowthLogDao;
    @Inject private UserAchievementRecordDao userAchievementRecordDao;

    @Inject protected ActionEventDayRangeCounter actionEventDayRangeCounter;
    @Inject protected ActionEventDayRangeCounter_Like actionEventDayRangeCounter_Like;
    @Inject protected ActionEventMonthRangeCounter_Like actionEventMonthRangeCounter_like;
    @Inject protected ActionEventWeekRangeCounter actionEventWeekRangeCounter;
    @Inject protected ClazzAttendanceCountDao clazzAttendanceCountDao;
    @Inject protected UserAttendanceCountDao userAttendanceCountDao;
    @Inject protected UserAttendanceLogDao userAttendanceLogDao;
    @Inject protected UserGrowthRewardLogDao userGrowthRewardLogDao;
    @Inject protected UserLoaderClient userLoaderClient;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;
    @Inject protected PrivilegeLoaderClient privilegeLoaderClient;
    @Inject protected PrivilegeManagerClient privilegeManagerClient;
    @Inject protected PrivilegeServiceClient privilegeServiceClient;

    protected void ensureId(UserGrowthLog log) {
        Objects.requireNonNull(log);
        Objects.requireNonNull(log.getUserId());
        Objects.requireNonNull(log.getActionTime());
        String day = DayRange.newInstance(log.getActionTime().getTime()).toString();
        String id = log.getUserId() + "-" + day + "-" + RandomUtils.nextObjectId();
        log.setId(id);
    }

    protected void addAndGet(Long userId, ActionEventType type, int delta) {

        UserAchievementRecord userAchievementRecord = userAchievementRecordDao.upsertAchievement(userId, type, delta);

        Achievement achievement = AchievementBuilder.build(userAchievementRecord, type);
        if (achievement.getRank() == 0) {
            return;
        }
        if (null == achievement.getType()) {
            return;
        }

        UserAchievementLog log = new UserAchievementLog();
        log.setLevel(achievement.getRank());
        log.setType(achievement.getType());
        log.setUserId(userId);
        log.generateId();
        try {
            userAchievementLogDao.insert(log);
        } catch (Exception ex) {
            if (!MongoExceptionUtils.isDuplicateKeyError(ex)) {
                throw ex;
            }
            return;
        }

        //生成班级头条
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(log.getUserId());
        if (null == clazz) return;
        User user = userLoaderClient.loadUser(log.getUserId());
        if (null == user) return;
        saveClazzAchievementHeadlineIfNecessary(clazz.getId(), user, log);
        saveAchievementHeadlineIfNecessary(clazz.getId(), user, log);

        sendAchievementRemind(user, log);
    }

    protected void addGrowth(ActionEvent event) {
        //学生等级上线后成长值就不再加了
//        if (getEventType().getDelta() == 0)
//            return;
//        userGrowthDao.addAndGet(event.getUserId(), getEventType().getDelta());
//
//        UserGrowthLog log = new UserGrowthLog();
//        log.setUserId(event.getUserId());
//        log.setActionTime(new Date(event.getTimestamp()));
//        log.setType(event.getType());
//        log.setDelta(getEventType().getDelta());
//        ensureId(log);
//        userGrowthLogDao.insert(log);
    }

    private void saveAchievementHeadlineIfNecessary(Long clazzId, User user, UserAchievementLog log) {
        zoneQueueServiceClient.createClazzJournal(clazzId)
                .withUser(log.getUserId())
                .withUser(user.fetchUserType())
                .withClazzJournalCategory(ClazzJournalCategory.APPLICATION_STD)
                .withClazzJournalType(ClazzJournalType.ACHIEVEMENT_HEADLINE)
                .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                        "achievementType", log.getType().name(),
                        "level", log.getLevel()
                ))).commit();
    }

    //如果是班级内第一个获得此成就,要发班级头条
    private void saveClazzAchievementHeadlineIfNecessary(Long clazzId, User user, UserAchievementLog log) {
        ClazzAchievementCount count = clazzAchievementCountDao.increase(clazzId, log.getType(), log.getLevel());
        assert count != null;
        if (SafeConverter.toInt(count.getCount()) == 1) {
            //记录班级成就墙
            ClazzAchievementLog clazzAchievementLog = new ClazzAchievementLog();
            clazzAchievementLog.setId(clazzId + "-" + log.getType().name() + "-" + log.getLevel());
            clazzAchievementLog.setUserId(user.getId());
            clazzAchievementLog.setAchievementType(log.getType().name());
            clazzAchievementLog.setAchievementLevel(log.getLevel());
            clazzAchievementLogDao.insert(clazzAchievementLog);

            zoneQueueServiceClient.createClazzJournal(clazzId)
                    .withUser(log.getUserId())
                    .withUser(user.fetchUserType())
                    .withClazzJournalCategory(ClazzJournalCategory.APPLICATION_STD)
                    .withClazzJournalType(ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE)
                    .withJournalJson(JsonUtils.toJson(MiscUtils.m(
                            "achievementType", log.getType().name(),
                            "level", log.getLevel()
                    ))).commit();

            sendClazzAchievementRemind(user, log);
        }
    }

    private void sendClazzAchievementRemind(User user, UserAchievementLog log) {
        AppMessage messages = new AppMessage();
        messages.setMessageType(StudentAppPushType.ACHIEVEMENT_WALL_HEAD_LINE_REMIND.getType());
        messages.setUserId(user.getId());
        messages.setTitle("成就提醒");
        messages.setViewed(false);
        messages.setContent("你达成了" + log.getType().getTitle() + log.getLevel() + "成就,登上了班级荣誉榜");

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("userName", user.fetchRealname());
        extInfo.put("userImg", user.fetchImageUrl());
        extInfo.put("achievementType", log.getType().name());
        extInfo.put("level", log.getLevel());

        messages.setExtInfo(extInfo);

        messageCommandServiceClient.getMessageCommandService().createAppMessage(messages);
    }

    private void sendAchievementRemind(User user, UserAchievementLog log) {
        AppMessage messages = new AppMessage();
        messages.setMessageType(StudentAppPushType.ACHIEVEMENT_HEAD_LINE_REMIND.getType());
        messages.setUserId(user.getId());
        messages.setTitle("成就提醒");
        messages.setViewed(false);
        messages.setContent("你达成了" + log.getType().getTitle() + log.getLevel() + "成就,登上了班级头条");

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("userName", user.fetchRealname());
        extInfo.put("userImg", user.fetchImageUrl());
        extInfo.put("achievementType", log.getType().name());
        extInfo.put("level", log.getLevel());

        messages.setExtInfo(extInfo);

        messageCommandServiceClient.getMessageCommandService().createAppMessage(messages);
    }
}
