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
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * AGENT角色表
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-7-4
 */
@Data
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_GROUP_USER")
@UtopiaCacheExpiration
public class AgentGroupUser extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    private static final long serialVersionUID = 2169025113244531303L;

    @UtopiaSqlColumn Long userId;                   // 用户ID
    @UtopiaSqlColumn Long groupId;                  // 组ID
    @UtopiaSqlColumn Integer userRoleId;            // 所属在Group中的角色ID

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentGroupUser.class, "ALL");
    }

    public static String ck_gid(Long groupId) {
        return CacheKeyGenerator.generateCacheKey(AgentGroupUser.class, "gid", groupId);
    }

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentGroupUser.class, "uid", userId);
    }

    public AgentRoleType getUserRoleType() {
        return AgentRoleType.of(userRoleId);
    }

}
