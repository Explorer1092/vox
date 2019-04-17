package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

@UtopiaCacheExpiration(30 * 24 * 3600)
public class ActivityHomeworkParentRewardCacheManager extends PojoCacheObject<String, Long> {

    public ActivityHomeworkParentRewardCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getKidsDayCacheKey(String homeworkId, Long studentId) {
        return "KIDS_DAY_PARENT_REWARD_" + homeworkId + "_" + studentId;
    }
}
