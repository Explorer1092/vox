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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.alps.spi.common.DataProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by xiaopeng.yang on 2015/5/28.
 * 期末回馈计划 -- MENTOR -- 可帮助的老师列表缓存管理器
 *
 * @deprecated 只有清除缓存操作而没有写缓存方法的调用
 */
@Deprecated
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MentorTermEndCacheManager extends PojoCacheObject<Long, List<Map<String, Object>>> {

    public MentorTermEndCacheManager(UtopiaCache cache) {
        super(cache);
    }

    @Deprecated
    public List<Map<String, Object>> getTermEndTeacherList(Long schoolId, DataProvider<Long, List<Map<String, Object>>> provider) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = load(schoolId);
        if (list != null) {
            return list;
        }
        try {
            list = Objects.requireNonNull(provider).provide(schoolId);
        } catch (Exception ex) {
            logger.error("Failed to load mentor incrStudentSelfLearnCount student count list schoolId={}", schoolId, ex);
            list = null;
        }
        if (list != null) {
            add(schoolId, list);
        }
        return CollectionUtils.toLinkedList(list);
    }

    public void clean(Long schoolId) {
        if (schoolId == null) {
            return;
        }
        evict(schoolId);
    }
}
