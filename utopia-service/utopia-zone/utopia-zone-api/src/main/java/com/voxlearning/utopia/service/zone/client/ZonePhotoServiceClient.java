package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ZonePhotoService;
import lombok.Getter;

public class ZonePhotoServiceClient {

    @Getter
    @ImportService(interfaceClass = ZonePhotoService.class)
    private ZonePhotoService zonePhotoService;
}
