package com.voxlearning.utopia.service.vendor.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.Unknown;

/**
 * @author Ruib
 * @since 2016/11/30
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 3)
public class ParentFairylandClassmatesUsageCacheManager extends
        PojoCacheObject<ParentFairylandClassmatesUsageCacheManager.ClazzWithAppKey, Map<Long, String>> {

    public ParentFairylandClassmatesUsageCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Map<Long, String> fetch(Long clazzId, OrderProductServiceType appKey) {
        if (clazzId == null || appKey == null || appKey == Unknown) return Collections.emptyMap();

        String date = DateUtils.dateToString(DayRange.current().previous().getStartDate(), DateUtils.FORMAT_SQL_DATE);
        if (RuntimeMode.current().le(Mode.STAGING)) {
            date = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        }
        Map<Long, String> cached = load(new ClazzWithAppKey(clazzId, appKey, date));
        return cached == null ? new HashMap<>() : cached;
    }

    public void record(Long clazzId, OrderProductServiceType appKey, Long studentId, String content) {
        if (clazzId == null || studentId == null || StringUtils.isEmpty(content) || appKey == null || appKey == Unknown)
            return;

        String date = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        String key = cacheKey(new ClazzWithAppKey(clazzId, appKey, date));

        CacheObject<Map<Long, String>> cacheObject = getCache().get(key);
        if (null == cacheObject.getValue()) {
            Map<Long, String> map = new HashMap<>();
            map.put(studentId, content);
            cache.add(key, expirationInSeconds(), map);
        } else {
            Map<Long, String> map = cacheObject.getValue();
            if (map.size() >= 5 || map.containsKey(studentId)) return;
            cache.cas(key, expirationInSeconds(), cacheObject, 3, currentValue -> {
                currentValue = new HashMap<>(currentValue);
                currentValue.put(studentId, content);
                return currentValue;
            });
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"clazzId", "appKey", "date"})
    class ClazzWithAppKey {
        public Long clazzId;
        public OrderProductServiceType appKey;
        public String date; // yyyy-MM-dd

        @Override
        public String toString() {
            return "C=" + clazzId + ",AK=" + appKey.name() + ",D=" + date;
        }
    }
}
