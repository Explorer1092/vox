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

package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.*;

/**
 * Created by Summer Yang on 2015/7/20.
 * 用于记录老师每周布置作业和测验的天数
 */
@UtopiaCachePrefix(prefix = "TEACHER:HAQDW")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class AssignHomeworkAndQuizDayCountManager extends PojoCacheObject<Long, String> {
    public AssignHomeworkAndQuizDayCountManager(UtopiaCache cache) {
        super(cache);
    }

    public Map<Long, Set<String>> currentDays(Collection<Long> userIds) {
        Set<Long> ids = CollectionUtils.toLinkedHashSet(userIds);
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> keys = new HashSet<>();
        for (Long id : ids) {
            keys.add(cacheKey(id));
        }
        Map<String, Set<String>> map = cache.loads(keys);
        Map<Long, Set<String>> result = new HashMap<>();
        for (Long id : ids) {
            Set<String> value = map.get(cacheKey(id));
            result.put(id, value);
        }
        return result;
    }

    public void addAssignDays(Collection<Long> userIds) {
        Set<Long> ids = CollectionUtils.toLinkedHashSet(userIds);
        if (ids.isEmpty()) {
            return;
        }
        Set<String> keys = new HashSet<>();
        for (Long id : ids) {
            keys.add(cacheKey(id));
        }
        Map<String, CacheObject<Set<String>>> map = cache.gets(keys);
        String nowDate = DateUtils.dateToString(new Date(), "yyyyMMdd");
        for (Long id : ids) {
            CacheObject<Set<String>> cacheObject = map.get(cacheKey(id));
            if (cacheObject == null || CollectionUtils.isEmpty(cacheObject.getValue())) {
                Set<String> data = Collections.singleton(nowDate);
                cache.set(cacheKey(id), DateUtils.getCurrentToWeekEndSecond(), data);
            } else if (!cacheObject.getValue().contains(nowDate)) {
                cache.cas(cacheKey(id), DateUtils.getCurrentToWeekEndSecond(), cacheObject, currentValue -> {
                    currentValue = new LinkedHashSet<>(currentValue);
                    currentValue.add(nowDate);
                    return currentValue;
                });
            }
        }
    }
}
