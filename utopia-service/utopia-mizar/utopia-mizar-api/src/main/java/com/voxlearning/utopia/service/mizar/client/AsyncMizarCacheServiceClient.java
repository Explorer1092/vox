package com.voxlearning.utopia.service.mizar.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mizar.api.AsyncMizarCacheService;
import lombok.Getter;

public class AsyncMizarCacheServiceClient {

    @Getter
    @ImportService(interfaceClass = AsyncMizarCacheService.class)
    private AsyncMizarCacheService asyncMizarCacheService;
}
