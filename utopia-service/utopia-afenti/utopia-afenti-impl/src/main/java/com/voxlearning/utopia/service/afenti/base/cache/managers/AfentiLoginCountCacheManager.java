package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Ruib
 * @since 2016/8/27
 */
public class AfentiLoginCountCacheManager extends PojoCacheObject<AfentiLoginCountCacheManager.StudentWithSubject, String> {

    public AfentiLoginCountCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public int fetchCurrentCount(Long studentId, Subject subject) {
        if (null == studentId || null == subject) return 0;
        String value = load(new StudentWithSubject(studentId, subject));
        return SafeConverter.toInt(value);
    }

    public void updateCurrentCount(Long studentId, Subject subject, int count) {
        if (studentId == null || subject == null || count <= 0) return;
        cache.set(cacheKey(new StudentWithSubject(studentId, subject)), expirationInSeconds(), String.valueOf(count));
    }

    @Override
    public int expirationInSeconds() {
        return 0; // 不过期
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
