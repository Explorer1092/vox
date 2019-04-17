package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 错题宝视频课程观看记录
 *
 * @author liu jingchao
 * @since 2017/7/26
 */
@UtopiaCachePrefix(prefix = "AFENTI_VIDEO_COURSE_VIEW_RECORD")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 365)
public class AfentiVideoCourseViewRecordCacheManager extends PojoCacheObject<AfentiVideoCourseViewRecordCacheManager.GeneratorKey, Set<String>> {

    public AfentiVideoCourseViewRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addViewRecord(Long studentId, String videoId) {
        if (studentId == null || StringUtils.isBlank(videoId))
            return;

        String key = cacheKey(new AfentiVideoCourseViewRecordCacheManager.GeneratorKey(studentId));
        CacheObject<Set<String>> cacheObject = getCache().get(key);
        if (null != cacheObject.getValue()) {
            cache.cas(key, expirationInSeconds(), cacheObject, 3, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(videoId);
                return currentValue;
            });
        } else {
            Set<String> videoIdsSet = new HashSet<>();
            videoIdsSet.add(videoId);
            cache.add(key, expirationInSeconds(), videoIdsSet);
        }

    }

    public Boolean isAddedViewRecord(Long studentId, String videoId) {
        if (studentId == null || StringUtils.isBlank(videoId))
            return true;

        String key = cacheKey(new AfentiVideoCourseViewRecordCacheManager.GeneratorKey(studentId));
        Set<String> videoIdsSet = getCache().load(key);
        if (CollectionUtils.isEmpty(videoIdsSet) || !videoIdsSet.contains(videoId))
            return false;
        else
            return true;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"studentId"})
    public class GeneratorKey {
        private Long studentId;

        @Override
        public String toString() {
            return "SID=" + studentId;
        }
    }

}
