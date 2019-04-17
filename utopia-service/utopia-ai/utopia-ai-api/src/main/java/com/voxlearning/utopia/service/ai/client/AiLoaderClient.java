package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.utopia.service.ai.api.AiLoader;
import com.voxlearning.alps.annotation.remote.ImportService;
import lombok.Getter;

/**
 * Created by Summer on 2018/3/27
 */
public class AiLoaderClient {

    @Getter
    @ImportService(interfaceClass = AiLoader.class)
    AiLoader remoteReference;


}
