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

/**
 * 记录学生当天是否已经收到过关于被赞或者被评论的右下角弹窗
 *
 * @author RuiBao
 * @since 11/16/2015
 */
@UtopiaCachePrefix(prefix = "CLAZZ_ZONE:DLC")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class DailyLikeOrCommentCache extends PojoCacheObject<Long, String> {

    public DailyLikeOrCommentCache(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long studentId) {
        if (studentId == null) return;
        set(studentId, "dummy");
    }

    public boolean sended(Long studentId) {
        return studentId == null || load(studentId) != null;
    }
}
