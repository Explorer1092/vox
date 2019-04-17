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
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 活动记录
 * @author chensn
 * @date 2018-10-30 16:02
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_activity_record_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181030")
public class ClazzActivityRecord implements Serializable {
    private static final long serialVersionUID = -463601911673480678L;

    public static final String CLAZZ_BOSS_USER_AWARD = "clazzBossUserAward";

    /**
     * activityId_schoolId_clazzId_userId;
     */
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    /**
     * 状态 1.已参加 0.已完成
     */
    private Integer status;
    /**
     * 得分
     */
    private Integer score;
    private Integer activityId;
    private Long clazzId;
    private Long userId;
    private Long schoolId;
    private Map<String,Integer> studentTotalReward;//用户奖励
    //活动用户支付订单列表
    private List<String> orderIds;
    //活动用户支付订单支付情况 （支付失败暂时不存，只存成功数据）
    private Map<String, Boolean> orderPay;
    //暂存各个业务模糊的分类字段
    private Map<String, Object> condition;
    //业务对象  针对某一个活动
    private Object bizObject;

    public static String cacheKeyFromActivityId(Integer activityId) {
        return CacheKeyGenerator.generateCacheKey(ClazzActivityRecord.class, new String[]{"activityId"}, new Object[]{activityId});
    }
    public static String cacheKeyFromSchoolId(Integer activityId,Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(ClazzActivityRecord.class, new String[]{"activityId", "schoolId"}, new Object[]{activityId, schoolId});
    }

    public static String cacheKeyFromClazzId(Integer activityId,Long schoolId,Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ClazzActivityRecord.class, new String[]{"activityId", "schoolId", "clazzId"}, new Object[]{activityId, schoolId, clazzId});
    }

    public void generateId() {
        id = activityId + "_" + schoolId + "_" + clazzId + "_" + userId;
    }

    public static String generateId(Integer activityId,Long schoolId,Long clazzId,Long userId) {
        return activityId + "_" + schoolId + "_" + clazzId + "_" + userId;
    }
}
