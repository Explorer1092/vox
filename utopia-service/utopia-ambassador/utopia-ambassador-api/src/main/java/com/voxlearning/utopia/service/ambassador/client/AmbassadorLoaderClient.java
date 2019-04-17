package com.voxlearning.utopia.service.ambassador.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ambassador.api.AmbassadorLoader;
import lombok.Getter;

public class AmbassadorLoaderClient {

    @Getter
    @ImportService(interfaceClass = AmbassadorLoader.class)
    private AmbassadorLoader ambassadorLoader;
}
