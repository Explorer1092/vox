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

package com.voxlearning.utopia.entity.ucenter;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/7/2015
 */
@DocumentTable(table = "VOX_MENTOR_HISTORY")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@RequiredArgsConstructor
@Getter
@Setter
public class MentorHistory extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -1348367990566970958L;

    @UtopiaSqlColumn @NonNull private Long mentorId;
    @UtopiaSqlColumn @NonNull private Long menteeId;
    @UtopiaSqlColumn @NonNull private String mentorType;//主被动类型
    @UtopiaSqlColumn @NonNull private String mentorCategory;//帮助类型
    @UtopiaSqlColumn @NonNull private String mentorLevel;//帮助级别
    @UtopiaSqlColumn private Boolean success;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(MentorHistory.class, id);
    }

    public static String ck_mentorId(Long mentorId) {
        return CacheKeyGenerator.generateCacheKey(MentorHistory.class, "mentorId", mentorId);
    }

    public static String ck_menteeId(Long menteeId) {
        return CacheKeyGenerator.generateCacheKey(MentorHistory.class, "menteeId", menteeId);
    }
}
