package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 老师&学生查看亲子古诗活动缓存
 */
@UtopiaCacheRevision("20190227")
public class ViewPoetryActivityCacheManager extends PojoCacheObject<String, Integer> {

    public ViewPoetryActivityCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(Long userId, String activityId) {
        return new ViewPoetryActivityCacheManager.CacheKey(userId, activityId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        private Long userId;
        private String activityId;

        @Override
        public String toString() {
            String id = StringUtils.join("VIEW_POETRY_ACTIVITY_", userId);
            if (activityId != null) {
                id = StringUtils.join(id, "_", activityId);
            }
            return id;
        }
    }

    public boolean add(Long userId, String activityId) {
        if (userId == null || userId == 0) {
            return false;
        }
        String key = getCacheKey(userId, activityId);
        return cache.set(cacheKey(key), 0, 1);
    }

}
