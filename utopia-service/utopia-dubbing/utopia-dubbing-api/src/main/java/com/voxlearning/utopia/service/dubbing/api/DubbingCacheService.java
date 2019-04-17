package com.voxlearning.utopia.service.dubbing.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2017-8-25
 */
@ServiceVersion(version = "20180131")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface DubbingCacheService extends IPingable {
    Long addDubbingUserCount(String categoryId);

    Long loadDubbingUserCount(String categoryId);

    Map<String, Long> loadDubbingUserCounts(Collection<String> categoryIds);

    void addWeekRank(int week, String dubbingId, String historyId);

    Set<String> loadWeekRank(int week, String dubbingId);

    Long addUserDubbingCountInCategory(Long userId, String categoryId);

    Map<String, Long> loadUserDubbingCountInCategories(Long userId, Collection<String> categoryIds);

    Long decrUserDubbingCountInCategory(Long userId, String categoryId);

    //临时修补category人数
    void addDubbingUserCountTemp(String categoryId, Long count);

}
