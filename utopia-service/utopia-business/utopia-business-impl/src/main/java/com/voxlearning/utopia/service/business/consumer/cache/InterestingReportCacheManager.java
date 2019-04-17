package com.voxlearning.utopia.service.business.consumer.cache;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * Created by Summer on 2016/12/20.
 * 年度趣味报告领取礼包记录
 * @deprecated 代码根本就已经过期了
 */
@Deprecated
public class InterestingReportCacheManager extends PojoCacheObject<Long, String> {
    public InterestingReportCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long userId) {
        if (userId == null) {
            return;
        }
        set(userId, "Real_Madrid");
    }

    public boolean done(Long userId) {
        return userId == null || load(userId) != null;
    }


    @Override
    public int expirationInSeconds() {
        return (int) (DateUtils.stringToDate("2017-01-20 00:00:00").getTime() / 1000);
    }
}
