package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.02.06")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ZonePhotoService {

    @Async
    AlpsFuture<Boolean> photoUploaded(Long studentId, Long clazzId);

    @Async
    AlpsFuture<Boolean> alreadyUploaded(Long studentId, Long clazzId);
}
