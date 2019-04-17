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

package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * For recording user study master count within current month.
 *
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @version 0.1
 * @since 3/26/2015
 */
@UtopiaCachePrefix(prefix = "CLAZZ_ZONE:MSMCM")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
public class MonthStudyMasterCountCache extends PojoCacheObject<MonthStudyMasterCountCache.IdWithHomeworkType, String> {

    public MonthStudyMasterCountCache(UtopiaCache cache) {
        super(cache);
    }

    public Map<Long, Integer> currentCount(Collection<Long> userIds, HomeworkType type) {
        Set<Long> ids = CollectionUtils.toLinkedHashSet(userIds);
        if (ids.isEmpty() || type == null) {
            return Collections.emptyMap();
        }
        Set<String> keys = new HashSet<>();
        for (Long id : ids) {
            IdWithHomeworkType inst = new IdWithHomeworkType(id, type);
            keys.add(cacheKey(inst));
        }
        Map<String, String> map = cache.loads(keys);
        Map<Long, Integer> result = new HashMap<>();
        for (Long id : ids) {
            IdWithHomeworkType inst = new IdWithHomeworkType(id, type);
            String key = cacheKey(inst);
            String value = map.get(key);
            result.put(id, SafeConverter.toInt(value));
        }
        return result;
    }

    public void increase(Collection<Long> userIds, HomeworkType type) {
        Set<Long> ids = CollectionUtils.toLinkedHashSet(userIds);
        if (ids.isEmpty() || type == null) {
            return;
        }
        for (Long id : ids) {
            IdWithHomeworkType inst = new IdWithHomeworkType(id, type);
            String key = cacheKey(inst);
            cache.incr(key, 1, 1, expirationInSeconds());
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdWithHomeworkType {
        public Long relevantUserId;
        public HomeworkType homeworkType;

        @Override
        public String toString() {
            // this toString is necessary
            return "U=" + relevantUserId + ",HT=" + homeworkType.name();
        }
    }
}
