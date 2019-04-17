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

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2015/10/29.
 * 试手气 开宝盒
 */

@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_MOONLIGHT_BOX_HISTORY")
@NoArgsConstructor
@UtopiaCacheExpiration
public class RewardMoonLightBoxHistory extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -4408682426715866884L;
    @Getter @Setter @UtopiaSqlColumn private Long productId;
    @Getter @Setter @UtopiaSqlColumn private String productName;
    @Getter @Setter @UtopiaSqlColumn private Long skuId;
    @Getter @Setter @UtopiaSqlColumn private String skuName;
    @Getter @Setter @UtopiaSqlColumn private Double price;
    @Getter @Setter @UtopiaSqlColumn private Long userId;
    @Getter @Setter @UtopiaSqlColumn private String awardName;
    @Getter @Setter @UtopiaSqlColumn private Integer awardId;

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(RewardMoonLightBoxHistory.class, "userId", userId);

    }

    public static String ck_userId_awardId(Long userId, Integer awardId) {
        return CacheKeyGenerator.generateCacheKey(RewardMoonLightBoxHistory.class,
                new String[]{"userId", "awardId"},
                new Object[]{userId, awardId});
    }

    public static String getCacheKeyByPrice(int price) {
        String cacheKey = "";
        if (price >= 1000 && price < 5000) {
            cacheKey = "REWARD_M_L_B_M_C_1000";
        } else if (price >= 5000 && price < 10000) {
            cacheKey = "REWARD_M_L_B_M_C_5000";
        } else if (price >= 10000 && price < 20000) {
            cacheKey = "REWARD_M_L_B_M_C_10000";
        } else if (price >= 20000 && price < 50000) {
            cacheKey = "REWARD_M_L_B_M_C_20000";
        } else if (price >= 50000) {
            cacheKey = "REWARD_M_L_B_M_C_50000";
        }
        return cacheKey;
    }
}
