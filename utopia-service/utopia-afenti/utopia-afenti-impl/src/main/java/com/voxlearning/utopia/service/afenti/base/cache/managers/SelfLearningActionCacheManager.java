package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author Ruib
 * @since 2016/8/25
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class SelfLearningActionCacheManager extends PojoCacheObject<Long, String> {

    public SelfLearningActionCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean sended(Long studentId) {
        return null != studentId && !add(studentId, "dummy");
    }
}
