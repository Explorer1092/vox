package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @Description: 假期作业：提醒学生进度
 * @author: Mr_VanGogh
 * @date: 2018/11/23 下午2:36
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181123")
public class RemindStudentVacationProgressCacheManager extends PojoCacheObject<String, Integer> {

    public RemindStudentVacationProgressCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String packageId) {
        return new RemindStudentVacationProgressCacheManager.CacheKey(packageId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String packageId;

        @Override
        public String toString() {
            return "REMIND_STUDENT_VACATION_PROGRESS_" + packageId;
        }
    }
}
