package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 *
 * 缓存期末复习关卡学生的家长奖励
 * @author songtao
 * @since 2017/11/30
 */
public class AfentiReviewParrentRewardCacheManager extends CacheBaseManager<AfentiReviewParrentRewardCacheManager.GeneratorKey, Set<String>, String> {

    public AfentiReviewParrentRewardCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean addRecord(Long studentId, String unitId) {
        String cacheKey = cacheKey(new AfentiReviewParrentRewardCacheManager.GeneratorKey(studentId));
        return casAddSet(cacheKey, unitId);
    }

    public Set<String> loadRecord(Long studentId) {
        String cacheKey = cacheKey(new AfentiReviewParrentRewardCacheManager.GeneratorKey(studentId));
        return getCache().load(cacheKey);
    }

    public boolean containRecord(Long studentId, String unitId) {
        String cacheKey = cacheKey(new AfentiReviewParrentRewardCacheManager.GeneratorKey(studentId));
        CacheObject<Set<String>> cache = getCache().get(cacheKey);
        return cache != null  && CollectionUtils.isNotEmpty(cache.getValue()) && cache.getValue().contains(unitId);
    }

    @Override
    public int expirationInSeconds() {
        return 86400 * 30 * 2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"studentId"})
    public class GeneratorKey {
        private Long studentId;

        @Override
        public String toString() {
            return "SID=" + studentId;
        }
    }
}
