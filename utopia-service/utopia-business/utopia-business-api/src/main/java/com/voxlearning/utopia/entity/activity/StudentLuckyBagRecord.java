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

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.LuckyBagStatus;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2016/1/27.
 * 3月回流活动 福袋
 */
@DocumentTable(table = "VOX_STUDENT_LUCKY_BAG_RECORD")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudentLuckyBagRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 237659722551908121L;
    @UtopiaSqlColumn @Getter @Setter private Long senderId;   // 传递人  如果为0 代表是起点
    @UtopiaSqlColumn @Getter @Setter private Long receiverId; // 被传递人
    @UtopiaSqlColumn @Getter @Setter private LuckyBagStatus status;  // 状态

    public static String ck_senderId(Long senderId) {
        return CacheKeyGenerator.generateCacheKey(StudentLuckyBagRecord.class,
                new String[]{"senderId"},
                new Object[]{senderId},
                new Object[]{0L});
    }

    public static String ck_receiverId(Long receiverId) {
        return CacheKeyGenerator.generateCacheKey(StudentLuckyBagRecord.class,
                new String[]{"receiverId"},
                new Object[]{receiverId},
                new Object[]{0L});
    }
}
