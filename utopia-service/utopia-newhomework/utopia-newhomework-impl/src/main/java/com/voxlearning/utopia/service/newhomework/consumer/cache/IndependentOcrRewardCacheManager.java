package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 独立拍照发放奖励缓存
 */
@UtopiaCacheRevision("20190326")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class IndependentOcrRewardCacheManager extends PojoCacheObject<String, Integer> {

    public IndependentOcrRewardCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(Long userId) {
        return new IndependentOcrRewardCacheManager.CacheKey(userId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        private Long userId;

        @Override
        public String toString() {
            return StringUtils.join("INDEPENDENT_OCR_REWARD_", userId);
        }
    }

    public boolean exist(Long userId) {
        if (userId == null || userId == 0) {
            return false;
        }
        return load(getCacheKey(userId)) != null;
    }

}
