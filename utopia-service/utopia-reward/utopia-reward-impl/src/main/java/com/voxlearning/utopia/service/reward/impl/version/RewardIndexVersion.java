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

package com.voxlearning.utopia.service.reward.impl.version;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import javax.inject.Named;
import java.util.Objects;

@Named
public class RewardIndexVersion extends SpringContainerSupport {

    private static final String KEY = "RewardIndexVersion";

    private UtopiaCache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        cache = CacheSystem.CBS.getCache("storage");
        Objects.requireNonNull(cache);
    }

    public long currentVersion() {
        return SafeConverter.toLong(cache.load(KEY), 1);
    }

    public void increase() {
        cache.incr(KEY, 1, 2, 0);
    }
}
