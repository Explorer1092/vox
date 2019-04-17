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

package com.voxlearning.ucenter.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import javax.inject.Named;

/**
 * ucenter cache system
 *
 * @author changyuan.liu
 * @since 2015.12.6
 */
@Named
public class UcenterWebCacheSystem extends SpringContainerSupport {

    public CBS_Container CBS;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        CBS = new CBS_Container();
        CBS.flushable = CacheSystem.CBS.getCacheBuilder().getCache("flushable");
        CBS.unflushable = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
        CBS.persistence = CacheSystem.CBS.getCacheBuilder().getCache("persistence");
    }

    public static class CBS_Container {
        public UtopiaCache flushable;
        public UtopiaCache unflushable;
        public UtopiaCache persistence;
    }
}
