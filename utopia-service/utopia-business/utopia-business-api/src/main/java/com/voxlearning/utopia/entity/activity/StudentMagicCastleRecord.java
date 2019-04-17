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

package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2015/12/1.
 */
@DocumentConnection(configName = "hs_platform")
@DocumentTable(table = "VOX_STUDENT_MAGIC_CASTLE_RECORD")
@NoArgsConstructor
@UtopiaCacheExpiration(7200)
@UtopiaCacheRevision("20160928")
public class StudentMagicCastleRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 8429304752767532380L;
    @UtopiaSqlColumn @Getter @Setter private Long magicianId;  // 魔法师ID
    @UtopiaSqlColumn @Getter @Setter private Long activeId;    // 唤醒ID
    @UtopiaSqlColumn @Getter @Setter private Long clazzId;
    @UtopiaSqlColumn @Getter @Setter private Boolean success;
    @UtopiaSqlColumn @Getter @Setter private Boolean disabled;
    @UtopiaSqlColumn @Getter @Setter private Integer activeLevel; // 0为老数据 1 一层梦境 2 二层梦境
    @UtopiaSqlColumn @Getter @Setter private Source source;
    @UtopiaSqlColumn @Getter @Setter private Integer successLevel; // 0 老数据 1 0-24小时之内唤醒 2 24-72小时内唤醒

    public static String ck_clazzId(Long clazzId) {
        return CacheKeyGenerator.generateCacheKey(StudentMagicCastleRecord.class,
                new String[]{"clazzId"},
                new Object[]{clazzId},
                new Object[]{0L});
    }

    public static String ck_magicianId(Long magicianId) {
        return CacheKeyGenerator.generateCacheKey(StudentMagicCastleRecord.class,
                new String[]{"magicianId"},
                new Object[]{magicianId},
                new Object[]{0L});
    }

    public static String ck_activeId(Long activeId) {
        return CacheKeyGenerator.generateCacheKey(StudentMagicCastleRecord.class,
                new String[]{"activeId"},
                new Object[]{activeId},
                new Object[]{0L});
    }

    public enum Source {
        pc,
        mobile
    }
}