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
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * @author RuiBao
 * @since 11/17/2015
 */
@UtopiaCachePrefix(prefix = "CLAZZ_ZONE:DNLCR")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class DailyNewLikeOrCommentReceivedCache extends PojoCacheObject<Long, String> {

    public DailyNewLikeOrCommentReceivedCache(UtopiaCache cache) {
        super(cache);
    }

    public boolean show(Long studentId) {
        return studentId != null && load(studentId) != null;
    }

    public void turnOn(Long studentId) {
        if (studentId == null || load(studentId) != null) return;
        set(studentId, "dummy");
    }

    public void turnOff(Long studentId) {
        if (studentId == null) return;
        evict(studentId);
    }
}
