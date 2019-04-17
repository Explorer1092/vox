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

package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.StageAppType;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.SystemAppCategory;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 系统小游戏，大冒险配置
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/8 15:45
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-adventure")
@DocumentCollection(collection = "vox_adventure_systemapp")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20150806")
public class SystemApp implements Serializable {
    private static final long serialVersionUID = 2133660689706310119L;

    @DocumentId private Long id;
    private String categoryName;
    private String fileName;
    private String practiceName;
    private StageAppType type;
    private SystemAppCategory category;
    private String size;
    private Boolean valid;

    @JsonIgnore
    public boolean isValidTrue() {
        return Boolean.TRUE.equals(valid);
    }

    public static String cacheKeyAll() {
        return CacheKeyGenerator.generateCacheKey(SystemApp.class, "ALL");
    }
}
