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

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.KeyGenerator;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementStatus;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiUserAchievementRecord;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.zhang.a
 * @since 16-7-25
 */
@Named
@UtopiaCacheSupport(value = AfentiUserAchievementRecord.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class AfentiUserAchievementRecordPersistence extends StaticPersistence<Long, AfentiUserAchievementRecord> {
    @Override
    protected void calculateCacheDimensions(AfentiUserAchievementRecord source, Collection<String> dimensions) {
        dimensions.add(AfentiUserAchievementRecord.ck_id(source.getId()));
        dimensions.add(AfentiUserAchievementRecord.ck_us(source.getUserId(), source.getSubject()));
    }

    @UtopiaCacheable
    public List<AfentiUserAchievementRecord> find(@UtopiaCacheKey(name = "UID") final Long userId,
                                                  @UtopiaCacheKey(name = "SJ") final Subject subject) {
        return withSelectFromTable("WHERE USER_ID=? AND SUBJECT=? ORDER BY CREATE_DATETIME DESC").useParamsArgs(userId, subject).queryAll();
    }

    public Map<Long, List<AfentiUserAchievementRecord>> findByUserIds(Set<Long> userIds, Subject subject) {
        CacheObjectLoader.Loader<Long, List<AfentiUserAchievementRecord>> loader = getCache()
                .getCacheObjectLoader().createLoader(new KeyGenerator<Long>() {
                    @Override
                    public String generate(Long source) {
                        return AfentiUserAchievementRecord.ck_us(source, subject);
                    }
                });
        return loader.loads(userIds)
                .loadsMissed(missedSources -> findByUserIds(missedSources, subject))
                .writeAsList(entityCacheExpirationInSeconds())
                .getResult();
    }

    /**
     * 默认为0
     */
    public Map<Long, Integer> findMaxLevelByUserIds(Set<Long> userIds, Subject subject, AchievementType achievementType) {
        Map<Long, Integer> maxLevelMap = new HashMap<>();


        findByUserIds(userIds, subject).forEach((userId, records) -> {
            int maxLevel = records.stream()
                    .filter(p -> p.getAchievementType() == achievementType)
                    .mapToInt(AfentiUserAchievementRecord::getLevel)
                    .max()
                    .orElse(0);
            maxLevelMap.put(userId, maxLevel);
        });
        return maxLevelMap;
    }

    public boolean updateStatus(Long id, AchievementStatus status) {
        if (id == 0 || status == null) {
            return false;
        }
        String sql = "SET STATUS=?,UPDATE_DATETIME=? WHERE id=?";
        int changeNum = withUpdateTable(sql).useParamsArgs(status, new Date(), id).executeUpdate();
        if (changeNum > 0) {
            Collection<String> keys = calculateDimensions(id);
            getCache().delete(keys);
        }
        return changeNum > 0;
    }

    //提供批量查的方法
    private Map<Long, List<AfentiUserAchievementRecord>> findByUserIds(Collection<Long> userIds, Subject subject) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userIds", userIds);
        parameters.put("subject", subject);
        return withSelectFromTable("WHERE USER_ID IN (:userIds) AND SUBJECT=(:subject) ORDER BY CREATE_DATETIME DESC")
                .useParams(parameters)
                .queryAll()
                .stream()
                .collect(Collectors.groupingBy(AfentiUserAchievementRecord::getUserId, Collectors.toList()));
    }
}