package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;

/**
 * @author xuesong.zhang
 * @since 2016/9/12
 */
public class NewHomeworkLivecastLoaderClient {

    @ImportService(interfaceClass = NewHomeworkLivecastLoader.class)
    private NewHomeworkLivecastLoader remoteReference;

}
