package com.voxlearning.utopia.service.dubbing.impl.support;

import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author shiwei.liao
 * @since 2017-8-24
 */
@Slf4j
@UtopiaCachePrefix(prefix = "DubbingPersistenceCache")
@UtopiaCacheRevision("20171026")
public class DubbingPersistenceCache extends PojoCacheObject<String, Long> {
    public DubbingPersistenceCache(UtopiaCache cache) {
        super(cache);
    }

    public Long incrDubbingUserCount(String categoryId) {
        if (StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        String cacheKey = cacheKey(generateDubbingUserCountKey(categoryId));
        return cache.incr(cacheKey, 1, 1, 0);
    }

    public Long loadDubbingUserCount(String categoryId) {
        if (StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        String cacheKey = cacheKey(generateDubbingUserCountKey(categoryId));
        return SafeConverter.toLong(cache.load(cacheKey));
    }

    public void addWeekRank(int week, String dubbingId, String historyId) {
        if (StringUtils.isBlank(historyId) || StringUtils.isBlank(dubbingId) || week < 1) {
            return;
        }
        String cacheKey = cacheKey(generateDubbingRankKey(week, dubbingId));

        CacheObject<Object> cacheObject = getCache().get(cacheKey);
        if (cacheObject == null || cacheObject.getValue() == null) {
            getCache().add(cacheKey, 3600 * 24 * 16, Collections.singleton(historyId));
        } else {
            ChangeCacheObject<Set<String>> modifier = currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(historyId);
                return currentValue;
            };
            CacheValueModifierExecutor<Set<String>> executor = cache.createCacheValueModifier();
            executor.key(cacheKey)
                    .expiration(3600 * 24 * 16)
                    .modifier(modifier)
                    .execute();
        }

    }

    public Boolean addDubbingUserCountTemp(String categoryId, Long count) {
        if (StringUtils.isBlank(categoryId)) {
            return Boolean.FALSE;
        }
        String cacheKey = cacheKey(generateDubbingUserCountKey(categoryId));
        cache.delete(cacheKey);
        return cache.incr(cacheKey, count, count, 0) > 0;
    }

    @SuppressWarnings("unchecked")
    public Set<String> loadWeekRank(int week, String dubbingId) {
        if (StringUtils.isBlank(dubbingId) || week < 1) {
            return Collections.emptySet();
        }
        String cacheKey = cacheKey(generateDubbingRankKey(week, dubbingId));
        Object o = cache.load(cacheKey);
        if (o == null) {
            return Collections.emptySet();
        }
        return (Set) o;
    }

    public Long addUserDubbingCountInCategory(Long userId, String categoryId) {
        if (userId == null || StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        String cacheKey = cacheKey(generateUserDubbingCountInCategoryCacheKey(userId, categoryId));
        return cache.incr(cacheKey, 1, 1, 0);
    }

    public Long loadUserDubbingCountInCategory(Long userId, String categoryId) {
        if (userId == null || StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        String cacheKey = cacheKey(generateUserDubbingCountInCategoryCacheKey(userId, categoryId));
        return SafeConverter.toLong(cache.load(cacheKey));
    }

    public Long decrUserDubbingCountInCategory(Long userId, String categoryId) {
        if (userId == null || StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        String cacheKey = cacheKey(generateUserDubbingCountInCategoryCacheKey(userId, categoryId));
        return cache.decr(cacheKey, 1, 0, 0);
    }

    private String generateDubbingUserCountKey(String categoryId) {
        Objects.requireNonNull(categoryId);
        return categoryId;
    }

    private String generateDubbingRankKey(int week, String dubbingId) {
        return "dubbing_rank_" + week + "_" + dubbingId;
    }

    private String generateUserDubbingCountInCategoryCacheKey(Long userId, String categoryId) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(categoryId);
        return userId + "_" + categoryId;
    }
}
