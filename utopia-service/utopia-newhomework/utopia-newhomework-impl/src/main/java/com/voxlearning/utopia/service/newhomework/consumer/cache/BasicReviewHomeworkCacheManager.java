package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = UtopiaCacheExpiration.MAX_TTL_IN_SECONDS)
@UtopiaCacheRevision("20171110")
public class BasicReviewHomeworkCacheManager extends PojoCacheObject<String, BasicReviewHomeworkCacheMapper> {
    public BasicReviewHomeworkCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public String getCacheKey(Long studentId, String packageId) {
        return new CacheKey(studentId, packageId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public Long studentId;
        public String packageId;

        @Override
        public String toString() {
            return "BASIC_REVIEW_HOMEWORK_" + packageId + "_" + studentId;
        }
    }
}
