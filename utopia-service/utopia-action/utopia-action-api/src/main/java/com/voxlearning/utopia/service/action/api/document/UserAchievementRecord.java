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

package com.voxlearning.utopia.service.action.api.document;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * New user achievement data structure to replace the old UserAchievementRecord
 *
 * @author Alex
 * @since May 17, 2017
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-user")
@DocumentDatabase(database = "vox-user")
@DocumentCollection(collection = "vox_user_achievement_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170605")
public class UserAchievementRecord implements CacheDimensionDocument {
    private static final long serialVersionUID = -4855558126161223586L;

    public static final String ID_SEP = "_";

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                // userid_ActionEventType format
    @DocumentCreateTimestamp
    @DocumentField("ct")
    private Date createTime;
    @DocumentUpdateTimestamp
    @DocumentField("ut")
    private Date updateTime;
    @DocumentField("score")
    private Integer score;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_id(id),
                ck_uid(SafeConverter.toLong(id.split(ID_SEP)[0]))
        };
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(UserAchievementRecord.class, id);
    }

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(UserAchievementRecord.class, "UID", userId);
    }

    public static void main(String[] args) {
        System.out.println(ck_id("333915329_ObtainStar"));
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"userId", "aet"})
    @AllArgsConstructor
    public static class UserAchievementRecordId implements Serializable {
        private static final long serialVersionUID = 9214421142251255823L;

        private Long userId;
        private String aet;

        @Override
        public String toString() {
            return StringUtils.join(Arrays.asList(userId, aet), ID_SEP);
        }
    }

    public UserAchievementRecord.UserAchievementRecordId parse() {
        if (id == null) {
            return null;
        }
        String[] segments = id.split(ID_SEP);
        if (segments.length != 2) {
            return null;
        }
        return new UserAchievementRecord.UserAchievementRecordId(SafeConverter.toLong(segments[0]), segments[1]);
    }

}
