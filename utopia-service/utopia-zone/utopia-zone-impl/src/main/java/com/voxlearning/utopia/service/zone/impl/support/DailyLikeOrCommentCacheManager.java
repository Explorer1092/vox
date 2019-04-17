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

@Named("com.voxlearning.utopia.service.zone.impl.support.DailyLikeOrCommentCacheManager")
public class DailyLikeOrCommentCacheManager implements InitializingBean {

    private DailyLikeOrCommentCache dailyLikeOrCommentCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
        dailyLikeOrCommentCache = new DailyLikeOrCommentCache(cache);
    }

    public void record(Long studentId) {
        dailyLikeOrCommentCache.record(studentId);
    }

    public boolean sent(Long studentId) {
        return dailyLikeOrCommentCache.sended(studentId);
    }
}
