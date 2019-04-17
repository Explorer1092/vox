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
public class NoticeShareReportToJztCacheManager extends PojoCacheObject<String, Integer> {

    public NoticeShareReportToJztCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String hid) {
        return new NoticeShareReportToJztCacheManager.CacheKey(hid).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String hid;

        @Override
        public String toString() {
            return "NOTICE_SHARE_REPORT_TO_JZT_" + hid;
        }
    }

}
