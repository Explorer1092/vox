package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ZoneLikeService;
import lombok.Getter;

public class ZoneLikeServiceClient {

    @Getter
    @ImportService(interfaceClass = ZoneLikeService.class)
    private ZoneLikeService zoneLikeService;
}
