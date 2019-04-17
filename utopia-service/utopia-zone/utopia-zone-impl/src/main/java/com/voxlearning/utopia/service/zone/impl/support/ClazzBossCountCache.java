package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author : kai.sun
 * @version : 2018-11-06
 * @description :
 **/

@UtopiaCachePrefix(prefix = "clazz_boss_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 30)
public class ClazzBossCountCache extends PojoCacheObject<String, Long> {

    private static final String PREFIX_KEY = "count_";

    protected ClazzBossCountCache(UtopiaCache cache) {
        super(cache);
    }

    public Long increase(String postKey) {
        String key = cacheKey(PREFIX_KEY+postKey);
        return cache.incr(key, 1, 1, expirationInSeconds());
    }

    public Long loadByKey(String postKey){
        String key = cacheKey(PREFIX_KEY+postKey);
        return SafeConverter.toLong(cache.load(key));
    }

    public Boolean setByKey(String postKey,Long value){
        String key = cacheKey(PREFIX_KEY+postKey);
        return cache.set(key,expirationInSeconds(),value);
    }

    public Boolean deleteByKey(String postKey){
        String key = cacheKey(PREFIX_KEY+postKey);
        return cache.delete(key);
    }

    public static String generatorSchoolCountCacheKey(Long schoolId,Integer type){
        return String.valueOf(schoolId)+"_"+String.valueOf(type);
    }

    public static String generatorClazzCountCacheKey(Long clazzId){
        return String.valueOf(clazzId);
    }

}
