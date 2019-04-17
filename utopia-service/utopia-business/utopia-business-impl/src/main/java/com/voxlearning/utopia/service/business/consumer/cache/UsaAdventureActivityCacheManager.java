package com.voxlearning.utopia.service.business.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.business.api.constant.LearningGoalType;

/**
 * 走遍美国学英语开学拉新活动9月1号到9月31号
 *
 * @author peng.zhang.a
 * @since 16-8-22
 */
@UtopiaCachePrefix(prefix = "USAADVENTURE:ACTIVITY:LEARNINGGOAL")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
public class UsaAdventureActivityCacheManager extends PojoCacheObject<Long, String> {
    public UsaAdventureActivityCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean addRecord(Long userId, LearningGoalType type) {
        return add(userId, type.name());
    }

    public LearningGoalType loadRecord(Long userId) {
        String type = load(userId);
        if (type != null) {
            return LearningGoalType.valueOf(type);
        }
        return null;
    }
}
