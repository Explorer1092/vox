package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.mapper.OutsideReadingDynamicCacheMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 学生阅读成就打卡缓存
 * 缓存时间: 30天(60 * 60 * 24 * 30)
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 60 * 60 * 24 * 30)
@UtopiaCacheRevision("20181121")
public class OutsideReadingDynamicCacheManager extends PojoCacheObject<String, List<OutsideReadingDynamicCacheMapper>> {

    public OutsideReadingDynamicCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public String getCacheKey(String readingId) {
        return new CacheKey(readingId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        private String readingId;

        @Override
        public String toString() {
            return "OUTSIDE_READING_DYNAMIC_" + readingId;
        }
    }

    /**
     * 添加一条班级学生动态(一个readingId+studentId只存在一条记录)
     * @param readingId
     * @param mapper
     */
    public void addDynamic(String readingId, OutsideReadingDynamicCacheMapper mapper) {
        if (readingId == null || mapper == null) return;
        List<OutsideReadingDynamicCacheMapper> mappers = load(getCacheKey(readingId));
        if (CollectionUtils.isEmpty(mappers)) {
            mappers = new LinkedList<>();
        }
        if (!mappers.contains(mapper)) {
            mappers.add(0, mapper);
            if (mappers.size() > 60) {
                mappers = mappers.subList(0, 60);
            }
            set(getCacheKey(readingId), mappers);
        }
    }

    /**
     * 查询多个阅读任务动态
     * @param readingIds
     * @return
     */
    public List<OutsideReadingDynamicCacheMapper> loadDynamicByReadingIds(Collection<String> readingIds) {
        if (CollectionUtils.isEmpty(readingIds)) {
            return Collections.emptyList();
        }
        List<OutsideReadingDynamicCacheMapper> mappers = new LinkedList<>();
        readingIds.forEach(readingId -> {
            List<OutsideReadingDynamicCacheMapper> readingDynamics = load(getCacheKey(readingId));
            if (CollectionUtils.isNotEmpty(readingDynamics)) {
                mappers.addAll(readingDynamics);
            }
        });
        mappers.sort((o1, o2) -> o2.getFinishAt().compareTo(o1.getFinishAt()));
        return mappers;
    }
}
