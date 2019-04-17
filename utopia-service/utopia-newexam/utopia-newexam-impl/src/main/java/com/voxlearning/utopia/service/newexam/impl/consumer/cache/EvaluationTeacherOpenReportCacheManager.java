package com.voxlearning.utopia.service.newexam.impl.consumer.cache;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.data.SchoolYear;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@UtopiaCacheRevision("20180412")
public class EvaluationTeacherOpenReportCacheManager extends PojoCacheObject<String, Set<Long>> {
    public EvaluationTeacherOpenReportCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String examId) {
        return new CacheKey(examId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String examId;

        @Override
        public String toString() {
            return "EVALUATION_REPORT_TEACHER_OPEN_" + examId;
        }
    }

    @Override
    public int expirationInSeconds() {
        SchoolYear schoolYear = SchoolYear.newInstance();
        DateRange dateRange = schoolYear.currentTermDateRange();
        Date currentDate = new Date();
        return (int) ((dateRange.getEndTime() - currentDate.getTime()) / 1000);
    }
}
