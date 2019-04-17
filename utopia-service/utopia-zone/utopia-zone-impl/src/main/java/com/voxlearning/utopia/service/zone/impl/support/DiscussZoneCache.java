package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZone;

import java.util.List;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_zone_discuss")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class DiscussZoneCache extends PojoCacheObject<String, List<DiscussZone>> {
    public static final String KEY = "all_discuss";
    protected DiscussZoneCache(UtopiaCache cache) {
        super(cache);
    }
    public  List<DiscussZone> findUsedDiscussZoneCache(){
        String cacheKey = cacheKey(KEY);
        return cache.load(cacheKey);
    }
}
