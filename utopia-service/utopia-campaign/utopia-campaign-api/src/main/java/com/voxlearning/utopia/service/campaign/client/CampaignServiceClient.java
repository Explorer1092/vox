package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.campaign.api.CampaignService;
import lombok.Getter;

public class CampaignServiceClient {

    @Getter
    @ImportService(interfaceClass = CampaignService.class)
    private CampaignService campaignService;
}
