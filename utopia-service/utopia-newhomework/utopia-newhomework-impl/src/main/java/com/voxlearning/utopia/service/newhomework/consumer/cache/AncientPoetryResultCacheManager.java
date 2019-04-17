package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author xuesong.zhang
 * @since 2016/12/1
 */
@UtopiaCacheRevision("20190221")
public class AncientPoetryResultCacheManager extends PojoCacheObject<String, AncientPoetryResultCacheMapper> {

    public AncientPoetryResultCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String activityId, Long studentId) {
        return new CacheKey(activityId, studentId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String activityId;
        public Long studentId;

        @Override
        public String toString() {
            return "ANCIENT_POETRY_RESULT_" + activityId + "_" + studentId;
        }
    }


    public Boolean addPoetryResult(String key, AncientPoetryResultCacheMapper mapper) {
        if (key == null || mapper == null) {
            return false;
        }
        return cache.set(cacheKey(key), 0, mapper);
    }

}
