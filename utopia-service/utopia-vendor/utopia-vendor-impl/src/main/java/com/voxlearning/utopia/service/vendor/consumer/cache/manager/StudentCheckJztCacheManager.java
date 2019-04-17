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

package com.voxlearning.utopia.service.vendor.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 看到过强绑家长通卡片的用户，7*24小时内不再看见
 * Created by shuai.huan on 2016/4/18.
 */
@UtopiaCachePrefix(prefix = "APP:SCJCM")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 604800)
public class StudentCheckJztCacheManager extends PojoCacheObject<Long, String> {

    public StudentCheckJztCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long studentId) {
        set(studentId, "Y");
    }

    public boolean hasRecord(Long studentId) {
        if (studentId == null) {
            return true;
        }
        return load(studentId) != null;
    }
}
