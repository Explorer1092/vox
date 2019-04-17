package com.voxlearning.utopia.service.afenti.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;

public class AfentiCache {

    public static UtopiaCache getAfentiCache() {
        return AfentiCacheHolder.afentiCache;
    }

    public static UtopiaCache getPersistent(){
        return AfentiCacheHolder.persistent;
    }

    private static class AfentiCacheHolder {
        private static final UtopiaCache afentiCache;
        private static final UtopiaCache persistent;

        static {
            afentiCache = CacheSystem.CBS.getCache("flushable");
            persistent = CacheSystem.CBS.getCache("persistence");
        }
    }
}
