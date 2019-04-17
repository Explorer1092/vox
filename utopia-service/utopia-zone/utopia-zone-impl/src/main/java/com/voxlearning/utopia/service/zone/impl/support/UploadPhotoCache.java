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
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * For checking if specified student uploaded photo in clazz zone within today.
 * Below staging, always return no photo uploaded.
 * Cache prefix specified for keeping back compatibility.
 *
 * @author Xiaohai Zhang
 * @since May 13, 2015
 */
@UtopiaCachePrefix(prefix = "com.voxlearning.utopia.service.zone.base.cache.UploadPhotoManager")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UploadPhotoCache extends PojoCacheObject<UploadPhotoCache.StudentWithClazz, String> {

    public UploadPhotoCache(UtopiaCache cache) {
        super(cache);
    }

    /**
     * Student has uploaded photo to clazz zone within today.
     *
     * @param studentId student id
     * @param clazzId   clazz id
     */
    public void photoUploaded(Long studentId, Long clazzId) {
        if (studentId == null || clazzId == null) {
            return;
        }
        StudentWithClazz id = new StudentWithClazz(studentId, clazzId);
        set(id, "Y");
    }

    /**
     * Check if student already upload photo to clazz zone within today.
     * Default return true in case of any error occurs.
     *
     * @param studentId student id
     * @param clazzId   clazz id
     * @return true or false
     */
    public boolean alreadyUploaded(Long studentId, Long clazzId) {
        if (studentId == null || clazzId == null) {
            return true;
        }
        // FIXME: ===================
        // FIXME: UNIT TEST HARD CODE
        // FIXME: ===================
        if (RuntimeMode.lt(Mode.STAGING) && !RuntimeMode.isUnitTest()) {
            // always return false below staging mode
            return false;
        }
        StudentWithClazz id = new StudentWithClazz(studentId, clazzId);
        CacheObject<String> cacheObject = cache.get(cacheKey(id));
        if (cacheObject == null) {
            logger.warn("Failed to access cache system, return TRUE to prohibit next photo uploading");
            return true;
        }
        return cacheObject.getValue() != null;
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
