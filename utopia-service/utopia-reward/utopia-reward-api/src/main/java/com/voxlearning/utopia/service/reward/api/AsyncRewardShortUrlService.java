package com.voxlearning.utopia.service.reward.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2016.03.07")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncRewardShortUrlService {

    @Async
    AlpsFuture<String> dwzTinyUrl(String longUrl);

    @Async
    AlpsFuture<String> i7TinyUrl(String longUrl);
}
