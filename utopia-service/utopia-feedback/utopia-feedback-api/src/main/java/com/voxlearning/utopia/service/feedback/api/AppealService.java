package com.voxlearning.utopia.service.feedback.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.misc.UserAppeal;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.02.12")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AppealService {

    @Async
    AlpsFuture<MapMessage> saveUserAppeal(UserAppeal appeal);
}
