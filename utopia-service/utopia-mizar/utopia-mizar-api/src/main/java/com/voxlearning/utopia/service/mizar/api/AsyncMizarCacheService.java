package com.voxlearning.utopia.service.mizar.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.03.07")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncMizarCacheService {

    // ========================================================================
    // CodecSaltCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> CodecSaltCacheManager_setCode(String name);

    @Async
    AlpsFuture<String> CodecSaltCacheManager_getCode(String name);

    // ========================================================================
    // MizarCourseReadCountManager
    // ========================================================================

    @Async
    AlpsFuture<Long> MizarCourseReadCountManager_increaseCount(String courseId);

    @Async
    AlpsFuture<Long> MizarCourseReadCountManager_loadReadCount(String courseId);

    // ========================================================================
    // MizarLikeShopMonthCountManager
    // ========================================================================

    @Async
    AlpsFuture<Long> MizarLikeShopMonthCountManager_increaseCount(Long parentId);

    @Async
    AlpsFuture<Long> MizarLikeShopMonthCountManager_loadLikeCount(Long parentId);

    // ========================================================================
    // MizarUserSessionManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> MizarUserSessionManager_addUserSessionAttribute(String userId, String attrKey, Object attrValue);

    @Async
    AlpsFuture<Object> MizarUserSessionManager_getUserSessionAttribute(String userId, String attrKey);

    @Async
    AlpsFuture<Boolean> MizarUserSessionManager_removeUserSession(String userId);
}
