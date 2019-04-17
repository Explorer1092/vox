package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.AiOrderProductService;
import lombok.Getter;

/**
 * @author guangqing
 * @since 2018/8/15
 */
public class AiOrderProductServiceClient {

    @Getter
    @ImportService(interfaceClass = AiOrderProductService.class)
    AiOrderProductService remoteReference;
}
