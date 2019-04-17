package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_zone_activity_type")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 30)
public class ClassActivityCountCache extends PojoCacheObject<String, Long> {
    public static final String KEY = "count_";
    protected ClassActivityCountCache(UtopiaCache cache) {
        super(cache);
    }
    public void increaseByActivity(Integer activityId) {
        String key = cacheKey(KEY+activityId);
        cache.incr(key, 1, 1, expirationInSeconds());
    }

    public void setIncreaseByActivity(Integer activityId, Long num) {
        String key = cacheKey(KEY + activityId);
        cache.set(key, expirationInSeconds(), num.toString());
    }
    public Long loadByActivity(Integer activityId) {
        String key = cacheKey(KEY+activityId);
        Object value = cache.load(key);
        //兼容持久化 monggo存的值是String类型
        if (value == null) {
            return null;
        } else {
            if (value instanceof String) {
                return Long.parseLong((String) value);
            } else {
                return (Long) value;
            }
        }
    }
    public String loadByKey(String key) {
        return cache.load(key);
    }
    public void setValueByKey(String key,String value) {
        cache.set(key,86400 * 30,value);
    }
    public void deleteKey(String key){
        cache.delete(key);
    }
}
