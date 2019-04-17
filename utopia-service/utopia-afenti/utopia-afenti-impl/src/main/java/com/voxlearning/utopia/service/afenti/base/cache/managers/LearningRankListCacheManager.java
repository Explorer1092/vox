package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 总的排行榜单信息
 * 如果排行榜为空则默然缓存保存1个小时
 *
 * @author peng.zhang.a
 * @since 16-7-27
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class LearningRankListCacheManager extends PojoCacheObject<LearningRankListCacheManager.GenerateKey, List<Map<String, Object>>> {

    private static final int NULL_VALUE_EXPIRATION_SECONDS = 60 * 60;

    public LearningRankListCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void addNationalRank(List<Map<String, Object>> rankList, Subject subject, Date calculateDate) {
        addRank(rankList, AfentiRankType.national, 0L, subject, calculateDate, expirationInSeconds());
    }

    public void addSchoolRank(List<Map<String, Object>> rankList, Long schoolId, Subject subject, Date calculateDate) {
        addRank(rankList, AfentiRankType.school, schoolId, subject, calculateDate, expirationInSeconds());
    }

    private void addRank(List<Map<String, Object>> rankList, AfentiRankType afentiRankType, Long school, Subject subject, Date calculateDate, int seconds) {
        if (rankList == null) rankList = new ArrayList<>();
        seconds = CollectionUtils.isEmpty(rankList) ? NULL_VALUE_EXPIRATION_SECONDS : seconds;
        String cacheKey = generateKey(afentiRankType, school, subject, calculateDate);
        getCache().set(cacheKey, seconds, rankList);
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
            return "ART=" + afentiRankType
                    + ",SCHOOLID=" + schoolId
                    + ",DATE=" + DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE)
                    + ",SUBJECT=" + subject;
        }
    }


}