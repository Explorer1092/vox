/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 *
 * @author RuiBao
 * @version 0.1
 * @since 7/7/2015
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class StudentWishCreationCacheManager extends PojoCacheObject<Long, String> {

    public StudentWishCreationCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long studentId) {
        if (studentId == null) return;
        add(studentId, "dummy");
    }

    public boolean wishMadeThisWeek(Long studentId) {
        return studentId == null || load(studentId) != null;
    }
}
