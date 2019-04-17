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

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180202")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
public interface UserLikeService extends IPingable {

    /**
     * 用户点赞数据收集
     *
     * @param recordId   记录ID
     * @param likeType   点赞类型
     * @param likerId    点赞人ID
     * @param userId     被点赞人ID
     * @param attributes 附加信息
     */
    MapMessage like(String recordId, UserLikeType likeType, Long likerId, String likerName, Long userId, Map<String, Object> attributes);

    RecordLikeInfo loadRecordLikeInfo(UserLikeType likeType, String recordId);

    UserLikedSummary loadUserLikedSummary(Long userId, Date actionTime);

    MapMessage commentClazzJournal(Long journalId, UserRecordSnapshot snapshot);

    MapMessage commentClazzRecord(String recordId, UserRecordSnapshot snapshot);

    @Deprecated
    UserRecordEcho loadLikeRecord(UserLikeType likeType, String recordId);

    UserRecordEcho loadCommentRecord(UserLikeType likeType, String recordId);

    MapMessage recallCommentClazzJournal(Long journalId, Long userId, String comment);
}