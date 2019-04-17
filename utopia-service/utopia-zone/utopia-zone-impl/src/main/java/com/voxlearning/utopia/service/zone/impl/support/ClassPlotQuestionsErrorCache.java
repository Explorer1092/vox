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
@UtopiaCachePrefix(prefix = "class_zone_plot_question")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClassPlotQuestionsErrorCache extends PojoCacheObject<String, Long> {
    public static final String KEY = "error_";
    protected ClassPlotQuestionsErrorCache(UtopiaCache cache) {
        super(cache);
    }
    public void incrErrorCount(Integer activityId,Long userId) {
        String key = cacheKey(KEY+activityId+"_"+userId);
        cache.incr(key, 0, 1, expirationInSeconds());
    }

    public Long loadUserCount(Integer activityId,Long userId){
        String key = cacheKey(KEY+activityId+"_"+userId);
        return load(key);
    }

}
