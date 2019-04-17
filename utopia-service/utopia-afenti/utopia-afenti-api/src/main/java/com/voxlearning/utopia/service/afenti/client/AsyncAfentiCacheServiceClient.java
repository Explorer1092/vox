package com.voxlearning.utopia.service.afenti.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.afenti.api.AsyncAfentiCacheService;
import lombok.Getter;

public class AsyncAfentiCacheServiceClient {

    @Getter
    @ImportService(interfaceClass = AsyncAfentiCacheService.class)
    private AsyncAfentiCacheService asyncAfentiCacheService;
}
