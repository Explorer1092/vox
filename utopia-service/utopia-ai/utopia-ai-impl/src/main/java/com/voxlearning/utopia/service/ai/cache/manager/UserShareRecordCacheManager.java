package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Summer on 2018/8/3
 */
public class UserShareRecordCacheManager extends PojoCacheObject<UserShareRecordCacheManager.GenerateKey, Set<String>> {

    public UserShareRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addRecord(Long userId, String bookId, String unitId) {
        String cacheKey = cacheKey(new UserShareRecordCacheManager.GenerateKey(userId, bookId));
        CacheObject<Set<String>> cacheObject = getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() == null) {
            getCache().add(cacheKey, expirationInSeconds(), new HashSet<>(Collections.singleton(unitId)));
        } else if (cacheObject != null) {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(unitId);
                return currentValue;
            });
        }
    }

    public Set<String> loadUserShareRecords(Long userId, String bookId) {
        String cacheKey = cacheKey(new GenerateKey(userId, bookId));
        Set<String> result = getCache().load(cacheKey);
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        } else {
            return Collections.emptySet();
        }
    }


    @Override
    public int expirationInSeconds() {
        return 60 * 60 * 24 * 30;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "bookId"})
    public static class GenerateKey {
        private Long userId;
        private String bookId;

        @Override
        public String toString() {
            return "UID=" + userId + ";BID=" + bookId;
        }
    }
}
