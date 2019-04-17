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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.ZoneConstants;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.zone.impl.persistence.ClazzJournalPersistence")
@UtopiaCacheSupport(ClazzJournal.class)
public class ClazzJournalPersistence extends StaticPersistence<Long, ClazzJournal> {

    @Override
    protected void calculateCacheDimensions(ClazzJournal source, Collection<String> dimensions) {
        // 修改时请务必读清楚下面的逻辑，这里做了很多手工保持缓存同步的工作
        dimensions.add(ClazzJournal.cacheKeyFromId(source.getId()));
        dimensions.add(ClazzJournal.cacheKeyFromClazzId(source.getClazzId()));
        dimensions.add(ClazzJournal.cacheKeyFromUserId(source.getRelevantUserId()));
        dimensions.add(ClazzJournal.cacheKeyFromJournalType(source.getJournalType()));
    }

    @Override
    public Collection<Long> persist(Collection<ClazzJournal> entities) throws DataAccessException {
        // No multiple entities persist supported
        throw new UnsupportedOperationException();
    }

    @Override
    public Long persist(ClazzJournal entity) throws DataAccessException {
        Long id = super.persistIntoDatabase(entity);
        entity.setId(id);
        if (entity.getLikeCount() == null) entity.setLikeCount(0);
        if (entity.getJournalCategory() == null) entity.setJournalCategory(ClazzJournalCategory.MISC);

        // A. 主动写入ID对应的缓存
        String key = ClazzJournal.cacheKeyFromId(id);
        getCache().add(key, entityCacheExpirationInSeconds(), entity);

        // B. 更新CLAZZ_ID对应的缓存
        key = ClazzJournal.cacheKeyFromClazzId(entity.getClazzId());
        getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                currentValue -> CollectionUtils.addToSet(currentValue, entity.toComplexID()));

        // C. 更新RELEVENT_USER_ID对应的缓存
        key = ClazzJournal.cacheKeyFromUserId(entity.getRelevantUserId());
        getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                currentValue -> CollectionUtils.addToSet(currentValue, entity.toComplexID()));

        // D. 更新JOURNAL_TYPE对应的缓存
        key = ClazzJournal.cacheKeyFromJournalType(entity.getJournalType());
        getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                currentValue -> CollectionUtils.addToSet(currentValue, entity.toComplexID()));

        return id;
    }

    private static final RowMapper<ClazzJournal.ComplexID> ROW_MAPPER = (rs, rowNum) -> {
        ClazzJournal.ComplexID ID = new ClazzJournal.ComplexID();
        ID.setId(rs.getLong("ID"));
        ID.setClazzId(rs.getLong("CLAZZ_ID"));
        ID.setGroupId(rs.getLong("CLAZZ_GROUP_ID"));
        ID.setType(ClazzJournalType.valueOf(rs.getString("JOURNAL_TYPE")).getId());
        ID.setCategory(ClazzJournalCategory.valueOf(rs.getString("JOURNAL_CATEGORY")).getId());
        return ID;
    };

    @UtopiaCacheable
    public Set<ClazzJournal.ComplexID> queryByClazzId(@UtopiaCacheKey(name = "CID") Long clazzId) {
        String sql = "SELECT ID,CLAZZ_ID,CLAZZ_GROUP_ID,JOURNAL_TYPE,JOURNAL_CATEGORY " +
                "FROM VOX_CLAZZ_JOURNAL WHERE CLAZZ_ID=? AND CREATE_DATETIME>=?";
        return utopiaSql.withSql(sql)
                .useParamsArgs(clazzId, ZoneConstants.getClazzJournalStartDate())
                .queryAll(ROW_MAPPER)
                .stream()
                .collect(Collectors.toSet());
    }

    @UtopiaCacheable
    public Set<ClazzJournal.ComplexID> queryByUserId(@UtopiaCacheKey(name = "UID") Long userId) {
        String sql = "SELECT ID,CLAZZ_ID,CLAZZ_GROUP_ID,JOURNAL_TYPE,JOURNAL_CATEGORY " +
                "FROM VOX_CLAZZ_JOURNAL WHERE RELEVANT_USER_ID=? AND CREATE_DATETIME>=?";
        return utopiaSql.withSql(sql)
                .useParamsArgs(userId, ZoneConstants.getClazzJournalStartDate())
                .queryAll(ROW_MAPPER)
                .stream()
                .collect(Collectors.toSet());
    }

    @UtopiaCacheable
    public Set<ClazzJournal.ComplexID> queryByJournalType(@UtopiaCacheKey(name = "JT") Integer journalType) {
        if (journalType == null) {
            return Collections.emptySet();
        }
        String sql = "SELECT ID,CLAZZ_ID,CLAZZ_GROUP_ID,JOURNAL_TYPE,JOURNAL_CATEGORY " +
                "FROM VOX_CLAZZ_JOURNAL WHERE JOURNAL_TYPE=? AND CREATE_DATETIME>=?";
        return utopiaSql.withSql(sql)
                .useParamsArgs(ClazzJournalType.safeParse(journalType).name(), ZoneConstants.getClazzJournalStartDate())
                .queryAll(ROW_MAPPER)
                .stream()
                .collect(Collectors.toSet());
    }

    public void increaseLikeCount(Long id) {
        Date current = new Date();
        int rows = withUpdateTable("SET LIKE_COUNT=LIKE_COUNT+1,UPDATE_DATETIME=? WHERE ID=?")
                .useParamsArgs(current, id)
                .executeUpdate();
        if (rows > 0) {
            String key = ClazzJournal.cacheKeyFromId(id);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<ClazzJournal>() {
                        @Override
                        public ClazzJournal changeCacheObject(ClazzJournal currentValue) {
                            currentValue.setLikeCount(SafeConverter.toInt(currentValue.getLikeCount()) + 1);
                            currentValue.setUpdateDatetime(current);
                            return currentValue;
                        }
                    });
        }
    }

    public void updateJournalJson(Long id, String journalJson) {
        Date current = new Date();
        int rows = withUpdateTable("SET JOURNAL_JSON=?,UPDATE_DATETIME=? WHERE ID=?")
                .useParamsArgs(journalJson, current, id).executeUpdate();
        if (rows > 0) {
            String key = ClazzJournal.cacheKeyFromId(id);
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<ClazzJournal>() {
                        @Override
                        public ClazzJournal changeCacheObject(ClazzJournal currentValue) {
                            currentValue.setJournalJson(journalJson);
                            currentValue.setUpdateDatetime(current);
                            return currentValue;
                        }
                    });
        }
    }

    @SuppressWarnings("unchecked")
    private Set removeFromSet(Object currentValue, Long id) {
        if (!(currentValue instanceof Set)) {
            throw new IllegalStateException();
        }
        Set set = new HashSet((Set) currentValue);
        for (Iterator it = set.iterator(); it.hasNext(); ) {
            ClazzJournal.ComplexID ID = (ClazzJournal.ComplexID) it.next();
            if (Objects.equals(ID.getId(), id)) {
                it.remove();
                break;
            }
        }
        return set;
    }

    public int deleteJournal(Long journalId, Long relevantUserId) {
        ClazzJournal journal = load(journalId);
        if (journal == null) {
            return 0;
        }
        String sql = "DELETE FROM VOX_CLAZZ_JOURNAL WHERE ID=? AND RELEVANT_USER_ID=?";
        int rows = utopiaSql.withSql(sql)
                .useParamsArgs(journalId, relevantUserId)
                .executeUpdate();
        if (rows > 0) {
            String key = ClazzJournal.cacheKeyFromId(journalId);
            getCache().delete(key);

            key = ClazzJournal.cacheKeyFromClazzId(journal.getClazzId());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                    currentValue -> removeFromSet(currentValue, journalId));

            key = ClazzJournal.cacheKeyFromUserId(journal.getRelevantUserId());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                    currentValue -> removeFromSet(currentValue, journalId));

            key = ClazzJournal.cacheKeyFromJournalType(journal.getJournalType());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                    currentValue -> removeFromSet(currentValue, journalId));
        }
        return rows;
    }

    public int delJournal(Long journalId) {
        ClazzJournal journal = load(journalId);
        if (journal == null) {
            return 0;
        }
        String updateSql = "DELETE FROM VOX_CLAZZ_JOURNAL WHERE ID=?";
        int rows = utopiaSql.withSql(updateSql)
                .useParamsArgs(journalId)
                .executeUpdate();
        if (rows > 0) {
            String key = ClazzJournal.cacheKeyFromId(journalId);
            getCache().delete(key);

            key = ClazzJournal.cacheKeyFromClazzId(journal.getClazzId());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                    currentValue -> removeFromSet(currentValue, journalId));

            key = ClazzJournal.cacheKeyFromUserId(journal.getRelevantUserId());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                    currentValue -> removeFromSet(currentValue, journalId));

            key = ClazzJournal.cacheKeyFromJournalType(journal.getJournalType());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(), 5,
                    currentValue -> removeFromSet(currentValue, journalId));
        }
        return rows;
    }

}
