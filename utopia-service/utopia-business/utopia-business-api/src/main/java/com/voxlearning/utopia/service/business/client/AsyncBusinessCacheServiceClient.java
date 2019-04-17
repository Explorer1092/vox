package com.voxlearning.utopia.service.business.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.business.api.AsyncBusinessCacheService;
import lombok.Getter;

public class AsyncBusinessCacheServiceClient {

    @Getter
    @ImportService(interfaceClass = AsyncBusinessCacheService.class)
    private AsyncBusinessCacheService asyncBusinessCacheService;
}
