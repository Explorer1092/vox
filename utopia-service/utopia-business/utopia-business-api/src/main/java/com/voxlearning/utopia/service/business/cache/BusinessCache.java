package com.voxlearning.utopia.service.business.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import org.slf4j.Logger;

public class BusinessCache {
    private static final Logger logger = LoggerFactory.getLogger(BusinessCache.class);

    public static UtopiaCache getBusinessCache() {
        return BusinessCacheHolder.businessCache;
    }

    private static class BusinessCacheHolder {
        private static final UtopiaCache businessCache;

        static {
            UtopiaCache cache = CacheSystem.CBS.getCache("flushable");
            if (cache == null) {
                logger.warn("Business cache CBS/flushable not found, use NOP.");
                cache = CacheSystem.NOP.getCache("");
            }
            businessCache = cache;
        }
    }
}
