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

package com.voxlearning.utopia.service.zone.support;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.api.ZoneQueueService;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy.NONE;

public class ClazzJournalCreator {
    private static final Logger logger = LoggerFactory.getLogger(ClazzJournalCreator.class);

    private final ZoneQueueService zoneQueueService;

    public ClazzJournalCreator(ZoneQueueService zoneQueueService) {
        this.zoneQueueService = Objects.requireNonNull(zoneQueueService);
    }

    private Long userId;
    private UserType userType;
    private Long groupId;
    private Long clazzId;
    private ClazzJournalType clazzJournalType;
    private ClazzJournalCategory clazzJournalCategory;
    private String journalJson;
    private JournalDuplicationPolicy policy = NONE;
    private String key;

    public ClazzJournalCreator withUser(Long userId) {
        this.userId = userId;
        return this;
    }

    public ClazzJournalCreator withUser(UserType userType) {
        this.userType = userType;
        return this;
    }

    public ClazzJournalCreator withGroup(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public ClazzJournalCreator withClazz(Long clazzId) {
        this.clazzId = clazzId;
        return this;
    }

    public ClazzJournalCreator withClazzJournalType(ClazzJournalType clazzJournalType) {
        this.clazzJournalType = clazzJournalType;
        return this;
    }

    public ClazzJournalCreator withClazzJournalCategory(ClazzJournalCategory clazzJournalCategory) {
        this.clazzJournalCategory = clazzJournalCategory;
        return this;
    }

    public ClazzJournalCreator withJournalJson(String journalJson) {
        this.journalJson = journalJson;
        return this;
    }

    public ClazzJournalCreator withPolicy(JournalDuplicationPolicy policy) {
        this.policy = policy;
        return this;
    }

    public ClazzJournalCreator withKey(String key) {
        this.key = key;
        return this;
    }

    public void commit() {
        if (clazzId == null) {
            logger.warn("No clazz specified when creating ClazzJournal");
            return;
        }
        if (userId == null) {
            logger.warn("No user specified when creating ClazzJournal");
            return;
        }
        if (userType == null) {
            logger.warn("No user type specified when creating ClazzJournal");
            return;
        }
        if (clazzJournalType == null) {
            logger.warn("No ClazzJournalType specified, 'UNKNOWN' will be used");
            return;
        }
        if (StringUtils.isBlank(journalJson)) {
            logger.warn("No journalJson specified when creating ClazzJournal");
            return;
        }
        createClazzJournal();
    }

    private void createClazzJournal() {
        Map<String, Object> clazzJournal = new LinkedHashMap<>();
        clazzJournal.put("clazzId", clazzId);
        clazzJournal.put("groupId", groupId);
        clazzJournal.put("relevantUserId", userId);
        clazzJournal.put("relevantUserType", userType);
        clazzJournal.put("journalType", clazzJournalType);
        clazzJournal.put("journalCategory", clazzJournalCategory);
        clazzJournal.put("journalJson", journalJson);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("TS", System.currentTimeMillis());
        message.put("CJ", clazzJournal);
        message.put("P", policy == null ? null : policy.name());
        message.put("T", ZoneEventType.CLAZZ_JOURNAL);
        message.put("K", key);

        String json = JsonStringSerializer.getInstance().serialize(message);
        zoneQueueService.sendMessage(Message.newMessage().withStringBody(json));
    }

}
