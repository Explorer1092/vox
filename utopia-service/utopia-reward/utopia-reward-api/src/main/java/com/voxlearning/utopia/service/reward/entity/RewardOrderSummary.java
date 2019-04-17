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
 * Created by Summer Yang on 2015/10/26.
 */
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_ORDER_SUMMARY")
@NoArgsConstructor
@UtopiaCacheExpiration
public class RewardOrderSummary extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -4408682426715866884L;
    @Getter @Setter @UtopiaSqlColumn private Long productId;
    @Getter @Setter @UtopiaSqlColumn private String productName;
    @Getter @Setter @UtopiaSqlColumn private Long skuId;
    @Getter @Setter @UtopiaSqlColumn private String skuName;
    @Getter @Setter @UtopiaSqlColumn private Integer studentCount;
    @Getter @Setter @UtopiaSqlColumn private Double studentPrice;
    @Getter @Setter @UtopiaSqlColumn private Integer teacherCount;
    @Getter @Setter @UtopiaSqlColumn private Double teacherPrice;
    @Getter @Setter @UtopiaSqlColumn private Integer juniorTeacherCount;
    @Getter @Setter @UtopiaSqlColumn private Double juniorTeacherPrice;
    @Getter @Setter @UtopiaSqlColumn private Integer month;

    public static String ck_month(Integer month) {
        return CacheKeyGenerator.generateCacheKey(RewardOrderSummary.class, "month", month);
    }

    public static String getUserCountSummaryKey() {
        return "R_O_U_C_STR:";
    }

}