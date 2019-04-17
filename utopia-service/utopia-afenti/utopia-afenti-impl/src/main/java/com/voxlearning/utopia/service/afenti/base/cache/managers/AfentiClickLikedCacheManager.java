package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 标记用户点赞，用于排重，避免当天多次点赞
 *
 * @author peng.zhang.a
 * @since 16-7-27
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiClickLikedCacheManager extends PojoCacheObject<AfentiClickLikedCacheManager.GenerateClickLikedKey, Set<Long>> {


    public AfentiClickLikedCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void clickLiked(StudentDetail clickUser, StudentDetail likedUser, Subject subject, AfentiRankType afentiRankType) {
        if (clickUser == null || likedUser == null || subject == null || afentiRankType == null) {
            return;
        }
        String cacheKey = cacheKey(new AfentiClickLikedCacheManager.GenerateClickLikedKey(clickUser.getId(), subject, afentiRankType));
        CacheObject<Set<Long>> cacheObject = getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() == null) {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singleton(likedUser.getId()));
        } else if (cacheObject != null) {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(likedUser.getId());
                return currentValue;
            });
        }
    }

    public Set<Long> loadTodayClickLikedSet(StudentDetail clickUser, Subject subject, AfentiRankType afentiRankType) {
        if (clickUser == null || subject == null || afentiRankType == null) {
            return new HashSet<>();
        }
        return load(new AfentiClickLikedCacheManager.GenerateClickLikedKey(clickUser.getId(), subject, afentiRankType));

    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "subject", "afentiRankType"})
    class GenerateClickLikedKey {
        private Long userId;
        private Subject subject;
        private AfentiRankType afentiRankType;

        @Override
        public String toString() {
            return "UID=" + userId
                    + ",SUBJECT=" + subject
                    + ",RANKTYPE=" + afentiRankType;
        }
    }


}

