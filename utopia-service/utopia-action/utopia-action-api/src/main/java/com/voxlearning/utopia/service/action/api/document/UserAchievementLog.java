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
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

/**
 * 主键格式：userId-type-level
 *
 * @author xinxin
 * @since 12/8/2016
 * 用户获得成就等级的日志
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-achievement")
@DocumentCollection(collection = "vox_user_achievement_log_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UserAchievementLog implements CacheDimensionDocument {
    private static final long serialVersionUID = 2762578048902629791L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    @DocumentField("ct")
    private Date createTime;
    @DocumentUpdateTimestamp
    @DocumentField("ut")
    private Date updateTime;

    private Long userId;
    private AchievementType type; //成就类型
    private Integer level;  //成就等级

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey(new String[]{"UID"}, new Object[]{userId}),
        };
    }

    public UserAchievementLog generateId() {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(level);
        id = generateId(userId, type.name(), level);
        return this;
    }

    public static String generateId(Long userId, String type, Integer level) {
        return userId + "-" + type + "-" + level;
    }
}
