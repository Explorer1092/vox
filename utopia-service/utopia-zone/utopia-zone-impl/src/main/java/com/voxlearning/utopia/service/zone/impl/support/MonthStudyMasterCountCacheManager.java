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
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@Named("com.voxlearning.utopia.service.zone.impl.support.MonthStudyMasterCountCacheManager")
public class MonthStudyMasterCountCacheManager implements InitializingBean {

    private MonthStudyMasterCountCache monthStudyMasterCountCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("persistence");
        monthStudyMasterCountCache = new MonthStudyMasterCountCache(cache);
    }

    public Map<Long, Integer> currentCount(Collection<Long> userIds, HomeworkType type) {
        return monthStudyMasterCountCache.currentCount(userIds, type);
    }

    public void increase(Collection<Long> userIds, HomeworkType type) {
        monthStudyMasterCountCache.increase(userIds, type);
    }
}
