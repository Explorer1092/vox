package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;

import java.util.concurrent.TimeUnit;

/**
 * TODO: 2017年2月12日之后核实没有使用后请删除
 *
 * @deprecated use {@link ZonePhotoService} instead.
 */
@Deprecated
@ServiceVersion(version = "2016.12.29")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ZoneCacheService {

    @Async
    AlpsFuture<Boolean> alreadyUploaded(Long studentId, Long clazzId);

    @NoResponseWait
    void photoUploaded(Long studentId, Long clazzId);
}
