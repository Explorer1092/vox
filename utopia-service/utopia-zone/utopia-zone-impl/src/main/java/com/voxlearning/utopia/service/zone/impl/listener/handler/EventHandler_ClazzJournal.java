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

package com.voxlearning.utopia.service.zone.impl.listener.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableObject;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzJournalPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For handling received CLAZZ_JOURNAL message.
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
@Named
public class EventHandler_ClazzJournal extends SpringContainerSupport implements ZoneEventHandler {

    @Inject private ClazzJournalPersistence clazzJournalPersistence;
    @Inject private GlobalTagServiceClient globalTagServiceClient;

    private static final List<ClazzJournalType> grayJournalTypes = Arrays.asList(
            ClazzJournalType.GROWN_WORD_NEW_PET,
            ClazzJournalType.GROWN_WORD_PET_LEVEL_UP,
            ClazzJournalType.CLASS_BOSS_CHALLENGE_RANK,
            ClazzJournalType.COMPETITION_ISLAND_LEVEL_UP,
            ClazzJournalType.COMPETITION_ISLAND_SEASON_CLASS_TOP3,
            ClazzJournalType.WONDERLAND_NEW_MEDAL,
            ClazzJournalType.WONDERLAND_MEDAL_GRADE,
            ClazzJournalType.NORMAL_CLASS_COMPETITION_INVITE_MATE,
            ClazzJournalType.RECESSIVE_CLASS_COMPETITION_INVITE_MATE
    );

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.CLAZZ_JOURNAL;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        long timestamp = System.currentTimeMillis();
        JsonNode timestampNode = root.get("TS");
        if (timestampNode != null) {
            timestamp = timestampNode.asLong();
        }

        JournalDuplicationPolicy policy = JournalDuplicationPolicy.NONE;
        JsonNode policyNode = root.get("P");
        if (policyNode != null) {
            try {
                policy = JournalDuplicationPolicy.valueOf(policyNode.asText());
            } catch (Exception ex) {
                policy = JournalDuplicationPolicy.NONE;
            }
        }

        JsonNode clazzJournalNode = root.get("CJ");
        if (clazzJournalNode == null) {
            logger.warn("No CJ node");
            return;
        }
        ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
        ClazzJournal clazzJournal = mapper.readValue(clazzJournalNode.traverse(), ClazzJournal.class);

        if (grayJournalTypes.contains(clazzJournal.getJournalType())) {
            if (globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.NoStudentHeadlineShare.name()).stream()
                    .map(GlobalTag::getTagValue).collect(Collectors.toList())
                    .contains(ConversionUtils.toString(clazzJournal.getRelevantUserId()))) {
                return;
            }

        }

        clazzJournal.setCreateDatetime(new Date(timestamp));
        clazzJournal.setLikeCount(0);

        final MutableObject<Long> journalId = new MutableObject<>(null);

        boolean skipJournalPersist = false;
        if (policy == JournalDuplicationPolicy.DAILY) {
            String cacheKey;
            JsonNode journalKey = root.get("K");
            if (journalKey != null && StringUtils.isNotBlank(journalKey.asText())) {
                cacheKey = CacheKeyGenerator.generateCacheKey(EventHandler_ClazzJournal.class,
                        new String[]{"USER_ID", "JOURNAL_TYPE", "KEY"},
                        new Object[]{clazzJournal.getRelevantUserId(), clazzJournal.getJournalType(), journalKey.asText()});
            } else {
                cacheKey = CacheKeyGenerator.generateCacheKey(EventHandler_ClazzJournal.class,
                        new String[]{"USER_ID", "JOURNAL_TYPE"},
                        new Object[]{clazzJournal.getRelevantUserId(), clazzJournal.getJournalType()});
            }

            Long ret = CacheSystem.CBS.getCache("persistence")
                    .incr(cacheKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
            if (SafeConverter.toLong(ret) != 1) {
                // 没有拿到那个1，忽略写数据库
                // 只有拿到1的才是今天的第一次，允许写数据库
                skipJournalPersist = true;
            }
        }

        if (!skipJournalPersist) {
            journalId.setValue(clazzJournalPersistence.persist(clazzJournal));
        }

    }
}
