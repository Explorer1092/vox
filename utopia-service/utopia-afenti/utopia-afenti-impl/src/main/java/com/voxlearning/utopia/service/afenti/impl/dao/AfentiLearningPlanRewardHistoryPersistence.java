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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanRewardHistory;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Persistence implementation of {@code AfentiLearningPlanRewardHistory} data structure.
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @since 2013-09-09 00:02
 */
@Named
@UtopiaCacheSupport(value = AfentiLearningPlanRewardHistory.class, cacheSystem = CacheSystem.CBS)
public class AfentiLearningPlanRewardHistoryPersistence extends StaticPersistence<Long, AfentiLearningPlanRewardHistory> {

    @Override
    protected void calculateCacheDimensions(AfentiLearningPlanRewardHistory source, Collection<String> dimensions) {
        dimensions.add(AfentiLearningPlanRewardHistory.ck_uid_nbid(source.getUserId(), source.getNewBookId()));
    }

    @Override
    public Long persist(AfentiLearningPlanRewardHistory entity) {
        Long id = persistIntoDatabase(entity);
        entity.setId(id);
        String key = AfentiLearningPlanRewardHistory.ck_uid_nbid(entity.getUserId(), entity.getNewBookId());
        getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                currentValue -> CollectionUtils.addToList(currentValue, entity));
        return id;
    }

    @UtopiaCacheable
    public List<AfentiLearningPlanRewardHistory> findByUserIdAndNewBookId(@UtopiaCacheKey(name = "UID") final Long userId,
                                                                          @UtopiaCacheKey(name = "NBID") final String newBookId) {
        if (userId == null || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return withSelectFromTable("WHERE USER_ID=? AND NEW_BOOK_ID=? AND PLAN_TYPE='BASE'")
                .useParamsArgs(userId, newBookId).queryAll();
    }

    public boolean update(AfentiLearningPlanRewardHistory history) {
        String update = "SET RECEIVED=?,UPDATETIME=? WHERE ID=?";
        int rows = withUpdateTable(update).useParamsArgs(history.getReceived(), history.getUpdateTime(), history.getId()).executeUpdate();
        if (rows > 0) {
            String key = AfentiLearningPlanRewardHistory.ck_uid_nbid(history.getUserId(), history.getNewBookId());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<List<AfentiLearningPlanRewardHistory>>() {
                        @Override
                        public List<AfentiLearningPlanRewardHistory> changeCacheObject(List<AfentiLearningPlanRewardHistory> currentValue) {
                            currentValue = new LinkedList<>(currentValue);
                            Map<Long, AfentiLearningPlanRewardHistory> map = currentValue.stream()
                                    .collect(Collectors.toMap(AfentiLearningPlanRewardHistory::getId, Function.identity()));
                            map.put(history.getId(), history);
                            return new ArrayList<>(map.values());
                        }
                    });
        }
        return rows > 0;
    }

    // 到数据用的，新阿分题上线一段时间后
    public List<AfentiLearningPlanRewardHistory> findByUserId(Long userId) {
        if (userId == null) return Collections.emptyList();
        return withSelectFromTable("WHERE USER_ID=?").useParamsArgs(userId).queryAll();
    }
}
