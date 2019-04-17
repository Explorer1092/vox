package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.Date;

/**
 * 磨耳朵活动缓存
 *
 * @author jiangpeng
 * @since 2016-10-26 下午1:13
 **/
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudentGrindEarDayRecordCacheManager extends PojoCacheObject<String, String> {

    public StudentGrindEarDayRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Boolean hasRecord(Long studentId, Date date){
        String key = cacheKey(studentId + "-" +DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE));
        String value = load(key);
        return value != null;
    }

    //这个日期的当天是否记录过
    public void todayRecord(Long studentId, Date date){
        String key = cacheKey(studentId + "-" +DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE));
        set(key, "1");
    }

}
