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

package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.campaign.api.WechatUserCampaignService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.campaign.impl.service.WechatUserCampaignServiceImpl")
@ExposeService(interfaceClass = WechatUserCampaignService.class)
public class WechatUserCampaignServiceImpl implements WechatUserCampaignService {

    @Inject private CampaignCacheSystem campaignCacheSystem;

    @Override
    public AlpsFuture<CampaignType> loadUserCampaign(Long userId) {
        CampaignType ct = campaignCacheSystem.getWechatUserCampaignManager().loadUserCampaign(userId);
        return new ValueWrapperFuture<>(ct);
    }

    @Override
    public AlpsFuture<Boolean> setUserCampaign(Long userId, Integer campaignId) {
        boolean ret = campaignCacheSystem.getWechatUserCampaignManager().set(userId, campaignId);
        return new ValueWrapperFuture<>(ret);
    }

    @Override
    public AlpsFuture<Boolean> removeUserCampaign(Long userId) {
        campaignCacheSystem.getWechatUserCampaignManager().evict(userId);
        return new ValueWrapperFuture<>(true);
    }
}
