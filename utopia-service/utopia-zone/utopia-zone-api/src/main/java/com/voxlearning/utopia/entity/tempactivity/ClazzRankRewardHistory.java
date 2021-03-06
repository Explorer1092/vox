/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.entity.tempactivity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by XiaoPeng.Yang on 14-11-13.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_TA_CLAZZ_RANK_REWARD_HISTORY")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClazzRankRewardHistory extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 5053510918159200873L;

    @DocumentField private Long clazzId;                     // 班级ID
    @DocumentField private Long userId;                    // 用户ID
    @DocumentField private String month;                   // 月份  201411
    @DocumentField private Boolean rewarded;                   // 奖励领取状态  是否成功
    @DocumentField private Boolean specialFlag;                // 是否特殊奖励

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRankRewardHistory.class, "userId", userId);
    }
}
