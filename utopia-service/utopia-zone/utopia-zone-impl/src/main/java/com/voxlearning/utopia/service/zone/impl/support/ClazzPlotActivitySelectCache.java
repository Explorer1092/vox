package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author : kai.sun
 * @version : 2018-11-26
 * @description :
 **/

@UtopiaCachePrefix(prefix = "clazz_plot_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 30)
public class ClazzPlotActivitySelectCache extends PojoCacheObject<String, Long> {

    private static final String PREFIX_KEY = "select_";

    protected ClazzPlotActivitySelectCache(UtopiaCache cache) {
        super(cache);
    }

    public Long increase(String key) {
        return cache.incr(cacheKey(key), 1, 1, expirationInSeconds());
    }

    public Long loadByKey(String key){
        return SafeConverter.toLong(cache.load(cacheKey(key)));
    }

    public static String generatorCacheKey(Integer activityId,Integer plotGroup,Integer common){
        return PREFIX_KEY+ activityId + "_" + plotGroup+"_" + common;
    }

}
