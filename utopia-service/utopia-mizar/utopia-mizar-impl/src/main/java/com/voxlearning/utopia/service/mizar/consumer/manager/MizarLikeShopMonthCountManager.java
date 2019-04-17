package com.voxlearning.utopia.service.mizar.consumer.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * Created by Summer Yang on 2016/9/19.
 */
@UtopiaCachePrefix(prefix = "MIZAR:LSMC")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
public class MizarLikeShopMonthCountManager extends PojoCacheObject<Long, String> {
    public MizarLikeShopMonthCountManager(UtopiaCache cache) {
        super(cache);
    }

    public long increaseCount(Long parentId) {
        if (parentId == null) {
            return 0;
        }
        String cacheKey = cacheKey(parentId);
        return SafeConverter.toLong(cache.incr(cacheKey, 1, 1, expirationInSeconds()));
    }

    public long loadLikeCount(Long parentId) {
        if (parentId == null) {
            return 0;
        }
        String value = load(parentId);
        return SafeConverter.toLong(value);
    }
}
