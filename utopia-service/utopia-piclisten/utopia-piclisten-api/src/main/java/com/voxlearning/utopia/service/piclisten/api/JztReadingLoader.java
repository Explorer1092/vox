package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.entity.UserReadingRef;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author malong
 * @since 2016/12/22
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface JztReadingLoader extends IPingable {
    /**
     *  获取绘本和用户关系列表的对应map
     * @param userIds
     */
    @CacheMethod(type = UserReadingRef.class, writeCache = false)
    Map<Long, List<UserReadingRef>> getUserReadingRefsByUserIds(@CacheParameter(value = "UID", multiple = true) Collection<Long> userIds);

    @CacheMethod(type = UserReadingRef.class, writeCache = false)
    List<UserReadingRef> getUserReadingRefsByUserId(@CacheParameter(value = "UID") Long userId);
}
