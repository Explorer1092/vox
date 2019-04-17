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
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-5-19
 */
@DocumentTable(table = "VOX_CLAZZ_ZONE_COMMENT")
@Getter
@Setter
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ClazzZoneComment implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = 5757417321629202497L;

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) private Long id;
    @UtopiaSqlColumn(name = "CREATE_DATETIME") private Date createDatetime;

    @UtopiaSqlColumn(name = "JOURNAL_ID") private Long journalId;
    @UtopiaSqlColumn(name = "JOURNAL_OWNER_ID") private Long journalOwnerId;
    @UtopiaSqlColumn(name = "JOURNAL_OWNER_CLAZZ_ID") private Long journalOwnerClazzId;
    @UtopiaSqlColumn(name = "USER_ID") private Long userId;
    @UtopiaSqlColumn(name = "USER_NAME") private String userName;
    @UtopiaSqlColumn(name = "IMG_COMMENT") private Long imgComment;
    @UtopiaSqlColumn(name = "USER_IMG") private String userImg;

    @Override
    public void touchCreateTime(long timestamp) {
        if (createDatetime == null) {
            createDatetime = new Date(timestamp);
        }
    }

    public static String cacheKeyFromJournalId(Long journalId) {
        return CacheKeyGenerator.generateCacheKey(ClazzZoneComment.class, "JID", journalId);
    }

    public static String cacheKeyFromJournalOwnerId(Long journalOwnerId) {
        return CacheKeyGenerator.generateCacheKey(ClazzZoneComment.class, "journalOwnerId", journalOwnerId);
    }
}
