package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.zone.data.SignInContext;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.01.23")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ZoneSignInService {

    @Async
    AlpsFuture<Boolean> checkSignIn(SignInContext context);

    @Async
    AlpsFuture<Boolean> finishSignIn(SignInContext context);
}
