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

package com.voxlearning.utopia.service.campaign.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

@Named
public class CampaignCacheSystem implements InitializingBean {

    public ContainerCBS CBS;
    @Getter
    private WechatUserCampaignManager wechatUserCampaignManager;


    @Override
    public void afterPropertiesSet() throws Exception {
        CBS = new ContainerCBS();
        CBS.flushable = CacheSystem.CBS.getCache("flushable");
        CBS.unflushable = CacheSystem.CBS.getCache("unflushable");
        CBS.persistence = CacheSystem.CBS.getCache("persistence");
        CBS.storage = CacheSystem.CBS.getCache("storage");
        wechatUserCampaignManager = new WechatUserCampaignManager(CBS.unflushable);
    }

    public static class ContainerCBS {
        public UtopiaCache flushable;
        public UtopiaCache persistence;
        public UtopiaCache unflushable;
        public UtopiaCache storage;
    }
}
