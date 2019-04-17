package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 吃鸡活动
 * @author : xuedongfeng
 * @version : 2018-11-15
 **/
@UtopiaCachePrefix(prefix = "clazz_zone_chicken_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 30)
public class ClazzCircleChickenCache extends PojoCacheObject<String, String> {

    private static final String PREFIX_KEY = "chicken_";

    protected ClazzCircleChickenCache(UtopiaCache cache) {
        super(cache);
    }

    public Long increase(String postKey) {
        String key = cacheKey(PREFIX_KEY + postKey);
        return cache.incr(key, 1, 1, expirationInSeconds());
    }

    public Long loadByKey(String postKey){
        String key = cacheKey(PREFIX_KEY + postKey);
        return SafeConverter.toLong(cache.load(key));
    }

    public Boolean setByKey(String postKey,Long value){
        String key = cacheKey(PREFIX_KEY + postKey);
        return cache.set(key,expirationInSeconds(),value);
    }

    public Boolean deleteByKey(String postKey){
        String key = cacheKey(PREFIX_KEY + postKey);
        return cache.delete(key);
    }

}
