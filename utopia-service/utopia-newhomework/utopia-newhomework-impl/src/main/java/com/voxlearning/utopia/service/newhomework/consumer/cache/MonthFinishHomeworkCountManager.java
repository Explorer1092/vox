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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * For recording student finished homework count with in current month.
 * Extends from PojoCacheObject. Use UtopiaCachePrefix to keep backward compatibility.
 *
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @version 0.1
 * @since 3/26/2015
 */
@UtopiaCachePrefix(prefix = "HOMEWORK:MFHCM")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
public class MonthFinishHomeworkCountManager extends PojoCacheObject<MonthFinishHomeworkCountManager.UserWithHomeworkType, String> {

    public MonthFinishHomeworkCountManager(UtopiaCache cache) {
        super(cache);
    }

    public Map<Long, Integer> currentCount(Collection<Long> userIds, HomeworkType homeworkType) {
        Set<Long> ids = CollectionUtils.toLinkedHashSet(userIds);
        if (ids.isEmpty() || homeworkType == null) {
            return Collections.emptyMap();
        }
        Set<String> keys = new HashSet<>();
        for (Long id : ids) {
            UserWithHomeworkType uht = new UserWithHomeworkType(id, homeworkType);
            keys.add(cacheKey(uht));
        }
        Map<String, String> map = cache.loads(keys);
        Map<Long, Integer> result = new HashMap<>();
        for (Long id : ids) {
            UserWithHomeworkType uht = new UserWithHomeworkType(id, homeworkType);
            String value = map.get(cacheKey(uht));
            result.put(id, SafeConverter.toInt(value));
        }
        return result;
    }

    public void increase(Collection<Long> userIds, HomeworkType homeworkType) {
        Set<Long> ids = CollectionUtils.toLinkedHashSet(userIds);
        if (ids.isEmpty() || homeworkType == null) {
            return;
        }
        for (Long id : ids) {
            UserWithHomeworkType uht = new UserWithHomeworkType(id, homeworkType);
            cache.incr(cacheKey(uht), 1, 1, expirationInSeconds());
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"relevantUserId", "homeworkType"})
    public static class UserWithHomeworkType {
        public Long relevantUserId;
        public HomeworkType homeworkType;

        @Override
        public String toString() {
            // this toString is necessary
            return "U=" + relevantUserId + ",HT=" + homeworkType.name();
        }
    }
}
