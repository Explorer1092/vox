package com.voxlearning.utopia.service.zone.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * Zone Cache
 * Created by alex on 2017/3/7.
 */
public class ZoneCache {

    public static UtopiaCache getCache() {
        return ZoneCacheHolder.zoneCache;
    }

    private static class ZoneCacheHolder {
        private static final UtopiaCache zoneCache;

        static {
            zoneCache = CacheSystem.CBS.getCache("flushable");
        }
    }

    private static String getHomeworkAvgScoreCacheKey(String homeworkId) {
        return "clazz:record:hw:score:avg:" + homeworkId;
    }

    public static void saveHomeworkAvgScore(String homeworkId, Double avgScore) {
        getCache().set(getHomeworkAvgScoreCacheKey(homeworkId), 1800, avgScore);
    }

    public static Double loadHomeworkAvgScore(String homeworkId) {
        return getCache().load(getHomeworkAvgScoreCacheKey(homeworkId));
    }

}
