package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ZoneStudyMasterService;
import lombok.Getter;

public class ZoneStudyMasterServiceClient {

    @Getter
    @ImportService(interfaceClass = ZoneStudyMasterService.class)
    private ZoneStudyMasterService zoneStudyMasterService;
}
