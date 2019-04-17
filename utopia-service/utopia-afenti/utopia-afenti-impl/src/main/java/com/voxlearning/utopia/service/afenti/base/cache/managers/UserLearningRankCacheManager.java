package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * 用户个人榜单排名信息
 * 每次排行榜有变化都需要重新加载写入
 *
 * @author peng.zhang.a
 * @since 16-7-27
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 14)
public class UserLearningRankCacheManager extends PojoCacheObject<UserLearningRankCacheManager.GenerateKey, Map<Long, Integer>> {


    public UserLearningRankCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void setNationalRank(Subject subject, Date date, Map<Long, Integer> list) {
        set(new GenerateKey(AfentiRankType.national, 0L, subject, date), list);
    }

    public void setSchoolRank(Subject subject, Long schoolId, Date date, Map<Long, Integer> list) {
        set(new GenerateKey(AfentiRankType.school, schoolId, subject, date), list);
    }

    public Date lastWeekCalculateDate() {
        return WeekRange.current().getStartDate();
    }

    public String generateKey(AfentiRankType afentiRankType, Long schoolId, Subject subject, Date date) {
        if (afentiRankType == AfentiRankType.national && schoolId != 0) schoolId = 0L;
        return cacheKey(new GenerateKey(afentiRankType, schoolId, subject, date));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"afentiRankType", "schoolId", "subject", "date"})
    class GenerateKey {
        private AfentiRankType afentiRankType;
        private Long schoolId;
        private Subject subject;
        private Date date;

        @Override
        public String toString() {
            return "RANKTYPE=" + afentiRankType
                    + ",SCHOOLID=" + schoolId
                    + ",DATE=" + DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE)
                    + ",SUBJECT=" + subject;
        }
    }
}
