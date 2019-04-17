package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/25
 */
public class AfentiPromptCacheManager extends PojoCacheObject<AfentiPromptCacheManager.StudentWithSubject, Map<AfentiPromptType, Boolean>> {

    public AfentiPromptCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long studentId, Subject subject, AfentiPromptType type) {
        if (null == studentId || null == type) return;
        StudentWithSubject key = new StudentWithSubject(studentId, subject);
        Map<AfentiPromptType, Boolean> value = load(key);
        if (null == value) {
            Map<AfentiPromptType, Boolean> prompt = init();
            prompt.put(type, true);
            add(key, prompt);
        } else if (!Boolean.TRUE.equals(value.get(type))) {
            value.put(type, true);
            set(key, value);
        }
    }

    public void record(Collection<Long> userIds, Subject subject, AfentiPromptType type) {
        if (CollectionUtils.isEmpty(userIds) || subject == null || type == null) return;
        Set<String> keys = userIds.stream().map(id -> cacheKey(new StudentWithSubject(id, subject))).collect(Collectors.toSet());
        Map<String, CacheObject<Map<AfentiPromptType, Boolean>>> cached = cache.gets(keys);
        for (Long userId : userIds) {
            String key = cacheKey(new StudentWithSubject(userId, subject));
            CacheObject<Map<AfentiPromptType, Boolean>> cacheObject = cached.getOrDefault(key, null);
            if (null == cacheObject) continue;
            if (null == cacheObject.getValue()) {
                Map<AfentiPromptType, Boolean> prompt = init();
                prompt.put(type, true);
                cache.add(key, expirationInSeconds(), prompt);
            } else {
                Map<AfentiPromptType, Boolean> value = cacheObject.getValue();
                if (!Boolean.TRUE.equals(value.get(type))) {
                    value.put(type, true);
                    cache.set(key, expirationInSeconds(), value);
                }
            }
        }
    }

    public void reset(Long studentId, Subject subject, AfentiPromptType type) {
        if (null == studentId || null == type) return;
        StudentWithSubject key = new StudentWithSubject(studentId, subject);
        Map<AfentiPromptType, Boolean> value = load(key);
        if (value != null && Boolean.TRUE.equals(value.get(type))) {
            value.put(type, false);
            set(key, value);
        }
    }

    public Map<AfentiPromptType, Boolean> fetch(Long studentId, Subject subject) {
        if (null == studentId) return init();
        StudentWithSubject key = new StudentWithSubject(studentId, subject);
        Map<AfentiPromptType, Boolean> result = load(key);
        return null == result ? init() : result;
    }

    @Override
    public int expirationInSeconds() {
        return 0; // 不过期
    }

    private Map<AfentiPromptType, Boolean> init() {
        Map<AfentiPromptType, Boolean> prompt = new LinkedHashMap<>();
        for (AfentiPromptType type : AfentiPromptType.values()) {
            if (prompt.containsKey(type)) continue;
            prompt.put(type, false);
        }
        return prompt;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"studentId", "subject"})
    public static class StudentWithSubject {
        public Long studentId;
        public Subject subject;

        @Override
        public String toString() {
            return "U=" + studentId + ",S=" + subject.name();
        }
    }
}
