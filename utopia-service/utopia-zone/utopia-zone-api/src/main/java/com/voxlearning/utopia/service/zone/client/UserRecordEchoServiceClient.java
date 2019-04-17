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
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.entity.comment.UserRecordSnapshot;
import com.voxlearning.utopia.service.zone.api.UserRecordEchoService;
import lombok.Getter;


@Deprecated
public class UserRecordEchoServiceClient {

    @Getter
    @ImportService(interfaceClass = UserRecordEchoService.class)
    private UserRecordEchoService remoteReference;

    public MapMessage commentClazzJournal(Long journalId, UserRecordSnapshot snapshot) {
        if (journalId == null || journalId == 0L || !snapshot.validComment()) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("UserRecordEchoService:commentClazzJournal")
                    .keys(journalId)
                    .callback(() -> remoteReference.commentClazzJournal(journalId, snapshot))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }
    }

}
