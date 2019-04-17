package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ZoneSignInService;
import lombok.Getter;

@Spring
public class ZoneSignInServiceClient {

    @Getter
    @ImportService(interfaceClass = ZoneSignInService.class)
    private ZoneSignInService zoneSignInService;
}
