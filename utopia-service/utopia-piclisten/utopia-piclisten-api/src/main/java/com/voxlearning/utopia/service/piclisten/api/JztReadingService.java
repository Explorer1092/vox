package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.entity.UserReadingRef;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author malong
 * @since 2016/12/22
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface JztReadingService extends IPingable {
    /**
     * 记录在读绘本
     */
    MapMessage upsertUserReadingRef(UserReadingRef userReadingRef);

    /**
     * 删除在读绘本
     */
    MapMessage deleteUserReadingRefs(Long userId, Set<String> pictureBookIds);

}
