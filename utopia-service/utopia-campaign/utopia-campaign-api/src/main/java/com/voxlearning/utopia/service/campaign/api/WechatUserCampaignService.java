package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.01.16")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WechatUserCampaignService {

    @Async
    AlpsFuture<CampaignType> loadUserCampaign(Long userId);

    @Async
    AlpsFuture<Boolean> setUserCampaign(Long userId, Integer campaignId);

    @Async
    AlpsFuture<Boolean> removeUserCampaign(Long userId);
}
