package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171221")
public class ShareVacationReportCacheManager extends PojoCacheObject<String, Integer> {
    public ShareVacationReportCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String packageId) {
        return new ShareVacationReportCacheManager.CacheKey(packageId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String packageId;

        @Override
        public String toString() {
            return "SHARE_VACATION_" + packageId;
        }
    }

}
