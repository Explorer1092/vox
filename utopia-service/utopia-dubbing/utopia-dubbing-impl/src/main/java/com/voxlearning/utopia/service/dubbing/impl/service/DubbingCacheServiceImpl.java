package com.voxlearning.utopia.service.dubbing.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.dubbing.api.DubbingCacheService;
import com.voxlearning.utopia.service.dubbing.impl.support.DubbingCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author shiwei.liao
 * @since 2017-8-25
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = DubbingCacheService.class, version = @ServiceVersion(version = "20180131")),
        @ExposeService(interfaceClass = DubbingCacheService.class, version = @ServiceVersion(version = "20171120")),
})
public class DubbingCacheServiceImpl implements DubbingCacheService {
    @Inject
    private DubbingCacheSystem dubbingCacheSystem;

    @Override
    public Long addDubbingUserCount(String categoryId) {
        if (StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        return dubbingCacheSystem.getDubbingPersistenceCache().incrDubbingUserCount(categoryId);
    }

    @Override
    public Long loadDubbingUserCount(String categoryId) {
        if (StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        return dubbingCacheSystem.getDubbingPersistenceCache().loadDubbingUserCount(categoryId);
    }

    @Override
    public Map<String, Long> loadDubbingUserCounts(Collection<String> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        Map<String, Long> countMap = new HashMap<>();
        categoryIds.forEach(id -> {
            Long count = dubbingCacheSystem.getDubbingPersistenceCache().loadDubbingUserCount(id);
            countMap.put(id, SafeConverter.toLong(count));
        });
        return countMap;
    }

    @Override
    public void addWeekRank(int week, String dubbingId, String historyId) {
        if (StringUtils.isBlank(historyId) || StringUtils.isBlank(dubbingId) || week < 1) {
            return;
        }
        dubbingCacheSystem.getDubbingPersistenceCache().addWeekRank(week, dubbingId, historyId);
    }

    @Override
    public Set<String> loadWeekRank(int week, String dubbingId) {
        if (StringUtils.isBlank(dubbingId) || week < 1) {
            return Collections.emptySet();
        }
        return dubbingCacheSystem.getDubbingPersistenceCache().loadWeekRank(week, dubbingId);
    }

    @Override
    public Long addUserDubbingCountInCategory(Long userId, String categoryId) {
        if (userId == null || StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        return dubbingCacheSystem.getDubbingPersistenceCache().addUserDubbingCountInCategory(userId, categoryId);
    }

    @Override
    public Map<String, Long> loadUserDubbingCountInCategories(Long userId, Collection<String> categoryIds) {
        if (userId == null || CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        Map<String, Long> countMap = new HashMap<>();
        categoryIds.forEach(id -> {
            Long count = dubbingCacheSystem.getDubbingPersistenceCache().loadUserDubbingCountInCategory(userId, id);
            countMap.put(id, SafeConverter.toLong(count));
        });
        return countMap;
    }

    @Override
    public Long decrUserDubbingCountInCategory(Long userId, String categoryId) {
        if (userId == null || StringUtils.isBlank(categoryId)) {
            return 0L;
        }
        return dubbingCacheSystem.getDubbingPersistenceCache().decrUserDubbingCountInCategory(userId, categoryId);
    }

    @Override
    public void addDubbingUserCountTemp(String categoryId, Long count) {
        if (StringUtils.isBlank(categoryId)) {
            return;
        }
        dubbingCacheSystem.getDubbingPersistenceCache().addDubbingUserCountTemp(categoryId, count);
    }
}
