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

package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.PrimaryKeyAccessor;
import com.voxlearning.alps.spi.common.TimestampAccessor;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-4-17
 */
@DocumentTable(table = "VOX_LIKE_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20150820")
public class LikeDetail implements Serializable, TimestampTouchable, TimestampAccessor, PrimaryKeyAccessor<Long> {
    private static final long serialVersionUID = 927724107030694196L;

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) private Long id;
    @UtopiaSqlColumn(name = "CREATE_DATETIME") private Date createDatetime;

    @UtopiaSqlColumn(name = "JOURNAL_ID") @NonNull private Long journalId;
    @UtopiaSqlColumn(name = "JOURNAL_OWNER_ID") @NonNull private Long journalOwnerId;
    @UtopiaSqlColumn(name = "JOURNAL_OWNER_CLAZZ_ID") @NonNull private Long journalOwnerClazzId;
    @UtopiaSqlColumn(name = "USER_ID") @NonNull private Long userId;
    @UtopiaSqlColumn(name = "USER_NAME") private String userName;
    @UtopiaSqlColumn(name = "USER_IMG") private String userImg;

    public static String cacheKeyFromJournalId(Long journalId) {
        return CacheKeyGenerator.generateCacheKey(LikeDetail.class, "journalId", journalId);
    }

    public static String cacheKeyFromJournalOwnerId(Long journalOwnerId) {
        return CacheKeyGenerator.generateCacheKey(LikeDetail.class, "journalOwnerId", journalOwnerId);
    }

    @Override
    public void touchCreateTime(long timestamp) {
        if (createDatetime == null) {
            createDatetime = new Date(timestamp);
        }
    }

    @Override
    public long fetchCreateTimestamp() {
        return createDatetime == null ? 0 : createDatetime.getTime();
    }

    @Override
    public long fetchUpdateTimestamp() {
        return 0;
    }
}
