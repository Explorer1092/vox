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

package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用于维护用户于今日班级空间中，被赞次数的缓存
 * 基于persistence
 * 继续使用前缀CLAZZ_SPACE_MOST_FAVORITE，保持向前兼容性
 */
@UtopiaCachePrefix(prefix = "CLAZZ_SPACE_MOST_FAVORITE")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class LikedCountCache extends PojoCacheObject<Long, String> {

    public LikedCountCache(UtopiaCache cache) {
        super(cache);
    }

    public void increase(Long userId) {
        String key = cacheKey(userId);
        cache.incr(key, 1, 1, expirationInSeconds());
    }

    /**
     * 获取用户今日被赞的次数
     *
     * @param userIds the user ids
     * @return users liked count
     */
    public Map<Long, Long> loadLikedCounts(Collection<Long> userIds) {
        userIds = CollectionUtils.toLinkedHashSet(userIds);
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> map = userIds.stream()
                .collect(Collectors.toMap(this::cacheKey, t -> t));
        return cache.loads(map.keySet()).entrySet().stream()
                .collect(Collectors.toMap(e -> map.get(e.getKey()), e -> SafeConverter.toLong(e.getValue())));
    }
}
