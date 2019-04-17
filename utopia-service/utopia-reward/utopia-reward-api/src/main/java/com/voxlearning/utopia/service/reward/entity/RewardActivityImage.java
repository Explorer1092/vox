package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 奖品中心 - 活动 图片实体
 * Created by ganhaitian on 2017/2/6.
 */
@DocumentTable(table = "VOX_REWARD_ACTIVITY_IMAGE")
@DocumentConnection(configName = "hs_reward")
public class RewardActivityImage extends AbstractDatabaseEntity {

    @UtopiaSqlColumn @Getter @Setter private Long activityId;
    @UtopiaSqlColumn @Getter @Setter private String location;
    @UtopiaSqlColumn @Getter @Setter private Integer displayOrder;

    public static String ck_acid(Long activityId) {
        return CacheKeyGenerator.generateCacheKey(RewardActivityImage.class, "ACID", activityId);
    }
}
