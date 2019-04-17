package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 14 * 24 * 60 * 60)
@UtopiaCacheRevision("20170502")
public class WeekReportCacheManager extends PojoCacheObject<String, Set<Long>> {

    public WeekReportCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String weekReportId) {
        return new CacheKey(weekReportId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String weekReportId;

        @Override
        public String toString() {
            return "WEEK_REPORT_JZT_PARENT" + weekReportId;
        }
    }

}
