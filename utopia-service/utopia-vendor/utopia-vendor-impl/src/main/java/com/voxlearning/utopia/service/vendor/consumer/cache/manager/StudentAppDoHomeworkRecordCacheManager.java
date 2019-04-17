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
 * 学生端记录每天学生是否做过作业
 * Created by Shuai Huan on 2015/11/6.
 */
@UtopiaCachePrefix(prefix = "APP:SADHR")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudentAppDoHomeworkRecordCacheManager extends PojoCacheObject<Long, String> {

    public StudentAppDoHomeworkRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void doHomework(Long studentId) {
        set(studentId, "Y");
    }

    public boolean hasDoneHomework(Long studentId) {
        if (studentId == null) {
            return true;
        }
        return load(studentId) != null;
    }
}
