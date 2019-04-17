/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.wechat.impl.support.WechatCacheSystem")
public class WechatCacheSystem extends SpringContainerSupport {

    public CBS_Container CBS;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        CBS = new CBS_Container();
        CBS.flushable = CacheSystem.CBS.getCache("flushable");
        CBS.persistence = CacheSystem.CBS.getCache("persistence");
    }

    public static class CBS_Container {
        public UtopiaCache flushable;
        public UtopiaCache persistence;
    }
}
