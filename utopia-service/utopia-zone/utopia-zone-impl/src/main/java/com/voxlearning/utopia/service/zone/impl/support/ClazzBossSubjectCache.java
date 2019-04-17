package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author : kai.sun
 * @version : 2018-11-08
 * @description :
 **/
@UtopiaCachePrefix(prefix = "clazz_zone_boss_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 30)
public class ClazzBossSubjectCache extends PojoCacheObject<String, String> {

    private static final String PREFIX_KEY = "subject_";

    protected ClazzBossSubjectCache(UtopiaCache cache) {
        super(cache);
    }

    public boolean setValueByKey(String postKey,String value) {
        String key = cacheKey(PREFIX_KEY+postKey);
        return cache.set(key,expirationInSeconds(),value);
    }

    public String getValueByKey(String postKey) {
        String key = cacheKey(PREFIX_KEY+postKey);
        return cache.load(key);
    }

}
