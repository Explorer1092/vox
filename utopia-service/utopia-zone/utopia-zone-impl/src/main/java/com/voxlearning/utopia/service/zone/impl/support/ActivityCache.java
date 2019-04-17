package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;

import java.util.List;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_zone_activity")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 30 * 24 * 3600)
public class ActivityCache extends PojoCacheObject<String, List<ClazzActivity>> {
    public static final String KEY = "all_activity";
    protected ActivityCache(UtopiaCache cache) {
        super(cache);
    }
    public  List<ClazzActivity> findActivityCache(){
        String cacheKey = cacheKey(KEY);
        return cache.load(cacheKey);
    }

}
