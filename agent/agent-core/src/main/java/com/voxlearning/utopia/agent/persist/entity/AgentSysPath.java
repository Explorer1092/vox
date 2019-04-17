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

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * AGENT角色表
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-7-4
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_SYS_PATH")
@UtopiaCacheExpiration
public class AgentSysPath extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 2169025113244531305L;

    @UtopiaSqlColumn
    String appName;                     // 系统应用名
    @UtopiaSqlColumn
    String pathName;                    // 系统路径名
    @UtopiaSqlColumn
    String description;                 // 用户描述

    @DocumentFieldIgnore
    List<AgentSysPathRole> authRoleList;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentSysPath.class, id);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentSysPath.class, "ALL");
    }
}
