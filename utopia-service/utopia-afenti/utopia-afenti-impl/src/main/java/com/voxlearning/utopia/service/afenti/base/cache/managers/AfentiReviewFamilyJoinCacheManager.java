package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 缓存期末复习家庭参与值
 * @author songtao
 * @since 2017/11/30
 */
public class AfentiReviewFamilyJoinCacheManager extends PojoCacheObject<AfentiReviewFamilyJoinCacheManager.GeneratorKey, Integer> {

    public AfentiReviewFamilyJoinCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean addRecordOrIncreaseNumber(Long studentId, int number) {
        String cacheKey = cacheKey(new AfentiReviewFamilyJoinCacheManager.GeneratorKey(studentId));
        CacheObject<Integer> cacheObject = getCache().get(cacheKey);
        if (cacheObject == null) return false;
        if (cacheObject.getValue() == null
                && getCache().add(cacheKey, expirationInSeconds(), number)) {
            return true;
        }
        return getCache().cas(cacheKey, expirationInSeconds(), cacheObject,3, currentValue -> {
                    currentValue = currentValue + number;
                    return currentValue;
               });
    }

    public Integer loadRecord(Long studentId) {
        String cacheKey = cacheKey(new AfentiReviewFamilyJoinCacheManager.GeneratorKey(studentId));
        return getCache().load(cacheKey);
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
