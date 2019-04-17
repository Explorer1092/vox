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
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import org.springframework.dao.DataAccessException;

import javax.inject.Named;
import java.util.*;

/**
 * {@link GiftHistory} persistence implementation.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 13-9-3
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence")
@UtopiaCacheSupport(GiftHistory.class)
public class GiftHistoryPersistence extends StaticPersistence<Long, GiftHistory> {

    @Override
    protected void calculateCacheDimensions(GiftHistory source, Collection<String> dimensions) {
        dimensions.add(GiftHistory.ck_id(source.getId()));
        dimensions.add(GiftHistory.ck_senderId(source.getSenderId()));
        dimensions.add(GiftHistory.ck_receiverId(source.getReceiverId()));
    }

    @Override
    public Long persist(GiftHistory entity) throws DataAccessException {
        final Long id = persistIntoDatabase(entity);
        entity.setId(id);
        getCache().safeAdd(GiftHistory.ck_id(id), entityCacheExpirationInSeconds(), entity);
        getCache().getCacheObjectModifier().modify(
                GiftHistory.ck_senderId(entity.getSenderId()),
                entityCacheExpirationInSeconds(),
                new ChangeCacheObject<Collection<Long>>() {
                    @Override
                    public Collection<Long> changeCacheObject(Collection<Long> currentValue) {
                        List<Long> list = new LinkedList<>(currentValue);
                        list.add(0, id);
                        return new LinkedHashSet<>(list);
                    }
                });
        getCache().getCacheObjectModifier().modify(
                GiftHistory.ck_receiverId(entity.getReceiverId()),
                entityCacheExpirationInSeconds(),
                new ChangeCacheObject<Collection<Long>>() {
                    @Override
                    public Collection<Long> changeCacheObject(Collection<Long> currentValue) {
                        List<Long> list = new LinkedList<>(currentValue);
                        list.add(0, id);
                        return new LinkedHashSet<>(list);
                    }
                });
        return id;
    }

    @UtopiaCacheable
    public Collection<Long> queryIdsBySenderId(@UtopiaCacheKey(name = "S") Long senderId) {
        String sql = "SELECT ID FROM VOX_GIFT_HISTORY WHERE SENDER_ID=? ORDER BY CREATE_DATETIME DESC";
        return new LinkedHashSet<>(utopiaSql.withSql(sql).useParamsArgs(senderId).queryColumnValues(Long.class));
    }

    @UtopiaCacheable
    public Collection<Long> queryIdsByReceiverId(@UtopiaCacheKey(name = "R") Long receiverId) {
        String sql = "SELECT ID FROM VOX_GIFT_HISTORY WHERE RECEIVER_ID=? ORDER BY CREATE_DATETIME DESC";
        return new LinkedHashSet<>(utopiaSql.withSql(sql).useParamsArgs(receiverId).queryColumnValues(Long.class));
    }

    public boolean delete(final Long id) {
        GiftHistory history = loadFromDatabase(id);
        String sql = "DELETE FROM VOX_GIFT_HISTORY WHERE ID=?";
        int rows = utopiaSql.withSql(sql).useParamsArgs(id).executeUpdate();
        if (rows > 0) {
            getCache().delete(GiftHistory.ck_id(id));
            getCache().getCacheObjectModifier().modify(
                    GiftHistory.ck_senderId(history.getSenderId()),
                    entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<Collection<Long>>() {
                        @Override
                        public Collection<Long> changeCacheObject(Collection<Long> currentValue) {
                            LinkedHashSet<Long> set = new LinkedHashSet<>(currentValue);
                            set.remove(id);
                            return set;
                        }
                    });
            getCache().getCacheObjectModifier().modify(
                    GiftHistory.ck_receiverId(history.getReceiverId()),
                    entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<Collection<Long>>() {
                        @Override
                        public Collection<Long> changeCacheObject(Collection<Long> currentValue) {
                            LinkedHashSet<Long> set = new LinkedHashSet<>(currentValue);
                            set.remove(id);
                            return set;
                        }
                    });
        }
        return rows > 0;
    }

    public int updateLatestReply(Long id, String reply) {
        final Date current = new Date();
        final String text = StringHelper.filterEmojiForMysql(reply);
        int rows = withUpdateTable("SET UPDATE_DATETIME=?,LATEST_REPLY=? WHERE ID=?")
                .useParamsArgs(current, text, id).executeUpdate();
        if (rows > 0) {
            getCache().getCacheObjectModifier().modify(
                    GiftHistory.ck_id(id),
                    entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<GiftHistory>() {
                        @Override
                        public GiftHistory changeCacheObject(GiftHistory currentValue) {
                            currentValue.setUpdateDatetime(current);
                            currentValue.setLatestReply(text);
                            return currentValue;
                        }
                    });
        }
        return rows;
    }

    public int updateThanks(Long id) {
        final Date current = new Date();
        int rows = withUpdateTable("SET UPDATE_DATETIME=?,IS_THANKS=TRUE WHERE ID=?")
                .useParamsArgs(current, id).executeUpdate();
        if (rows > 0) {
            getCache().getCacheObjectModifier().modify(
                    GiftHistory.ck_id(id),
                    entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<GiftHistory>() {
                        @Override
                        public GiftHistory changeCacheObject(GiftHistory currentValue) {
                            currentValue.setUpdateDatetime(current);
                            currentValue.setIsThanks(true);
                            return currentValue;
                        }
                    });
        }
        return rows;
    }
}
