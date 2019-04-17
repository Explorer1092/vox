package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;

import java.util.Arrays;
import java.util.Date;

/**
 * @author tanguohong
 * @since 2016/11/30
 */
public class VacationHomeworkIntegralCacheManager extends PojoCacheObject<String, String> {
    public VacationHomeworkIntegralCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean record(Long teacherId) {
        if (teacherId == null) {
            return false;
        }
        String cacheKey = cacheKey(teacherId.toString());
        return cache.add(cacheKey, expirationInSeconds(), "dummy");
    }

    public void recordStudentReward(Long studentId, String vacationHomeworkId, Integer level) {
        String key = StringUtils.join(Arrays.asList(vacationHomeworkId, studentId, level), ":");
        String cacheKey = cacheKey(key);
        cache.add(cacheKey, expirationInSeconds(), DateUtils.dateToString(new Date()));
    }

    public boolean studentRewarded(Long studentId, String vacationHomeworkId, Integer level) {
        String key = StringUtils.join(Arrays.asList(vacationHomeworkId, studentId, level), ":");
        return load(key) != null;
    }

    @Override
    public int expirationInSeconds() {
        return (int) (NewHomeworkConstants.VH_OFFLINE_DATE.getTime() / 1000);
    }
}
