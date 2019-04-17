package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.campaign.api.WechatUserCampaignService;
import lombok.Getter;

public class WechatUserCampaignServiceClient {

    @Getter
    @ImportService(interfaceClass = WechatUserCampaignService.class)
    private WechatUserCampaignService wechatUserCampaignService;
}
