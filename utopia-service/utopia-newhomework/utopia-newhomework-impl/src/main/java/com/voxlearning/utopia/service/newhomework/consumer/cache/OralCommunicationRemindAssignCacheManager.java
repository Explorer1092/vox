package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;

import java.util.List;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 2 * 24 * 60 * 60)
@UtopiaCacheRevision("20190308")
public class OralCommunicationRemindAssignCacheManager extends PojoCacheObject<String, List<Long>> {
    public OralCommunicationRemindAssignCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(Long teacherId) {
        return new CacheKey(teacherId).toString();
    }

    @AllArgsConstructor
    public static class CacheKey {
        public Long teacherId;

        @Override
        public String toString() {
            return "ORAL_COMMUNICATION_REMIND_ASSIGN_" + teacherId;
        }
    }
}
