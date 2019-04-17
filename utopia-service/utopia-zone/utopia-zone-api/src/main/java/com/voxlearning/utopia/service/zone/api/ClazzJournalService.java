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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20150821")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzJournalService extends IPingable {
    /**
     * 给班级新鲜事点赞（可以自己给自己点赞）
     *
     * @param user         点赞的用户
     * @param journalId    班级新鲜事ID
     * @param relevantUser 被赞的用户
     * @param clazzId      班级ID
     * @return map message
     */
    MapMessage like(User user, Long journalId, User relevantUser, Long clazzId);

    /**
     * 给学习圈点赞
     *
     * @param user         点赞的用户
     * @param userName     点赞的用户名
     * @param journalId    班级新鲜事ID
     * @param relevantUser 被赞的用户
     * @param clazzId      班级ID
     * @return map message
     */
    MapMessage likeLearningCycle(User user,String userName, Long journalId, User relevantUser, Long clazzId);

    /**
     * 评论班级新鲜事（可以自己评论自己）
     *
     * @param student      评论的用户
     * @param relevantUser 被评论的用户
     * @param journalId    班级新鲜事ID
     * @param clazzId      班级ID
     * @param imageId      评论表情ID
     * @return map message
     */
    MapMessage comment(User student, User relevantUser, Long journalId, Long clazzId, Long imageId);

    MapMessage delete(Long journalId, Long relevantUserId);
}
