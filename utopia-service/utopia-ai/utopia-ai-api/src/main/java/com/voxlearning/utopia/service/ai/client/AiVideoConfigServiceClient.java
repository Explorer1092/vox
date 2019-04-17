package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.AiVideoConfigService;
import lombok.Getter;

/**
　* @Description: app视频模块操作
　* @author zhiqi.yao
　* @date 2018/4/19 18:45
*/
public class AiVideoConfigServiceClient {
    @Getter
    @ImportService(interfaceClass = AiVideoConfigService.class)
    private AiVideoConfigService remoteReference;
}
