package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Ruib
 * @since 2016/8/27
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiLoginCacheManager extends PojoCacheObject<AfentiLoginCacheManager.StudentWithSubject, String> {

    public AfentiLoginCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean notified(Long studentId, Subject subject) {
        return null != studentId && !add(new StudentWithSubject(studentId, subject), "dummy");
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
