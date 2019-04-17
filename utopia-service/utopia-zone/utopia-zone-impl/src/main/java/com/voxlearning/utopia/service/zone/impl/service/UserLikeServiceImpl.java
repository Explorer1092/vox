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

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.zone.api.UserLikeService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.impl.dao.*;
import com.voxlearning.utopia.service.zone.impl.loader.ClazzJournalLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Named("com.voxlearning.utopia.service.zone.impl.service.UserLikeServiceImpl")
@ExposeService(interfaceClass = UserLikeService.class)
public class UserLikeServiceImpl extends SpringContainerSupport implements UserLikeService {

    @Inject private UserRecordEchoDao userRecordEchoDao;
    @Inject private RecordLikeInfoDao recordLikeInfoDao;
    @Inject private UserLikedSummaryDao userLikedSummaryDao;
    @Inject private ClazzJournalLoaderImpl clazzJournalLoader;
    @Inject private UserLoaderClient userLoaderClient;

    @Override
    public MapMessage like(String recordId, UserLikeType likeType, Long likerId, String likerName, Long userId, Map<String, Object> attributes) {
        if (likeType == null || StringUtils.isBlank(recordId) || likerId == null || userId == null) {
            return MapMessage.errorMessage("参数错误");
        }

        User liker = userLoaderClient.loadUser(likerId);
        if (liker == null) {
            return MapMessage.errorMessage("无效的用户ID");
        }

        try {
            String likeUserName = liker.fetchRealname();
            if (StringUtils.isNoneBlank(likerName)) {
                likeUserName = likerName;
            }

            // 1. 根据类型记录点赞的详细信息
            recordLikeInfoDao.liked(likeType, recordId, liker.getId(), likeUserName, new Date());

            // 2. 记录用户的点赞次数
            if (!Objects.equals(99999L, userId)) {
                Date now = new Date();
                userLikedSummaryDao.liked(userId, liker.getId(), recordId, now);
            }

        } catch (Exception ex) {
            logger.error("Failed record user like info. liker={}, user={}, likeType={}", likerId, userId, likeType, ex);
            return MapMessage.errorMessage("系统异常");
        }

        return MapMessage.successMessage();
    }


    @Override
    public UserRecordEcho loadLikeRecord(UserLikeType likeType, String recordId) {
        if (StringUtils.isBlank(recordId) || likeType == null) {
            return null;
        }

        String recordEchoId = StringUtils.join(recordId, "_", likeType.name());
        return userRecordEchoDao.load(recordEchoId);
    }

    @Override
    public UserRecordEcho loadCommentRecord(UserLikeType likeType, String recordId) {
        if (StringUtils.isBlank(recordId) || likeType == null) {
            return null;
        }

        String recordEchoId = StringUtils.join(recordId, "_", likeType.name());
        return userRecordEchoDao.load(recordEchoId);
    }

    @Override
    public RecordLikeInfo loadRecordLikeInfo(UserLikeType likeType, String recordId) {
        return recordLikeInfoDao.loadRecordLikeInfo(likeType, recordId);
    }

    @Override
    public UserLikedSummary loadUserLikedSummary(Long userId, Date actionTime) {
        Date actTime = actionTime;

        if (actTime == null) {
            actTime = new Date();
        }

        return userLikedSummaryDao.loadUserLikedSummary(userId, actTime);
    }

    @Override
    public MapMessage commentClazzJournal(Long journalId, UserRecordSnapshot snapshot) {
        if (journalId == null || journalId == 0L) {
            return MapMessage.errorMessage("这条新鲜事已经被删除了哦");
        }

        ClazzJournal journal = clazzJournalLoader.loadClazzJournal(journalId);
        if (journal == null || journal.getJournalType() == null) {
            return MapMessage.errorMessage("这条新鲜事已经被删除了哦").add("errCode", "1032");
        }

        if (!snapshot.validComment()) {
            return MapMessage.errorMessage("无效的评论信息").add("errCode", "1035");
        }

        UserRecordEcho echo = loadCommentRecord(UserLikeType.CLAZZ_JOURNAL, SafeConverter.toString(journal.getId()));
        if (echo == null) {
            echo = UserRecordEcho.createByClazzJournal(journal);
        }
        echo.comment(snapshot);

        UserRecordEcho upsert = userRecordEchoDao.upsert(echo);

        return new MapMessage().setSuccess(upsert != null);
    }

    @Override
    public MapMessage commentClazzRecord(String recordId, UserRecordSnapshot snapshot) {
        if (StringUtils.isBlank(recordId)) {
            return MapMessage.errorMessage("这条新鲜事已经被删除了哦");
        }

        if (!snapshot.validComment()) {
            return MapMessage.errorMessage("无效的评论信息").add("errCode", "1035");
        }

        // 出一些日志好查数据
        if (SafeConverter.toString(snapshot.getUserId()).endsWith("0")) {
            logger.info("commentClazzRecord with record id {}", recordId);
        }

        UserRecordEcho echo = loadCommentRecord(UserLikeType.CLAZZ_RECORD, recordId);
        if (echo == null) {
            echo = UserRecordEcho.createByClazzRecord(recordId, UserLikeType.CLAZZ_RECORD);
        }
        echo.comment(snapshot);

        UserRecordEcho upsert = userRecordEchoDao.upsert(echo);

        return new MapMessage().setSuccess(upsert != null);
    }

    @Override
    public MapMessage recallCommentClazzJournal(Long journalId, Long userId, String comment) {
        if (journalId == null || userId == null || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("无效的参数");
        }
        ClazzJournal journal = clazzJournalLoader.loadClazzJournal(journalId);
        if (journal == null || journal.getJournalType() == null) {
            return MapMessage.errorMessage("这条新鲜事已经被删除了哦").add("errCode", "1032");
        }
        UserRecordEcho echo = $findByClazzJournal(journal.getId());
        if (echo == null) {
            return MapMessage.successMessage();
        }
        echo.recallComment(userId, comment);

        UserRecordEcho upsert = userRecordEchoDao.upsert(echo);

        return new MapMessage().setSuccess(upsert != null);
    }

    private UserRecordEcho $findByClazzJournal(Long journalId) {
        if (journalId == null || journalId == 0L) {
            return null;
        }

        return loadCommentRecord(UserLikeType.CLAZZ_JOURNAL, journalId.toString());
    }

}