package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.02.19")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ZoneBagService {

    @Async
    AlpsFuture<List<ClazzZoneBag>> findClazzZoneBagList(Long userId);
}
