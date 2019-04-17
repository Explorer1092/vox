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
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanRankResult;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Persistence implementation of {@code AfentiLearningPlanRankResult} data structure.
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @since 2013-09-09 12:15AM
 */
@Named
@UtopiaCacheSupport(value = AfentiLearningPlanRankResult.class, cacheSystem = CacheSystem.CBS)
public class AfentiLearningPlanRankResultPersistence extends StaticPersistence<Long, AfentiLearningPlanRankResult> {

    @Override
    protected void calculateCacheDimensions(AfentiLearningPlanRankResult source, Collection<String> dimensions) {
        dimensions.add(AfentiLearningPlanRankResult.ck_uid_nbid(source.getUserId(), source.getNewBookId()));
    }

    @Override
    public Long persist(AfentiLearningPlanRankResult entity) {
        Long id = persistIntoDatabase(entity);
        entity.setId(id);
        String key = AfentiLearningPlanRankResult.ck_uid_nbid(entity.getUserId(), entity.getNewBookId());
        getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                currentValue -> CollectionUtils.addToList(currentValue, entity));
        return id;
    }

    @UtopiaCacheable
    public List<AfentiLearningPlanRankResult> findByUserIdAndNewBookId(@UtopiaCacheKey(name = "UID") final Long userId,
                                                                       @UtopiaCacheKey(name = "NBID") final String newBookId) {

        if (userId == null || newBookId == null) return Collections.emptyList();
        return withSelectFromTable("WHERE USER_ID=? AND NEW_BOOK_ID=? AND PLAN_TYPE='BASE'")
                .useParamsArgs(userId, newBookId).queryAll();
    }

    public boolean update(AfentiLearningPlanRankResult result) {
        String update = "SET UPDATETIME=?,ERROR_BOOK_KEY_NUM=?,RIGHT_RATE=?,MAX_STAR_NUM=? WHERE ID=? ";
        int rows = withUpdateTable(update)
                .useParamsArgs(result.getUpdateTime(), result.getErrorBookKey(),
                        result.getRightRate(), result.getMaxStarNum(), result.getId())
                .executeUpdate();
        if (rows > 0) {
            String key = AfentiLearningPlanRankResult.ck_uid_nbid(result.getUserId(), result.getNewBookId());
            getCache().getCacheObjectModifier().modify(key, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<List<AfentiLearningPlanRankResult>>() {
                        @Override
                        public List<AfentiLearningPlanRankResult> changeCacheObject(List<AfentiLearningPlanRankResult> currentValue) {
                            currentValue = new LinkedList<>(currentValue);
                            Map<Long, AfentiLearningPlanRankResult> map = currentValue.stream()
                                    .collect(Collectors.toMap(AfentiLearningPlanRankResult::getId, Function.identity()));
                            map.put(result.getId(), result);
                            return new LinkedList<>(map.values());
                        }
                    });
        }
        return rows > 0;
    }

    // 到数据用的，新阿分题上线一段时间后
    public List<AfentiLearningPlanRankResult> findByUserId(Long userId) {
        if (userId == null) return Collections.emptyList();
        return withSelectFromTable("WHERE USER_ID=?").useParamsArgs(userId).queryAll();
    }
}
