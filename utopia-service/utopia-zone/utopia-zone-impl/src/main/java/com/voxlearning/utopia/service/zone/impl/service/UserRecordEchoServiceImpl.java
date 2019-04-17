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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.service.zone.api.UserRecordEchoService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.impl.dao.UserRecordEchoDao;
import com.voxlearning.utopia.service.zone.impl.loader.ClazzJournalLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.zone.impl.service.UserRecordEchoServiceImpl")
@ExposeService(interfaceClass = UserRecordEchoService.class)
public class UserRecordEchoServiceImpl implements UserRecordEchoService {

    @Inject private UserRecordEchoDao userRecordEchoDao;
    @Inject private ClazzJournalLoaderImpl clazzJournalLoader;

    @Override
    public UserRecordEcho findClazzJournalEcho(Long journalId) {
        return $findByClazzJournal(journalId);
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

        UserRecordEcho echo = $findByClazzJournal(journal.getId());
        if (echo == null) {
            echo = UserRecordEcho.createByClazzJournal(journal);
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

        String recordId = journalId + "_CLAZZ_JOURNAL";
        return userRecordEchoDao.load(recordId);
    }

}