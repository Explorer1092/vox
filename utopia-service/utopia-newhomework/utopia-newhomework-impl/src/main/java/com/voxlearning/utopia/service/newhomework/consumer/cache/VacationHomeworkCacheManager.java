package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author xuesong.zhang
 * @since 2016/12/1
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = UtopiaCacheExpiration.MAX_TTL_IN_SECONDS)
@UtopiaCacheRevision("20181210")
public class VacationHomeworkCacheManager extends PojoCacheObject<String, VacationHomeworkCacheMapper> {
    public VacationHomeworkCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(Long clazzGroupId, Long studentId) {
        return new CacheKey(clazzGroupId, studentId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public Long clazzGroupId;
        public Long studentId;

        @Override
        public String toString() {
            return "WINTER_VACATION_HOMEWORK_" + clazzGroupId + "_" + studentId;
        }
    }

}
