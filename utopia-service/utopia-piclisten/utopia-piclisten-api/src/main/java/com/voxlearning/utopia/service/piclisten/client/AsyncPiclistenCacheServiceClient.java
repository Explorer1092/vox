package com.voxlearning.utopia.service.piclisten.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.piclisten.api.AsyncPiclistenCacheService;
import lombok.Getter;

public class AsyncPiclistenCacheServiceClient {

    @Getter
    @ImportService(interfaceClass = AsyncPiclistenCacheService.class)
    private AsyncPiclistenCacheService asyncPiclistenCacheService;
}
