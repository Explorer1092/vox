package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author guangqing
 * @since 2018/7/31
 */
public class AiLoaderRedDotCacheManager extends PojoCacheObject<String, String> {
    AiLoaderRedDotCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Boolean save(Long userId, String value) {
        String cacheKey = genRedDotKey(userId);
        int expire = calCacheExpireTime();
        return getCache().set(cacheKey, expire, value);
    }

    public String read(Long userId) {
        String cacheKey = genRedDotKey(userId);
        return getCache().load(cacheKey);
    }

    public Boolean delete(Long userId) {
        String cacheKey = genRedDotKey(userId);
        return getCache().delete(cacheKey);
    }

    private String genRedDotKey(Long userId) {
        return "AiLoaderRedDotCacheManager_RedDot_UID=" + userId;
    }

    /**
     *  单位 s
     */
    private int calCacheExpireTime() {
        return DateUtils.getCurrentToDayEndSecond();
    }
}
