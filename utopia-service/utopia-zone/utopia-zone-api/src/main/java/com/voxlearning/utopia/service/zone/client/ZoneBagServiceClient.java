package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ZoneBagService;
import lombok.Getter;

public class ZoneBagServiceClient {

    @Getter
    @ImportService(interfaceClass = ZoneBagService.class)
    private ZoneBagService zoneBagService;
}
