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

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;

@UtopiaCachePrefix(prefix = "WechatUserCampaignManager")
@UtopiaCacheExpiration(86400)
public class WechatUserCampaignManager extends PojoCacheObject<Long, Integer> {
    public WechatUserCampaignManager(UtopiaCache cache) {
        super(cache);
    }

    public CampaignType loadUserCampaign(Long userId) {
        Integer campaignId = load(userId);
        return campaignId == null ? null : CampaignType.of(campaignId);
    }
}
