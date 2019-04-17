package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 由个人原因发给班级维度奖励提示  （如班级内一人购买物品，给所在的全班给与奖励）
 * @author chensn
 * @date 2018-11-20 22:26
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_clazz_reward_notice_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181121")
public class ZoneClazzRewardNotice implements Serializable {

    private static final long serialVersionUID = -3122347531899116729L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private Integer activityId;

    private Long clazzId;
    /**
     *
     */
    private Long userId;

    /**
     * 自定义奖励 或者 任务，主要为了区别id，各个业务灵活应用
     */
    private Integer rewardType;

    /**
     * 是否领取
     */
    private Boolean isReceived;
    /**
     * 1 小U语文 2小U数学 3小U英语
     */
    private Integer subject;

    /**
     * 是否第一个购买
     */
    private Boolean isFirst;

    private BigDecimal price;

    private Integer period;

    private Boolean isImproved;

    @DocumentCreateTimestamp
    private Date ct;

    public String generateId() {
        return id = activityId + "_" + RandomUtils.nextObjectId();
    }
    public static String cacheKeyFromActivityIdAndClazzId(Integer activityId,Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ZoneClazzRewardNotice.class, new String[]{"activityId","clazzId"}, new Object[]{activityId,clazzId});
    }

    public static String cacheKeyFromUserId(Integer activityId, Long userId) {
        return CacheKeyGenerator.generateCacheKey(ZoneClazzRewardNotice.class, new String[]{"activityId", "userId"}, new Object[]{activityId, userId});
    }

    public static String cacheKeyFromAll(Integer activityId, Long userId, Integer rewardType) {
        return CacheKeyGenerator.generateCacheKey(ZoneClazzRewardNotice.class, new String[]{"activityId", "userId", "rewardType"}, new Object[]{activityId, userId, rewardType});
    }

    public static String cacheKeyFromClazzAndType(Integer activityId, Long clazzId, Integer rewardType) {
        return CacheKeyGenerator.generateCacheKey(ZoneClazzRewardNotice.class, new String[]{"activityId", "clazzId", "rewardType"}, new Object[]{activityId, clazzId, rewardType});
    }
}
