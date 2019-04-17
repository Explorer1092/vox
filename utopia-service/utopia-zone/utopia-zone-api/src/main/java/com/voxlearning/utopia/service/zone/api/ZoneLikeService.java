package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2016.12.29")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ZoneLikeService {

    @Async
    AlpsFuture<Boolean> increaseLikedCount(Long userId);

    @Idempotent
    Map<Long, Long> loadLikedCounts(Collection<Long> userIds);

    // ========================================================================
    // DailyLikeOrCommentCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> record(Long studentId);

    @Async
    AlpsFuture<Boolean> sent(Long studentId);

    // ========================================================================
    // DailyNewLikeOrCommentReceivedManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> show(Long studentId);

    @Async
    AlpsFuture<Boolean> turnOn(Long studentId);

    @Async
    AlpsFuture<Boolean> turnOff(Long studentId);
}
