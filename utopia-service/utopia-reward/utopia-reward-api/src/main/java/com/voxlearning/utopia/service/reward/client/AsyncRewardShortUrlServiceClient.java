package com.voxlearning.utopia.service.reward.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.reward.api.AsyncRewardShortUrlService;
import lombok.Getter;

public class AsyncRewardShortUrlServiceClient {

    @Getter
    @ImportService(interfaceClass = AsyncRewardShortUrlService.class)
    private AsyncRewardShortUrlService asyncRewardShortUrlService;
}
