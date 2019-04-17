package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.queue.Message;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2016.12.28")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface ZoneQueueService {

    @NoResponseWait
    void sendMessage(Message message);

}
