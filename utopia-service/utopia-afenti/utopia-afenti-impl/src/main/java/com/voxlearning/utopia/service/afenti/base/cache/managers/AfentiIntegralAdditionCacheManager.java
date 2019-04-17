package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * unflushable
 *
 * @author Ruib
 * @since 2016/11/25
 */
public class AfentiIntegralAdditionCacheManager extends PojoCacheObject<AfentiIntegralAdditionCacheManager.StudentWithSubject, String> {

    public AfentiIntegralAdditionCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean popup(Long studentId, Subject subject) {
        return null != studentId && add(new StudentWithSubject(studentId, subject), "dummy");
    }

    @Override
    public int expirationInSeconds() {
        return (int) (DateUtils.stringToDate("2016-11-30 23:59:59").getTime() / 1000);
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
