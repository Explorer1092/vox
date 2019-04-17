package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.AiLessonConfigService;
import lombok.Getter;

/**
 * @author songtao
 * @since 2018/4/10
 */
public class AiLessonConfigServiceClient {
    @Getter
    @ImportService(interfaceClass = AiLessonConfigService.class)
    private AiLessonConfigService remoteReference;
}
