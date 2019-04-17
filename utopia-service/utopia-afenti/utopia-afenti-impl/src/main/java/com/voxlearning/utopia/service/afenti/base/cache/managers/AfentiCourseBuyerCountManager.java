package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 阿分题视频总购买人数缓存
 *
 * @author liu jingchao
 * @since 2017/3/29
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 365 * 3)
public class AfentiCourseBuyerCountManager extends PojoCacheObject<String, Long> {

    private final static String CACHE_PREFIX = "AFENTI_VIDEO_COURSE_BUYER_COUNT";
    private final static Long DEFAULT_COUNT = 110090L;

    public AfentiCourseBuyerCountManager(UtopiaCache cache) {
        super(cache);
    }

    // 获取当前的购买人数
    public Long getCurrentBuyerCount() {
        try {
            Long buyerCount = load(CACHE_PREFIX);
            return buyerCount == null ? DEFAULT_COUNT : buyerCount;
        } catch (Exception e) {
            return DEFAULT_COUNT;
        }
    }

    // 更新或加入当前的购买人数
    public void upsertBuyerCount() {
        String key = cacheKey(CACHE_PREFIX);
        CacheObject<Long> cacheObject = getCache().get(key);
        if (null != cacheObject.getValue()) {
            cache.cas(key, expirationInSeconds(), cacheObject, 3, currentValue -> {
                currentValue = new Long(currentValue);
                return ++currentValue;
            });
        } else {
            cache.add(key, expirationInSeconds(), DEFAULT_COUNT + 1);
        }
    }

}
