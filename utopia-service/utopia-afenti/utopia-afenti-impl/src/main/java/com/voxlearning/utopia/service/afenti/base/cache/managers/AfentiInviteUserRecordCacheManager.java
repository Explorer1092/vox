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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * 记录用户当天的邀请列表，每个用户只能当天被邀请一次
 *
 * @author peng.zhang.a
 * @since 16-8-8
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiInviteUserRecordCacheManager extends PojoCacheObject<AfentiInviteUserRecordCacheManager.GenerateKey, Set<Long>> {

    public AfentiInviteUserRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void setRecord(Long sendInvitationUserId, Long invitedUserId, Subject subject) {
        if (sendInvitationUserId == null || invitedUserId == null || !AVAILABLE_SUBJECT.contains(subject)) return;

        String cacheKey = cacheKey(new GenerateKey(sendInvitationUserId, subject));
        CacheObject<Set<Long>> cacheObject = cache.get(cacheKey);
        if (cacheObject == null) return;

        if (cacheObject.getValue() == null) {
            getCache().add(cacheKey, expirationInSeconds(), Collections.singleton(invitedUserId));
        } else {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(invitedUserId);
                return currentValue;
            });
        }
    }

    public Set<Long> loadRecord(Long sendInvitationUserId, Subject subject) {
        Set<Long> set = load(new GenerateKey(sendInvitationUserId, subject));
        return CollectionUtils.isNotEmpty(set) ? set : Collections.emptySet();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "subject"})
    class GenerateKey {
        private Long userId;
        private Subject subject;

        @Override
        public String toString() {
            return "UID=" + userId + ",SUBJECT=" + subject;
        }
    }


}