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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 基于couchbase的缓存实现，用于维护学生（指定班级）当日是否已经在班级空间签到。
 * 缓存的内容没有任何影响，可以是任意值。只需要缓存键。
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
@UtopiaCachePrefix(prefix = "CLAZZ_ZONE:CZSI")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ZoneSignInCache extends PojoCacheObject<ZoneSignInCache.StudentWithClazz, String> {

    public ZoneSignInCache(UtopiaCache cache) {
        super(cache);
    }

    public boolean alreadySignedIn(Long studentId, Long clazzId) {
        if (studentId == null || clazzId == null) {
            return false;
        }
        StudentWithClazz sc = new StudentWithClazz(studentId, clazzId);
        return load(sc) != null;
    }

    public void setSignedIn(Long studentId, Long clazzId) {
        if (studentId == null || clazzId == null) {
            return;
        }
        StudentWithClazz sc = new StudentWithClazz(studentId, clazzId);
        set(sc, "1");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentWithClazz {

        public Long studentId;
        public Long clazzId;

        @Override
        public String toString() {
            // this toString is necessary
            return "S=" + studentId + ",C=" + clazzId;
        }
    }
}
