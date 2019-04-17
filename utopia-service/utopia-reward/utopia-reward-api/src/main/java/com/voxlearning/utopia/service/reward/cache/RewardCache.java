package com.voxlearning.utopia.service.reward.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardCache {
    private static final Logger logger = LoggerFactory.getLogger(RewardCache.class);

    public static UtopiaCache getRewardCache() {
        return RewardCacheHolder.rewardCache;
    }

    public static UtopiaCache getPersistent(){
        return RewardCacheHolder.persistentCache;
    }

    private static class RewardCacheHolder {
        private static final UtopiaCache rewardCache;
        private static final UtopiaCache persistentCache;

        static {
            UtopiaCache cache = CacheSystem.CBS.getCache("flushable");
            if (cache == null) {
                logger.warn("Reward cache CBS/flushable not found, use NOP instead.");
                CacheSystem.NOP.getCache("");
            }

            rewardCache = cache;
            persistentCache = CacheSystem.CBS.getCache("persistence");
        }
    }
}
