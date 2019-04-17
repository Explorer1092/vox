package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ruib
 * @since 2016/8/19
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 7)
public class AfentiKnowledgePointCacheManager extends PojoCacheObject<AfentiKnowledgePointCacheManager.StudentWithSubject, Set<String>> {

    public AfentiKnowledgePointCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean sended(Long studentId, Subject subject, String kp) {
        if (null == studentId || null == subject || StringUtils.isBlank(kp)) return false; // 需要发送
        Set<String> cached = load(new StudentWithSubject(studentId, subject));
        return cached != null && cached.contains(kp);
    }

    public void record(Long studentId, Subject subject, String kp) {
        if (null == studentId || null == subject || StringUtils.isBlank(kp)) return;

        String key = cacheKey(new StudentWithSubject(studentId, subject));
        CacheObject<Set<String>> cacheObject = getCache().get(key);
        if (null != cacheObject.getValue()) {
            cache.cas(key, expirationInSeconds(), cacheObject, 3, currentValue -> {
                currentValue = new HashSet<>(currentValue);
                currentValue.add(kp);
                return currentValue;
            });
        } else {
            cache.add(key, expirationInSeconds(), Collections.singleton(kp));
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"studentId", "subject"})
    class StudentWithSubject {
        public Long studentId;
        public Subject subject;

        @Override
        public String toString() {
            return "S=" + studentId + ",SJ=" + subject.name();
        }
    }
}
