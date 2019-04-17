package com.voxlearning.utopia.service.vendor.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

/**
 * 学生每日每个自学类型当天是否学习过
 *
 * @author jiangpeng
 * @since 2016-10-20 下午9:08
 **/
@Named
public class MySelfStudyDayRecorder implements InitializingBean {

    private Cache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        cache = CacheSystem.CBS.getCache("persistence");
    }


    public void record(Long studentId, SelfStudyType selfStudyType, Long dataTime) {
        String day = DayRange.newInstance(dataTime).toString();
        String key = CacheKeyGenerator.generateCacheKey("MySelfStudyDayRecorder",
                new String[]{"sid", "selfStudyType", "day"},
                new Object[]{studentId, selfStudyType.name(), day});
        int expiration = DateUtils.getCurrentToDayEndSecond() + 86400;
        cache.incr(key, 1, 1, expiration);
    }

    public Boolean hasRecord(Long studentId, SelfStudyType selfStudyType, Long dataTime) {
        String day = DayRange.newInstance(dataTime).toString();
        String key = CacheKeyGenerator.generateCacheKey("MySelfStudyDayRecorder",
                new String[]{"sid", "selfStudyType", "day"},
                new Object[]{studentId, selfStudyType.name(), day});
        CacheObject<String> objectCacheObject = cache.get(key);
        Long count = SafeConverter.toLong(objectCacheObject.getValue());
        return count >= 1;
    }


}
