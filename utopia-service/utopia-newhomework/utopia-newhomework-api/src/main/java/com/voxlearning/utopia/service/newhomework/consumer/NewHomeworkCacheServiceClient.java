package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkCacheService;
import lombok.Getter;

/**
 * @author xuesong.zhang
 * @since 2017/6/6
 */
public class NewHomeworkCacheServiceClient {

    @Getter
    @ImportService(interfaceClass = NewHomeworkCacheService.class)
    private NewHomeworkCacheService newHomeworkCacheService;
}
