package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 奖励弹窗通知
 * @author yulong.ma
 * @date 2018-11-20 22:26
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_activity_reward_notice_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181121")
public class ClazzCircleRewardNotice implements Serializable {
    private static final long serialVersionUID = -463601911673480678L;

    /**
     * activityId_userId_rewardType;
     */
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private Integer activityId;

    private Long userId;

    private Integer rewardType;

    private Boolean isShow;

    public static String generateId(Integer activityId,Long userId,Integer rewardType) {
        return activityId +  "_" + userId+"_"+rewardType+"_"+RandomUtils.nextObjectId();
    }
    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ClazzCircleRewardNotice.class, new String[]{"activityId"}, new Object[]{activityId});
    }

    public static String cacheKeyFromClazzIdAndUserId(Integer activityId,Long userId) {
        return CacheKeyGenerator.generateCacheKey(ClazzCircleRewardNotice.class, new String[]{"activityId", "userId"}, new Object[]{activityId,userId});
    }
}
