package com.voxlearning.utopia.service.parent.homework.impl.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.NonNull;

public class HomeWorkCache {
    private static final String PARENT_HOMEWORK_KEY_PREFIX = "PARENT_HOMEWORK_";
    private static final String version = "20190111";
    private static UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("flushable");
    private static int defaultSeconds = 24 * 60 * 60;
    public static <T> T load(@NonNull CacheKey cacheKey, Object... keys) {
        return cache.load(getGeneralKey(cacheKey, keys));
    }

    public static <T> void set(int seconds, T value, @NonNull CacheKey cacheKey, Object... keys) {
        cache.set(getGeneralKey(cacheKey, keys) , seconds, value);
    }

    public static <T> void set(T value, @NonNull CacheKey cacheKey, Object... keys) {
        cache.set(getGeneralKey(cacheKey, keys) , defaultSeconds, value);
    }

    public static void delete(@NonNull CacheKey cacheKey, Object... keys) {
        cache.delete(getGeneralKey(cacheKey, keys));
    }

    private static String getGeneralKey(CacheKey cacheKey, Object... keys) {
        return PARENT_HOMEWORK_KEY_PREFIX + cacheKey.name() + "_" + version + "." + StringUtils.join(keys, ".");
    }
}
