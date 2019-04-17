package com.voxlearning.utopia.service.nekketsu.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;

public class NekketsuCache {

    public static UtopiaCache getNekketsuCache() {
        return NekketsuCacheHolder.cache;
    }

    private static class NekketsuCacheHolder {
        private static final UtopiaCache cache;

        static {
            cache = CacheSystem.CBS.getCache("flushable");
        }
    }
}
