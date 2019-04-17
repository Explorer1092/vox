package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.campaign.api.MothersDayService;
import lombok.Getter;

public class MothersDayServiceClient {

    @Getter
    @ImportService(interfaceClass = MothersDayService.class)
    private MothersDayService mothersDayService;
}
