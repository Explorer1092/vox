package com.voxlearning.utopia.service.piclisten.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;

public class PiclistenCache {

    public static UtopiaCache getPiclistenCache() {
        return VendorCacheHolder.piclistenCache;
    }

    public static UtopiaCache getPersistenceCache() {
        return VendorCacheHolder.persistenceCache;
    }

    private static class VendorCacheHolder {
        private static final UtopiaCache piclistenCache;
        private static final UtopiaCache persistenceCache;

        static {
            piclistenCache = CacheSystem.CBS.getCache("flushable");
            persistenceCache = CacheSystem.CBS.getCache("persistence");
        }
    }
}
