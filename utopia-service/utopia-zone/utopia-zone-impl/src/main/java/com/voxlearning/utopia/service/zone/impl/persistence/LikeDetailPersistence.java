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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheValueLoaderExecutor;
import com.voxlearning.utopia.service.zone.api.constant.ZoneConstants;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-4-17
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.LikeDetailPersistence")
@UtopiaCacheSupport(LikeDetail.class)
public class LikeDetailPersistence extends StaticPersistence<Long, LikeDetail> {

    @Override
    protected void calculateCacheDimensions(LikeDetail source, Collection<String> dimensions) {
        // 请小心谨慎，因为业务原因，目前这里只允许有这么一个缓存维度。
        // 如果要修改此处，请读懂下面的逻辑
        dimensions.add(LikeDetail.cacheKeyFromJournalId(source.getJournalId()));
        dimensions.add(LikeDetail.cacheKeyFromJournalOwnerId(source.getJournalOwnerId()));
    }

    @Override
    public Long persist(LikeDetail entity) {
        Long id = super.persistIntoDatabase(entity);
        entity.setId(id);
        if (entity.getUserName() == null) entity.setUserName("");
        if (entity.getUserImg() == null) entity.setUserImg("");

        String key = LikeDetail.cacheKeyFromJournalId(entity.getJournalId());
        getCache().createCacheValueModifier()
                .key(key)
                .expiration(entityCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToList(currentValue, entity))
                .execute();

        key = LikeDetail.cacheKeyFromJournalOwnerId(entity.getJournalOwnerId());
        getCache().createCacheValueModifier()
                .key(key)
                .expiration(entityCacheExpirationInSeconds())
                .modifier(currentValue -> CollectionUtils.addToList(currentValue, entity))
                .execute();

        return id;
    }

    @Override
    public Collection<Long> persist(Collection<LikeDetail> entities) {
        throw new UnsupportedOperationException();
    }

    @UtopiaCacheable
    public List<LikeDetail> findByJournalOwnerId(@UtopiaCacheKey(name = "journalOwnerId") Long journalOwnerId) {
        return withSelectFromTable("WHERE JOURNAL_OWNER_ID=? AND CREATE_DATETIME>=?")
                .useParamsArgs(journalOwnerId, ZoneConstants.getClazzJournalStartDate())
                .queryAll();
    }

    @UtopiaCacheable
    public List<LikeDetail> findByJournalId(@UtopiaCacheKey(name = "journalId") Long journalId) {
        return withSelectFromTable("WHERE JOURNAL_ID=? AND CREATE_DATETIME>=?")
                .useParamsArgs(journalId, ZoneConstants.getClazzJournalStartDate())
                .queryAll();
    }

    public Map<Long, List<LikeDetail>> findByJournalIds(Collection<Long> journalIds) {
        CacheValueLoaderExecutor<Long, List<LikeDetail>> loader = getCache().createCacheValueLoader();
        return loader.keyGenerator(LikeDetail::cacheKeyFromJournalId)
                .externalLoader(this::internalFindByJournalIds)
                .keys(CollectionUtils.toLinkedHashSet(journalIds))
                .loads()
                .loadsMissed()
                .expiration(entityCacheExpirationInSeconds())
                .writeAsList()
                .getAndResortResult();
    }

    public List<LikeDetail> findByCreateDatetimeRange(Date start, Date end) {
        return withSelectFromTable("WHERE CREATE_DATETIME>=? AND CREATE_DATETIME<=?").useParamsArgs(start, end).queryAll();
    }

    public Page<LikeDetail> findByCreateDatetimeRange(Date start, Date end, Pageable pageable) {
        return withPageFromTable(pageable)
                .where("CREATE_DATETIME>=? AND CREATE_DATETIME<=?")
                .useParamsArgs(start, end)
                .orderBy("ID ASC")
                .queryPage();
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private Map<Long, List<LikeDetail>> internalFindByJournalIds(Collection<Long> journalIds) {
        return withSelectFromTable("WHERE JOURNAL_ID IN (:journalIds) AND CREATE_DATETIME>=:start")
                .useParams(MiscUtils.m("journalIds", journalIds, "start", ZoneConstants.getClazzJournalStartDate()))
                .queryAll()
                .stream()
                .collect(Collectors.groupingBy(LikeDetail::getJournalId));
    }
}