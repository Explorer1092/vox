package com.voxlearning.utopia.service.afenti.base.cache.managers.activity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.base.cache.managers.CacheBaseManager;
import com.voxlearning.utopia.service.afenti.base.cache.managers.activity.AfentiUserLoginRewardCacheManager.GenerateKey;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * 阿分题登陆奖励缓存
 *
 * @author peng.zhang.a
 * @since 16-9-1
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class AfentiUserLoginRewardCacheManager extends CacheBaseManager<GenerateKey, Set<Integer>, Integer> {


    public AfentiUserLoginRewardCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean addRecord(StudentDetail studentDetail, Subject subject) {
        if (studentDetail == null || subject == null) return false;
        String cacheKey = cacheKey(new GenerateKey(studentDetail.getId(), subject));
        Date startDate = WeekRange.current().getStartDate();
        Integer weekDayNum = (int) DateUtils.dayDiff(new Date(), startDate) + 1;
        return casAddSet(cacheKey, weekDayNum);
    }

    public boolean addTestRecord(StudentDetail studentDetail, Subject subject,int weekDayNum) {
        if (studentDetail == null || subject == null) return false;
        String cacheKey = cacheKey(new GenerateKey(studentDetail.getId(), subject));
        return casAddSet(cacheKey, weekDayNum);
    }

    public boolean existRecord(StudentDetail studentDetail, Subject subject) {
        if (studentDetail == null || subject == null) return false;

        Date startDate = WeekRange.current().getStartDate();
        Integer weekDayNum = (int) DateUtils.dayDiff(new Date(), startDate) + 1;
        Set<Integer> records = loadRecords(studentDetail, subject);
        if (CollectionUtils.isEmpty(records) || !records.contains(weekDayNum)) {
            return false;
        }
        return true;
    }

    public Set<Integer> loadRecords(StudentDetail studentDetail, Subject subject) {
        if (studentDetail == null || subject == null) return Collections.emptySet();
        Set<Integer> result = load(new GenerateKey(studentDetail.getId(), subject));
        if (result == null) result = Collections.emptySet();
        return result;

    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"userId", "subject"})
    class GenerateKey {
        private Long userId;
        private Subject subject;

        @Override
        public String toString() {
            return "UID=" + userId + ",SUBJECT=" + subject;
        }
    }
}
