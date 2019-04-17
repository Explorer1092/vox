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


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author tanguohong
 * @since 14-3-12
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-afenti")
@DocumentCollection(collection = "vox_afenti_user_afenti_stats")
public class UserAfentiStats implements Serializable {
    private static final long serialVersionUID = 1859910978815120445L;

    @DocumentId private Long id; // 用户ID
    private Map<String, String> stats;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(UserAfentiStats.class, id);
    }

    public UserAfentiStats initialize() {
        if (getStats() == null) setStats(new LinkedHashMap<>());
        return this;
    }
}
