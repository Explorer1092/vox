/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.zone.impl.support.DailyNewLikeOrCommentReceivedCacheManager")
public class DailyNewLikeOrCommentReceivedCacheManager implements InitializingBean {

    private DailyNewLikeOrCommentReceivedCache dailyNewLikeOrCommentReceivedCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
        dailyNewLikeOrCommentReceivedCache = new DailyNewLikeOrCommentReceivedCache(cache);
    }

    public boolean show(Long studentId) {
        return dailyNewLikeOrCommentReceivedCache.show(studentId);
    }

    public void turnOn(Long studentId) {
        dailyNewLikeOrCommentReceivedCache.turnOn(studentId);
    }

    public void turnOff(Long studentId) {
        dailyNewLikeOrCommentReceivedCache.turnOff(studentId);
    }
}
