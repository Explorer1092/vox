package com.voxlearning.utopia.service.ambassador.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ambassador.api.AmbassadorService;
import lombok.Getter;

public class AmbassadorServiceClient {

    @Getter
    @ImportService(interfaceClass = AmbassadorService.class)
    private AmbassadorService ambassadorService;
}
