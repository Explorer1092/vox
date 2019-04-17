package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DateRangeUnit;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType.school;

/**
 * 排行榜点赞汇总,区每周第一天作为本周点赞汇总标识
 *
 * @author peng.zhang.a
 * @since 16-7-27
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 14)
public class AfentiRankLikedSummaryCacheManager extends PojoCacheObject<AfentiRankLikedSummaryCacheManager.GenerateKey, Map<Long, Integer>> {

    final int RETRY_TIMES = 3;

    public AfentiRankLikedSummaryCacheManager(UtopiaCache cache) {
        super(cache);
    }

    /**
     * 获取本周第一天作为时间标识，只能点赞本周的排行榜
     */
    public void addLiked(AfentiRankType afentiRankType, StudentDetail likedUser) {
        if (likedUser == null || afentiRankType == null) {
            return;
        }
        Long schoolId = likedUser.getClazz() != null && afentiRankType == school ? likedUser.getClazz().getSchoolId() : 0;
        Date date = getWeekOfFirstDay(new Date());
        GenerateKey cacheKeyObject = new GenerateKey(date, schoolId, afentiRankType);
        String cacheKey = cacheKey(cacheKeyObject);

        CacheObject<Map<Long, Integer>> cacheObject = getCache().get(cacheKey(cacheKeyObject));
        if (cacheKeyObject == null) return;

        if (cacheObject.getValue() != null) {
            getCache().cas(cacheKey, expirationInSeconds(), cacheObject, RETRY_TIMES, currentValue -> {
                int num = currentValue.getOrDefault(likedUser.getId(), 0);
                currentValue.put(likedUser.getId(), num + 1);
                return currentValue;
            });
        } else {
            Map<Long, Integer> rankMap = new HashMap<>();
            rankMap.put(likedUser.getId(), 1);
            add(cacheKeyObject, rankMap);
        }
    }

    /**
     * @param date 　日期　ps:会将日期转换为日期当周　第一天作为key
     * @Param studentDetail 用户信息
     */
    public Map<Long, Integer> loadSchoolRank(StudentDetail studentDetail, Date date) {
        Long schoolId = studentDetail != null && studentDetail.getClazz() != null ? studentDetail.getClazz().getSchoolId() : 0;
        date = getWeekOfFirstDay(date);
        return load(new GenerateKey(date, schoolId, AfentiRankType.school));
    }

    public Map<Long, Integer> loadNationRank(Date date) {
        date = getWeekOfFirstDay(date);
        return load(new GenerateKey(date, 0L, AfentiRankType.national));
    }

    private Date getWeekOfFirstDay(Date date) {
        if (date == null) {
            date = new Date();
        }
        return DateRange.newInstance(date.getTime(), DateRangeUnit.WEEK).getStartDate();
    }

    @Override
    public int expirationInSeconds() {
        return 86400 * 14;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"weekOfFirstDate", "schoolId", "afentiRankType"})
    class GenerateKey {
        private Date weekOfFirstDate;
        private Long schoolId;
        private AfentiRankType afentiRankType;

        @Override
        public String toString() {
            return "WFDATE=" + DateUtils.dateToString(weekOfFirstDate, DateUtils.FORMAT_SQL_DATE)
                    + ",SCHOOLID=" + schoolId
                    + ",RANKTYPE=" + afentiRankType;
        }
    }
}
