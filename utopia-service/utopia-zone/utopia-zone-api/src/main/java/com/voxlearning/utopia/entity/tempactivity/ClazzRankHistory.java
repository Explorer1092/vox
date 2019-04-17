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
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by XiaoPeng.Yang on 14-11-13.
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_TA_CLAZZ_RANK_HISTORY")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160729")
public class ClazzRankHistory extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = -2344227063247750941L;

    private Long clazzId;          // 班级ID
    private Integer rank;          // 排名 为0表示特殊奖励
    private String month;          // 月份  201411
    private Integer level;         // 级别
    private Integer levelScore;    // 分数
    private String schoolName;     // 学校
    private String clazzName;      //  班级名称

    public static String ck_clazzId(Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRankHistory.class, "C", clazzId);
    }

    public static String ck_month(String month) {
        return CacheKeyGenerator.generateCacheKey(ClazzRankHistory.class, "M", month);
    }

    @Override
    @Deprecated
    public String[] generateCacheDimensions() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
