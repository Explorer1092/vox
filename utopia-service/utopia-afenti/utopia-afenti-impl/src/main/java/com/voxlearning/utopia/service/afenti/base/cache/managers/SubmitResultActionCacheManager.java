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
 * @since 2016/8/25
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class SubmitResultActionCacheManager extends PojoCacheObject<SubmitResultActionCacheManager.StudentWithSubject, String> {

    public SubmitResultActionCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean sended(Long studentId, Subject subject) {
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
