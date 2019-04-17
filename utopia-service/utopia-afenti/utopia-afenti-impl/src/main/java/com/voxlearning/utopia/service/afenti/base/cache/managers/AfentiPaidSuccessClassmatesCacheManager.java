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
 * 同班同学购买阿分题产品通知<br/>
 * 用户浏览一次就消失
 *
 * @author peng.zhang.a
 * @since 16-7-24
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 14)
public class AfentiPaidSuccessClassmatesCacheManager extends PojoCacheObject<AfentiPaidSuccessClassmatesCacheManager.GenerateKey, Set<Long>> {

    public AfentiPaidSuccessClassmatesCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean addPaidSuccessMsg(Long paySuccessUserId, Long classmateId, Subject subject) {
        if (paySuccessUserId == 0 || classmateId == 0 || subject == null) {
            return true;
        }
        String cacheKey = cacheKey(new GenerateKey(classmateId, subject));
        CacheObject<Set<Long>> cacheObject = getCache().get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() == null) {
            getCache().add(cacheKey, expirationInSeconds(), new HashSet<>(Collections.singletonList(paySuccessUserId)));
        } else if (cacheObject != null) {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(paySuccessUserId);
                return currentValue;
            });
        }
        return true;
    }

    public void addPaidSuccessMsg(Long paySuccessUserId, Collection<Long> classmateIds, Subject subject) {
        if (null == paySuccessUserId || CollectionUtils.isEmpty(classmateIds) || null == subject) return;
        Set<String> keys = classmateIds.stream().map(id -> cacheKey(new GenerateKey(id, subject))).collect(Collectors.toSet());
        Map<String, CacheObject<Set<Long>>> cached = getCache().gets(keys);
        for (Long classmateId : classmateIds) {
            String key = cacheKey(new GenerateKey(classmateId, subject));
            CacheObject<Set<Long>> value = cached.getOrDefault(key, null);
            if (value != null || !cache.add(key, expirationInSeconds(), Collections.singleton(paySuccessUserId))) {
                cache.cas(key, expirationInSeconds(), value, currentValue -> {
                    currentValue = new HashSet<>(currentValue);
                    currentValue.add(paySuccessUserId);

                    return currentValue;
                });
            }
        }
    }

    /**
     * 获取付款的同伴同学列表，只显示一次，读取之后会清除缓存
     */
    public Set<Long> loadPaidClassmateUserIds(Long userId, Subject subject) {
        String cacheKey = cacheKey(new GenerateKey(userId, subject));
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
    public static class GenerateKey {
        public Long userId;
        public Subject subject;

        @Override
        public String toString() {
            return "UID=" + userId + ",SUBJECT=" + subject;
        }
    }
}
