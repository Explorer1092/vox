package com.voxlearning.washington.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author feng.guo
 * @since 2019-01-31
 */
public class HomeworkCacheManager {

    private static final String HOME_WORK_EXECUTE = "HOME_WORK_EXECUTE_";

    private static class HomeworkCacheHolder {
        private static final UtopiaCache persistenceCache;

        static {
            persistenceCache = CacheSystem.CBS.getCache("persistence");
        }
    }

    public static UtopiaCache getPersistence() {
        return HomeworkCacheHolder.persistenceCache;
    }

    public static void setHomeWorkCache(Long sid, String bizType) {
        if (StringUtils.isBlank(bizType) || null == sid) {
            return;
        }
        getPersistence().set(HOME_WORK_EXECUTE + bizType + "_" + sid, 365 * 24 * 60 * 60, true);
    }

    public static boolean getHomeWorkCache(Long sid, String bizType) {
        if (StringUtils.isBlank(bizType) || null == sid) {
            return false;
        }
        CacheObject<Object> cacheObject = getPersistence().get(HOME_WORK_EXECUTE + bizType + "_" + sid);
        if (null == cacheObject || null == cacheObject.getValue()) {
            return false;
        }
        return SafeConverter.toBoolean(cacheObject.getValue(), false);
    }
}
