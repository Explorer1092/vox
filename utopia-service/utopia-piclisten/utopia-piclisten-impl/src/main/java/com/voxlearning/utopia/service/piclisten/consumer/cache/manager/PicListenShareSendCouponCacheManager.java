package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.piclisten.impl.support.PiclistenKillNamiActivity;

/**
 * @author wei.jiang
 * @since 2018/9/4
 */
public class PicListenShareSendCouponCacheManager extends PojoCacheObject<String, String> {

    private final static String SHARE_SEND_COUPON_KEY_PREFIX = "SHARE_SEND_COUPON_KEY_";

    private final static String NEW_SEND_COUPON_KEY_PREFIX = "NEW_SEND_COUPON_KEY_";

    public PicListenShareSendCouponCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Boolean addRecord(Long parentId, String productId) {
        if (parentId == 0L && StringUtils.isBlank(productId)) {
            return false;
        }
        String cacheKey = cacheKey(SHARE_SEND_COUPON_KEY_PREFIX + parentId + productId);
        return getCache().add(cacheKey, 86400, System.currentTimeMillis());
    }

    public Long load(Long parentId, String productId) {
        if (parentId == 0L && StringUtils.isBlank(productId)) {
            return 0L;
        }
        String cacheKey = cacheKey(SHARE_SEND_COUPON_KEY_PREFIX + parentId + productId);
        return SafeConverter.toLong(getCache().load(cacheKey));
    }

    public Boolean addNewSendRecord(Long parentId) {
        if (parentId == 0L) {
            return false;
        }
        String cacheKey = cacheKey(NEW_SEND_COUPON_KEY_PREFIX + parentId);
        return getCache().add(cacheKey, (int) (PiclistenKillNamiActivity.endDate.getTime() / 1000), 1);
    }


    public Long loadNewSendRecord(Long parentId) {
        if (parentId == 0L) {
            return 0L;
        }
        String cacheKey = cacheKey(NEW_SEND_COUPON_KEY_PREFIX + parentId);
        return SafeConverter.toLong(getCache().load(cacheKey));
    }
}
