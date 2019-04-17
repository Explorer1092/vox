package com.voxlearning.utopia.service.vendor.impl.push.umeng;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author xinxin
 * @since 4/27/17.
 */
public class UmengPushRateController {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final String CACHE_KEY_PREFIX_CURRENT_MINUTE_COUNT = "UMENG_TAG_PUSH_CURRENT_MINUTE_COUNT_";
    private static final String CACHE_KEY_PREFIX_MAX_MINUTE_COUNT = "UMENG_TAG_PUSH_MAX_MINUTE_COUNT_";
    private static final Integer MAX_REQUEST_COUNT_PER_MINUTE = RuntimeMode.current().le(Mode.STAGING) ? 5 : 1000;


    public static boolean isProduceAvailable(PushContext context) {
        //通过别名推送的不做控制
        if (PushTargetType.ALIAS.name().equals(context.getTargetType())) {
            return true;
        }

        String currentMinuteStr = UmengPushRateController.formatter.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        Long count = VendorCache.getVendorCache().incr(CACHE_KEY_PREFIX_CURRENT_MINUTE_COUNT + context.getSource().appKey + "_" + currentMinuteStr, 1, 1, 60 * 60);

        //记录下每分钟请求次数的极值
        CacheObject<Long> maxCount = VendorCache.getVendorCache().get(CACHE_KEY_PREFIX_MAX_MINUTE_COUNT + context.getSource().appKey);
        if (null == maxCount || null == maxCount.getValue()) {
            VendorCache.getVendorCache().incr(CACHE_KEY_PREFIX_MAX_MINUTE_COUNT + context.getSource().appKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
        } else if (count > SafeConverter.toLong(maxCount.getValue())) {
            //这里忽略并发造成的误差
            VendorCache.getVendorCache().set(CACHE_KEY_PREFIX_MAX_MINUTE_COUNT + context.getSource().appKey, DateUtils.getCurrentToDayEndSecond(), count);
        }

        return count <= MAX_REQUEST_COUNT_PER_MINUTE;
    }
}
