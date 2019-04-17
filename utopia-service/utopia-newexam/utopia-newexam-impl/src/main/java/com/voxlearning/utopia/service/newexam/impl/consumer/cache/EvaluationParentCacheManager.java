package com.voxlearning.utopia.service.newexam.impl.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 30 * 24 * 60 * 60)
@UtopiaCacheRevision("20180303")
public class EvaluationParentCacheManager extends PojoCacheObject<String, Set<Long>> {
    public EvaluationParentCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String examId) {
        return new CacheKey(examId).toString();
    }
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String examId;

        @Override
        public String toString() {
            return "EVALUATION_REPORT_JZT_PARENT_" + examId;
        }
    }
}
