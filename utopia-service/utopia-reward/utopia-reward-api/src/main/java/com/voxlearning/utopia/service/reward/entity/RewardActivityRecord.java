package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 奖品中心 - 用户参与活动记录
 * Created by haitian.gan on 2017/2/4.
 */
@DocumentTable(table = "VOX_REWARD_ACTIVITY_RECORD")
@DocumentConnection(configName = "hs_reward")
@UtopiaCacheRevision("20180629")
public class RewardActivityRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 6766241701168115242L;

    @UtopiaSqlColumn @Getter @Setter private Long activityId;
    @UtopiaSqlColumn @Getter @Setter private Long userId;
    @UtopiaSqlColumn @Getter @Setter private String userName;
    @UtopiaSqlColumn @Getter @Setter private Double price;
    @UtopiaSqlColumn @Getter @Setter private String comment;
    @UtopiaSqlColumn @Getter @Setter private Integer type; // 0 普通捐赠 1 里程碑事件
    @UtopiaSqlColumn @Getter @Setter private String collectId; // 记录collectId

    @DocumentFieldIgnore
    @Getter
    @Setter
    private String userHeaderImg;

    @DocumentFieldIgnore
    @Getter
    @Setter
    private String timeExpression;// 时间表达式

    @DocumentFieldIgnore
    @Getter
    @Setter
    private int collectNums;

    public static String ck_acid_uid(Long activityId, Long userId) {
        return CacheKeyGenerator.generateCacheKey(RewardActivityRecord.class,
                new String[]{"ACID", "UID"},
                new Object[]{activityId, userId});
    }

    public static String ck_acid(Long activityId) {
        return CacheKeyGenerator.generateCacheKey(RewardActivityRecord.class, "ACID", activityId);
    }

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(RewardActivityRecord.class, "USER_ID", userId);
    }

    public static String ck_collect_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(RewardActivityRecord.class, "COLLECT_UID", userId);
    }

    public static final Integer PLAN = 0;
    public static final Integer MILESTONES = 1;
}
