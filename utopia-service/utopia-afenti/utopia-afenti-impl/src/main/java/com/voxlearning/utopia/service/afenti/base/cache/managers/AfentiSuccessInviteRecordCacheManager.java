package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 邀请同班同学同学成功缓存记录
 *
 * @author peng.zhang.a
 * @since 16-7-24
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 14)
public class AfentiSuccessInviteRecordCacheManager extends PojoCacheObject<AfentiSuccessInviteRecordCacheManager.GenerateKey, Set<Long>> {

    public AfentiSuccessInviteRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }

    /**
     * 增加邀请成功记录
     *
     * @param sendInvitationUserId 邀请用户
     * @param invitedUserId        　被邀请用户
     */
    public void addRecords(List<Long> sendInvitationUserId, Long invitedUserId, Subject subject) {
        Map<String, Long> cacheKeys = sendInvitationUserId.stream().collect(Collectors.toMap(p -> cacheKey(new GenerateKey(p, subject)), t -> t));

        Map<String, CacheObject<Set<Long>>> cacheObjectMap = getCache().gets(cacheKeys.keySet());

        cacheKeys.forEach((cacheKey, userId) -> {
            if (cacheObjectMap.containsKey(cacheKey)
                    || !getCache().add(cacheKey, expirationInSeconds(), Collections.singleton(invitedUserId))) {
                getCache().cas(cacheKey, expirationInSeconds(), cacheObjectMap.get(cacheKey), currentValue -> {
                    currentValue = new HashSet<>(currentValue);
                    currentValue.add(invitedUserId);
                    return currentValue;
                });
            }

        });
    }

    /**
     * 加载邀请其他同学开通成功信息
     */
    public Set<Long> loadAndReset(Long sendInvitationUserId, Subject subject) {
        String cacheKey = cacheKey(new GenerateKey(sendInvitationUserId, subject));
        Set<Long> result = getCache().load(cacheKey);
        if (CollectionUtils.isNotEmpty(result)) {
            getCache().delete(cacheKey);
            return result;
        } else {
            return Collections.emptySet();
        }
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "subject"})
    static class GenerateKey {
        public Long userId;
        public Subject subject;

        @Override
        public String toString() {
            return "UID=" + userId + ",SUBJECT=" + subject;
        }
    }
}
