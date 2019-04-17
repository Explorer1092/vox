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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.cache.CacheValueLoaderExecutor;
import com.voxlearning.utopia.service.zone.api.constant.ZoneConstants;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneComment;
import org.springframework.dao.DataAccessException;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-5-19
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneCommentPersistence")
@UtopiaCacheSupport(ClazzZoneComment.class)
public class ClazzZoneCommentPersistence extends StaticPersistence<Long, ClazzZoneComment> {

    @Override
    protected void calculateCacheDimensions(ClazzZoneComment source, Collection<String> dimensions) {
        // 请修改时小心谨慎，目前只支持这个唯一的缓存维度
        // 如果要修改这里请务必要读清楚下面的逻辑
        dimensions.add(ClazzZoneComment.cacheKeyFromJournalId(source.getJournalId()));
        dimensions.add(ClazzZoneComment.cacheKeyFromJournalOwnerId(source.getJournalOwnerId()));
    }

    @Override
    public Long persist(ClazzZoneComment entity) throws DataAccessException {
        Long id = persistIntoDatabase(entity);
        entity.setId(id);

        String key = ClazzZoneComment.cacheKeyFromJournalId(entity.getJournalId());
        getCache().createCacheValueModifier()
                .key(key)
                .expiration(entityCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToList(currentValue, entity))
                .execute();

        key = ClazzZoneComment.cacheKeyFromJournalOwnerId(entity.getJournalOwnerId());
        getCache().createCacheValueModifier()
                .key(key)
                .expiration(entityCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToList(currentValue, entity))
                .execute();

        return id;
    }

    @UtopiaCacheable
    public List<ClazzZoneComment> findByJournalOwnerId(@UtopiaCacheKey(name = "journalOwnerId") Long journalOwnerId) {
        return withSelectFromTable("WHERE JOURNAL_OWNER_ID=? AND CREATE_DATETIME>=?")
                .useParamsArgs(journalOwnerId, ZoneConstants.getClazzJournalStartDate())
                .queryAll();
    }

    @UtopiaCacheable
    public List<ClazzZoneComment> findByJournalId(@UtopiaCacheKey(name = "JID") Long journalId) {
        return withSelectFromTable("WHERE JOURNAL_ID=? AND CREATE_DATETIME>=?")
                .useParamsArgs(journalId, ZoneConstants.getClazzJournalStartDate())
                .queryAll();
    }

    public Map<Long, List<ClazzZoneComment>> findByJournalIds(Collection<Long> journalIds) {
        CacheValueLoaderExecutor<Long, List<ClazzZoneComment>> loader = getCache().createCacheValueLoader();
        return loader.keyGenerator(ClazzZoneComment::cacheKeyFromJournalId)
                .externalLoader(this::internalFindByJournalIds)
                .keys(CollectionUtils.toLinkedHashSet(journalIds))
                .loads()
                .loadsMissed()
                .expiration(entityCacheExpirationInSeconds())
                .writeAsList()
                .getAndResortResult();
    }

    private Map<Long, List<ClazzZoneComment>> internalFindByJournalIds(Collection<Long> journalIds) {
        return withSelectFromTable("WHERE JOURNAL_ID IN (:journalIds) AND CREATE_DATETIME>=:start")
                .useParams(MiscUtils.map("journalIds", journalIds, "start", ZoneConstants.getClazzJournalStartDate()))
                .queryAll()
                .stream()
                .collect(Collectors.groupingBy(ClazzZoneComment::getJournalId));
    }
}
