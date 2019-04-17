/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * Reward wish order entity data structure.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Jul 14, 2014
 */
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_WISH_ORDER")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class RewardWishOrder implements Serializable, TimestampTouchable, TimestampAccessor, PrimaryKeyAccessor<Long> {
    private static final long serialVersionUID = -6415222839373856278L;

    @Getter @Setter @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) Long id;
    @DocumentCreateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "CREATE_DATETIME") Date createDatetime;
    @DocumentUpdateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "UPDATE_DATETIME") Date updateDatetime;
    @Getter @Setter @UtopiaSqlColumn private Long productId;
    @Getter @Setter @UtopiaSqlColumn private String productName;
    @Getter @Setter @UtopiaSqlColumn private Long userId;
    @Getter @Setter @UtopiaSqlColumn private Boolean disabled;
    @Getter @Setter @UtopiaSqlColumn private Boolean achieved;
    @Getter @Setter @UtopiaSqlColumn private Date achievedDatetime;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(RewardWishOrder.class, id);
    }

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(RewardWishOrder.class, "userId", userId);
    }

    public static RewardWishOrder newInstance(Long productId,
                                              Long userId) {
        RewardWishOrder inst = new RewardWishOrder();
        inst.productId = Objects.requireNonNull(productId);
        inst.userId = Objects.requireNonNull(userId);
        return inst;
    }

    @Override
    public void touchCreateTime(long timestamp) {
        if (getCreateDatetime() == null) {
            setCreateDatetime(new Date(timestamp));
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        if (getUpdateDatetime() == null) {
            setUpdateDatetime(new Date(timestamp));
        }
    }

    @Override
    public long fetchCreateTimestamp() {
        return createDatetime == null ? 0 : createDatetime.getTime();
    }

    @Override
    public long fetchUpdateTimestamp() {
        return updateDatetime == null ? 0 : updateDatetime.getTime();
    }
}

