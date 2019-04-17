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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.zone.api.UserLikeService;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

public class UserLikeServiceClient {

    @Getter
    @ImportService(interfaceClass = UserLikeService.class)
    private UserLikeService remoteReference;

    public MapMessage like(String recordId, UserLikeType type, Long likerId, String likerName, Long userId, Map<String, Object> attributes) {
        if (type == null || StringUtils.isBlank(recordId) || likerId == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("UserLikeService:like")
                    .keys(type, recordId)
                    .callback(() -> remoteReference.like(recordId, type, likerId, likerName, userId, attributes))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public RecordLikeInfo loadRecordLikeInfo(UserLikeType likeType, String recordId) {
        return remoteReference.loadRecordLikeInfo(likeType, recordId);
    }

    public UserLikedSummary loadUserLikedSummary(Long userId, Date actionTime) {
        return remoteReference.loadUserLikedSummary(userId, actionTime);
    }

    public MapMessage commentClazzJournal(Long journalId, UserRecordSnapshot snapshot) {
        if (journalId == null || !snapshot.validComment()) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("UserLikeService:commentClazzJournal")
                    .keys(journalId)
                    .callback(() -> remoteReference.commentClazzJournal(journalId, snapshot))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public MapMessage commentClazzRecord(String recordId, UserRecordSnapshot snapshot) {
        if (StringUtils.isBlank(recordId) || !snapshot.validComment()) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("UserLikeService:commentClazzRecord")
                    .keys(recordId)
                    .callback(() -> remoteReference.commentClazzRecord(recordId, snapshot))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

    public UserRecordEcho loadLikeRecord(UserLikeType likeType, String recordId) {
        return remoteReference.loadLikeRecord(likeType, recordId);
    }

    public UserRecordEcho loadCommentRecord(UserLikeType likeType, String recordId) {
        return remoteReference.loadCommentRecord(likeType, recordId);
    }

}
