package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author janko
 * @date 2016/10/30
 * @desc
 */
@ServiceVersion(version = "20161031")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface StudentAdvertisementInfoLoader {

    @Idempotent
    @CacheMethod(type = StudentAdvertisementInfo.class, writeCache = false)
    List<StudentAdvertisementInfo> loadByUserId(@CacheParameter("UID") Long userId);
}
