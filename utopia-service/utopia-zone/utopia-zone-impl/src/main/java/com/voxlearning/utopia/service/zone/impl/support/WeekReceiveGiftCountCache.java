/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * For recording user received gift count within current week.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @version 0.1
 * @since 3/16/2015
 */
@UtopiaCachePrefix(prefix = "CLAZZ_ZONE:WRGC")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class WeekReceiveGiftCountCache extends PojoCacheObject<Long, String> {

    public WeekReceiveGiftCountCache(UtopiaCache cache) {
        super(cache);
    }

    /**
     * How many gift received this week
     *
     * @param userId user id
     * @return received gift count within current week, return 0 if no user found
     */
    public int currentCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return SafeConverter.toInt(load(userId));
    }

    /**
     * FIXME: 这个方法为什么没有调用了？
     * Invoke this method after gift successfully sent.
     *
     * @param userId user id
     */
    public void increase(Long userId) {
        if (userId == null) {
            return;
        }
        String cacheKey = cacheKey(userId);
        cache.incr(cacheKey, 1, 1, expirationInSeconds());
    }
}
