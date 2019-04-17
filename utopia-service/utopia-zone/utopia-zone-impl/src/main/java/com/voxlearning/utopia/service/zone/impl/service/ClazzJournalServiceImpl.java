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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.ClazzJournalService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneComment;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzJournalPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneCommentPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.LikeDetailPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExposeService(interfaceClass = ClazzJournalService.class)
public class ClazzJournalServiceImpl extends SpringContainerSupport implements ClazzJournalService {

    @Inject private ClazzJournalPersistence clazzJournalPersistence;
    @Inject private ClazzZoneCommentPersistence clazzZoneCommentPersistence;
    @Inject private LikeDetailPersistence likeDetailPersistence;
    @Inject private StudentInfoPersistence studentInfoPersistence;

    @Override
    public MapMessage like(User user, Long journalId, User relevantUser, Long clazzId) {
        // 存赞明细
        LikeDetail detail = new LikeDetail();
        detail.setJournalId(journalId);
        detail.setJournalOwnerId(relevantUser.getId());
        detail.setJournalOwnerClazzId(clazzId);
        detail.setUserId(user.getId());
        detail.setUserName(user.fetchRealname());
        detail.setUserImg(user.fetchImageUrl());
        try {
            likeDetailPersistence.persist(detail);
        } catch (DuplicateKeyException ex) {
            // already liked this journal
            return MapMessage.errorMessage("请不要重复点赞");
        }

        // 更新这条新鲜事获得的总赞数
        clazzJournalPersistence.increaseLikeCount(journalId);

        // 更新新鲜事主获得的总赞数
        studentInfoPersistence.createOrIncreaseLikeCountByOne(relevantUser.getId());

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage likeLearningCycle(User user, String userName, Long journalId, User relevantUser, Long clazzId) {
        // 存赞明细
        LikeDetail detail = new LikeDetail();
        detail.setJournalId(journalId);
        detail.setJournalOwnerId(relevantUser.getId());
        detail.setJournalOwnerClazzId(clazzId);
        detail.setUserId(user.getId());
        detail.setUserName(userName);
        detail.setUserImg(user.fetchImageUrl());
        try {
            likeDetailPersistence.persist(detail);
        } catch (DuplicateKeyException ex) {
            return MapMessage.errorMessage("请不要重复点赞");
        }
        // 更新这条新鲜事获得的总赞数
        clazzJournalPersistence.increaseLikeCount(journalId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage comment(User student, User relevantUser, Long journalId, Long clazzId, Long imageId) {
        // 存表情评论明细
        ClazzZoneComment comment = new ClazzZoneComment();
        comment.setJournalId(journalId);
        comment.setJournalOwnerId(relevantUser.getId());
        comment.setJournalOwnerClazzId(clazzId);
        comment.setUserId(student.getId());
        comment.setUserName(student.fetchRealname());
        comment.setImgComment(imageId);
        comment.setUserImg(student.fetchImageUrl());
        try {
            clazzZoneCommentPersistence.persist(comment);
        } catch (DuplicateKeyException ex) {
            return MapMessage.errorMessage("请不要重复发表评论");
        }
        return MapMessage.successMessage("评论成功");
    }

    @Override
    public MapMessage delete(Long journalId, Long relevantUserId) {
        int rows = clazzJournalPersistence.deleteJournal(journalId, relevantUserId);
        return new MapMessage().setSuccess(rows > 0);
    }
}
