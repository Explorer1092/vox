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

package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/2/1
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class CheckHomeworkIntegralCacheManager extends PojoCacheObject<CheckHomeworkIntegralCacheManager.ComplexId, Set<CheckHomeworkIntegralDetail>> {

    public CheckHomeworkIntegralCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long teacherId, Long groupId, HomeworkType type, CheckHomeworkIntegralDetail detail) {
        if (teacherId == null || groupId == null || type == null || detail == null) return;
        String key = cacheKey(new ComplexId(teacherId, groupId, type));
        CacheObject<Set<CheckHomeworkIntegralDetail>> cacheObject = cache.get(key);
        if (cacheObject == null) return;
        if (cacheObject.getValue() == null) {
            cache.add(key, expirationInSeconds(), Collections.singleton(detail));
        } else {
            cache.cas(key, expirationInSeconds(), cacheObject, 3, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(detail);
                return currentValue;
            });
        }
    }

    public int weekCheckTime(Long teacherId, Long groupId, HomeworkType type) {
        if (teacherId == null || groupId == null || type == null) return 0;
        String key = cacheKey(new ComplexId(teacherId, groupId, type));
        CacheObject<Set<CheckHomeworkIntegralDetail>> cacheObject = cache.get(key);
        return cacheObject == null || cacheObject.getValue() == null ? 0 : cacheObject.getValue().size();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplexId {
        public Long teacherId;
        public Long groupId;
        public HomeworkType homeworkType;

        @Override
        public String toString() {
            return "TID=" + teacherId + ",GID=" + groupId + ",HT=" + homeworkType.name();
        }
    }
}
