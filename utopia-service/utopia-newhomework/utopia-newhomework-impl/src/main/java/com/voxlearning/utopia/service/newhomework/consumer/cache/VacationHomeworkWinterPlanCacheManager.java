package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author tanguohong
 * @since 2016/12/5
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = UtopiaCacheExpiration.MAX_TTL_IN_SECONDS)
@UtopiaCacheRevision("20181210")
public class VacationHomeworkWinterPlanCacheManager extends PojoCacheObject<String, VacationHomeworkWinterPlanCacheMapper> {


    public VacationHomeworkWinterPlanCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String bookId) {
        return new VacationHomeworkWinterPlanCacheManager.CacheKey(bookId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String bookId;

        @Override
        public String toString() {
            return "WINTER_VACATION_HOMEWORK_PLAN_" + bookId;
        }
    }
}


