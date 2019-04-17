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
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * User growth log data structure.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Aug 5, 2016
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-app")
@DocumentDatabase(database = "vox-growth")
@DocumentCollection(collection = "vox_user_growth_log_{}", dynamic = true)
@DocumentIndexes(
        @DocumentIndex(def = "{'userId':1,'actionTime':-1}", background = true)
)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UserGrowthLog implements CacheDimensionDocument {
    private static final long serialVersionUID = 4119491832069730722L;

    // ID usage: userId-yyyyMMdd-randomObjectId
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
    private Long userId;
    private ActionEventType type;
    private Date actionTime;
    private Integer delta;          // the delta of growth value

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("UID", userId)
        };
    }
}
