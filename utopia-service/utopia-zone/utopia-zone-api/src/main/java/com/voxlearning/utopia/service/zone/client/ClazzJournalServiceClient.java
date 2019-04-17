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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.ClazzJournalService;
import com.voxlearning.utopia.service.zone.api.constant.ZoneConstants;
import lombok.Getter;
import org.slf4j.Logger;

import javax.inject.Inject;

public class ClazzJournalServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ClazzJournalServiceClient.class);

    @Getter
    @ImportService(interfaceClass = ClazzJournalService.class)
    private ClazzJournalService clazzJournalService;

    @Inject private ZoneLikeServiceClient zoneLikeServiceClient;

    public MapMessage like(User user, Long journalId, User relevantUser, Long clazzId) {
        if (user == null || journalId == null || relevantUser == null || clazzId == null) {
            return MapMessage.errorMessage("点赞失败");
        }
        if (relevantUser.isTeacher()) {
            return MapMessage.errorMessage("抱歉~暂时不能给老师点赞");
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            MapMessage response = builder.keyPrefix("ClazzJournalServiceClient_like")
                    .keys(user.getId(), journalId)
                    .callback(() -> clazzJournalService.like(user, journalId, relevantUser, clazzId))
                    .build()
                    .execute();
            if (!response.isSuccess()) {
                return response;
            }
            // 更新新鲜事主当日获得赞数的缓存
            zoneLikeServiceClient.getZoneLikeService().increaseLikedCount(relevantUser.getId()).awaitUninterruptibly();
            return MapMessage.successMessage("点赞成功");
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("请不要重复点赞").withDuplicatedException();
        } catch (Exception ex) {
            logger.error("Student {} failed to like journal", user.getId(), ex);
            return MapMessage.errorMessage("点赞失败");
        }
    }

    public MapMessage likeLearningCycle(User user, String userName, Long journalId, User relevantUser, Long clazzId) {
        if (user == null || journalId == null || relevantUser == null || clazzId == null) {
            return MapMessage.errorMessage("点赞失败");
        }
        if (relevantUser.isTeacher()) {
            return MapMessage.errorMessage("抱歉~暂时不能给老师点赞");
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            MapMessage response = builder.keyPrefix("ClazzJournalServiceClient_likeLearningCycle")
                    .keys(user.getId(), journalId)
                    .callback(() -> clazzJournalService.likeLearningCycle(user, userName, journalId, relevantUser, clazzId))
                    .build()
                    .execute();
            if (!response.isSuccess()) {
                return response;
            }
            return MapMessage.successMessage("点赞成功");
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("请不要重复点赞").withDuplicatedException();
        } catch (Exception ex) {
            logger.error("Student {} failed to like journal", user.getId(), ex);
            return MapMessage.errorMessage("点赞失败");
        }
    }

    public MapMessage comment(User student, User relevantUser, Long journalId, Long clazzId, Long imageId) {
        if (student == null || relevantUser == null || journalId == null || clazzId == null) {
            return MapMessage.errorMessage("评论失败");
        }
        if (!ZoneConstants.IMG_COMMENT.contains(imageId)) {
            return MapMessage.errorMessage("表情不存在");
        }
        if (relevantUser.isTeacher()) {
            return MapMessage.errorMessage("抱歉~暂时不能给老师评论");
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("ClazzJournalServiceClient_comment")
                    .keys(student.getId(), journalId)
                    .callback(() -> clazzJournalService.comment(student, relevantUser, journalId, clazzId, imageId))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("请不要重复评论").withDuplicatedException();
        } catch (Exception ex) {
            logger.error("Student {} failed to comment journal", student.getId(), ex);
            return MapMessage.errorMessage("评论失败");
        }
    }

    public MapMessage delete(Long journalId, Long relevantUserId) {
        if (journalId == null || relevantUserId == null) {
            return MapMessage.errorMessage();
        }
        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("ClazzJournalServiceClient_delete")
                    .keys(relevantUserId, journalId)
                    .callback(() -> clazzJournalService.delete(journalId, relevantUserId))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage().withDuplicatedException();
        }
    }
}
