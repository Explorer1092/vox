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
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 老师鲜花榜排名领取奖励管理器
 * Created by Shuai Huan on 2015/6/3.
 */
@UtopiaCachePrefix(prefix = "FLOWER:TFRM")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
public class TeacherFlowerRewardManager extends PojoCacheObject<Long, String> {
    public TeacherFlowerRewardManager(UtopiaCache cache) {
        super(cache);
    }

    public void receiveReward(Long teacherId) {
        set(teacherId, "Y");
    }

    public boolean hasReceivedIntegral(Long teacherId) {
        if (teacherId == null) {
            return true;
        }
        return load(teacherId) != null;
    }
}
