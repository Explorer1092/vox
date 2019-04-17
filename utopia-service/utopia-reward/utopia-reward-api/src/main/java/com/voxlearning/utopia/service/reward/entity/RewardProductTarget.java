package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 奖品中心 - 商品的地区投放表
 * Created by haitian.gan on 2017/3/23.
 */
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_PRODUCT_TARGET")
@UtopiaCacheRevision("20170323")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class RewardProductTarget extends AbstractDatabaseEntityWithDisabledField {

    @Getter @Setter @UtopiaSqlColumn private Long productId;
    @Getter @Setter @UtopiaSqlColumn private Integer targetType;
    @Getter @Setter @UtopiaSqlColumn private String targetStr;

    public static String ck_productId(Long productId) {
        return CacheKeyGenerator.generateCacheKey(RewardProductTarget.class, "PRODUCT_ID", productId);
    }

    public static String ck_targetPid(Long productId) {
        return CacheKeyGenerator.generateCacheKey(RewardProductTarget.class, "TARGET_PID", productId);
    }

    public static String ck_allGroupByPid() {
        return CacheKeyGenerator.generateCacheKey(RewardProductTarget.class, "ALL_GROUPBY_PID");
    }
}
