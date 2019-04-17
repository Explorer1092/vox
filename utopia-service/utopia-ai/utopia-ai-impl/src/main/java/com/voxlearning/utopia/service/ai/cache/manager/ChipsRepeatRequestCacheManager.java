package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 重复请求过滤器
 */
public class ChipsRepeatRequestCacheManager extends PojoCacheObject<String, String> {
    ChipsRepeatRequestCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void save(String key) {
        String cacheKey = genKey(key);
        int expire = calCacheExpireTime();
        getCache().set(cacheKey, expire, "1");
    }

    public boolean exist(String key) {
        String cacheKey = genKey(key);
        Object val = getCache().load(cacheKey);
        return val != null;
    }


    private String genKey(String key) {
        return "chips_repeat_request_cache_" + key;
    }

    /**
     *  单位 s
     */
    private int calCacheExpireTime() {
        return 60 * 10;
    }
}
