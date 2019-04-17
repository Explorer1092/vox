package com.voxlearning.utopia.service.vendor.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;

public class VendorCache {

    public static UtopiaCache getVendorCache() {
        return VendorCacheHolder.vendorCache;
    }

    public static UtopiaCache getVendorPersistenceCache() {
        return VendorCacheHolder.vendorPersistenceCache;
    }

    private static class VendorCacheHolder {
        private static final UtopiaCache vendorCache;
        private static final UtopiaCache vendorPersistenceCache;

        static {
            vendorCache = CacheSystem.CBS.getCache("flushable");
            vendorPersistenceCache = CacheSystem.CBS.getCache("persistence");
        }
    }
}
