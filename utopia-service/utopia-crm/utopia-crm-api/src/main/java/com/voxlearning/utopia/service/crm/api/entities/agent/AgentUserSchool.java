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

package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by Shuai.Huan on 2014/7/11.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_USER_SCHOOL")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170331")
public class AgentUserSchool extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    private static final long serialVersionUID = 3456235267835465345L;

    @UtopiaSqlColumn Long userId;                   // 用户ID
    @UtopiaSqlColumn Integer regionCode;            // 学校所在区域Code
    @UtopiaSqlColumn Long schoolId;                 // 学校ID
    @UtopiaSqlColumn Integer schoolLevel;           // 1:小学  2:中学

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentUserSchool.class, "ALL");
    }

    public static String ck_school(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(AgentUserSchool.class, "S", schoolId);
    }

    public static String ck_user(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentUserSchool.class, "U", userId);
    }

}
