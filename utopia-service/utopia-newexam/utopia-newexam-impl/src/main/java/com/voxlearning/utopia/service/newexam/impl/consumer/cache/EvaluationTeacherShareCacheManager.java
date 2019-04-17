package com.voxlearning.utopia.service.newexam.impl.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;



@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180303")
public class EvaluationTeacherShareCacheManager extends PojoCacheObject<String, Integer> {
    public EvaluationTeacherShareCacheManager(UtopiaCache cache) {
        super(cache);
    }
    public String getCacheKey(String examId) {
        return new EvaluationTeacherShareCacheManager.CacheKey(examId).toString();
    }
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String examId;

        @Override
        public String toString() {
            return "EVALUATION_REPORT_TEACHER_SHARE_" + examId;
        }
    }
}
