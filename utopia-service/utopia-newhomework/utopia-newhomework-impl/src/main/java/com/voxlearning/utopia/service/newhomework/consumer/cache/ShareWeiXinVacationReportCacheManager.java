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
public class ShareWeiXinVacationReportCacheManager extends PojoCacheObject<String, Integer> {
    public ShareWeiXinVacationReportCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String packageId) {
        return new ShareWeiXinVacationReportCacheManager.CacheKey(packageId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String packageId;

        @Override
        public String toString() {
            return "SHARE_WEI_XIN_VACATION_" + packageId;
        }
    }

}
