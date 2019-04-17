package com.voxlearning.utopia.service.vendor.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.vendor.api.AsyncVendorCacheService;
import lombok.Getter;

public class AsyncVendorCacheServiceClient {

    @Getter
    @ImportService(interfaceClass = AsyncVendorCacheService.class)
    private AsyncVendorCacheService asyncVendorCacheService;
}
