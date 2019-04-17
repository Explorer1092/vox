package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZoneUserRecord;

import java.util.List;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_zone_discuss")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class DiscussRecordCache extends PojoCacheObject<String, List<DiscussZoneUserRecord>> {
    public static final String KEY = "record:";

    protected DiscussRecordCache(UtopiaCache cache) {
        super(cache);
    }

    public List<DiscussZoneUserRecord> findRecordCache(Integer discussId, Long clazzId) {
        String cacheKey = cacheKey(KEY + discussId + "_" + clazzId);
        return cache.load(cacheKey);
    }
}
