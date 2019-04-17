package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkLivecastService;

/**
 * @author xuesong.zhang
 * @since 2016/9/12
 */
public class NewHomeworkLivecastServiceClient {

    @ImportService(interfaceClass = NewHomeworkLivecastService.class)
    private NewHomeworkLivecastService remoteReference;
}
