package com.voxlearning.utopia.service.crm.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;

public class CrmCache {

    public static UtopiaCache getCrmCache() {
        return CrmCacheHolder.crmCache;
    }

    private static class CrmCacheHolder {
        private static final UtopiaCache crmCache;

        static {
            crmCache = CacheSystem.CBS.getCache("flushable");
        }
    }
}
