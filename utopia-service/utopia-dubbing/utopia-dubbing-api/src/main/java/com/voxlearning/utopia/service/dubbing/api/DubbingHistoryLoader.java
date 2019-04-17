package com.voxlearning.utopia.service.dubbing.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingHistory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2017-8-23
 */
@ServiceVersion(version = "3.0")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DubbingHistoryLoader extends IPingable {


    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    List<DubbingHistory> getDubbingHistoryByUserId(@CacheParameter(value = "UID") Long userId);


    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    List<DubbingHistory> getDubbingHistoryByClazzIdAndDubbingId(@CacheParameter(value = "CID") Long clazzId,
                                                                @CacheParameter(value = "DID") String dubbingId);

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    Long getDubbingHistoryCountByUserIdAndDubbingId(@CacheParameter(value = "UID") Long userId,
                                                    @CacheParameter(value = "DID") String dubbingId);

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    Map<String, Long> getDubbingHistoryCountByUserIdAndDubbingIds(@CacheParameter(value = "UID") Long userId, @CacheParameter(value = "DID", multiple = true) Collection<String> dubbingIds);

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    Long getDubbingHistoryCountByUserId(@CacheParameter(value = "UID_COUNT") Long userId);

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    DubbingHistory getDubbingHistoryById(@CacheParameter String id);

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    Map<String, DubbingHistory> getDubbingHistoriesByIds(@CacheParameter(multiple = true) Collection<String> ids);

    @Deprecated
    @ServiceMethod(timeout = 3, unit = TimeUnit.MINUTES)
    List<DubbingHistory> jobQueryBySecondary();

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    Map<String, Integer> getDubbingHistoryCountByUserIdAndCategoryIds(@CacheParameter(value = "UID") Long userId, @CacheParameter(value = "CAID", multiple = true) Collection<String> categoryIds);

    @CacheMethod(type = DubbingHistory.class, writeCache = false)
    DubbingHistory getDubbingHistoryByHomeworkId(@CacheParameter(value = "UID") Long userId, @CacheParameter(value = "DID") String dubbingId, @CacheParameter(value = "HID") String homeworkId);
}
