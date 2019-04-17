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

package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiGuide;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-afenti")
@DocumentCollection(collection = "vox_afenti_user_afenti_guide")
@UtopiaCacheRevision("20160615")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class UserAfentiGuide implements Serializable {
    private static final long serialVersionUID = 8867433542506341552L;

    @DocumentId private Long id;
    private Map<String, Boolean> guides;

    public UserAfentiGuide initialize() {
        if (guides == null) {
            guides = new LinkedHashMap<>();
        }
        for (AfentiGuide afentiGuide : AfentiGuide.values()) {
            if (guides.containsKey(afentiGuide.name())) {
                continue;
            }
            guides.put(afentiGuide.name(), false);
        }
        return this;
    }

    public boolean isCompleted(AfentiGuide afentiGuide) {
        if (afentiGuide == null) {
            return false;
        }
        Boolean value = guides.get(afentiGuide.name());
        return value != null && value;
    }

    public boolean isCompleted(String name) {
        return AfentiGuide.isValid(name) && isCompleted(AfentiGuide.valueOf(name));
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(UserAfentiGuide.class, id);
    }
}
