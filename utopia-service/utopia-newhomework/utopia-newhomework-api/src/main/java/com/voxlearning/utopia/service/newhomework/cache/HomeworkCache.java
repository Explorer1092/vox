package com.voxlearning.utopia.service.newhomework.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import org.slf4j.Logger;

/**
 * @author xuesong.zhang
 * @since 2017/6/6
 */
public class HomeworkCache {
    private static final Logger logger = LoggerFactory.getLogger(HomeworkCache.class);

    public static UtopiaCache getHomeworkCacheFlushable() {
        return HomeworkCacheHolder.homeworkCacheFlushable;
    }

    public static UtopiaCache getHomeworkCacheUnflushable() {
        return HomeworkCacheHolder.homeworkCacheUnflushable;
    }

    public static UtopiaCache getHomeworkCachePersistence() {
        return HomeworkCacheHolder.homeworkCachePersistence;
    }

    public static UtopiaCache getHomeworkCacheStorage() {
        return HomeworkCacheHolder.homeworkCacheStorage;
    }

    public static UtopiaCache getHomeworkCache() {
        return HomeworkCacheHolder.homeworkCache;
    }

    private static class HomeworkCacheHolder {
        private static final UtopiaCache homeworkCacheFlushable;
        private static final UtopiaCache homeworkCacheUnflushable;
        private static final UtopiaCache homeworkCachePersistence;
        private static final UtopiaCache homeworkCacheStorage;
        private static final UtopiaCache homeworkCache;

        static {
            homeworkCacheFlushable = CacheSystem.CBS.getCache("flushable");
            if (homeworkCacheFlushable == null) logger.error("HomeworkCache flushable is null");

            homeworkCacheUnflushable = CacheSystem.CBS.getCache("unflushable");
            if (homeworkCacheUnflushable == null) logger.error("HomeworkCache unflushable is null");

            homeworkCachePersistence = CacheSystem.CBS.getCache("persistence");
            if (homeworkCachePersistence == null) logger.error("HomeworkCache persistence is null");

            homeworkCacheStorage = CacheSystem.CBS.getCache("storage");
            if (homeworkCacheStorage == null) logger.error("HomeworkCache storage is null");

            homeworkCache = CacheSystem.CBS.getCache("utopia-homework-cache");
            if (homeworkCache == null) logger.error("HomeworkCache utopia-homework-cache is null");
        }
    }
}
