package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2016.12.29")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface UserGiftService {

    @Async
    AlpsFuture<Integer> getCurrentWeekReceivedGiftCount(Long userId);
}
