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
import com.voxlearning.alps.spi.common.DataProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xiaopeng.yang on 2015/5/28.
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MentorLatestCacheManager extends PojoCacheObject<Long, Map<String, Object>> {

    public MentorLatestCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Map<String, Object> pureLoad(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyMap();
        }
        return load(teacherId);
    }

    public void pureAdd(Long teacherId, Map<String, Object> data) {
        if (teacherId == null || data == null) {
            return;
        }
        add(teacherId, data);
    }

    @Deprecated
    public Map<String, Object> getMentorLatestInfo(Long teacherId, DataProvider<Long, Map<String, Object>> provider) {
        if (teacherId == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = load(teacherId);
        if (map != null) {
            return map;
        }
        try {
            map = Objects.requireNonNull(provider).provide(teacherId);
        } catch (Exception ex) {
            logger.error("Failed to load mentor latest info teacherId={}", teacherId, ex);
            map = null;
        }
        if (map != null) {
            add(teacherId, map);
        }
        return map;
    }

    public void clean(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        evict(teacherId);
    }

}
