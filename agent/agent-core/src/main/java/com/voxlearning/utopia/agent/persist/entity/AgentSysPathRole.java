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
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

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
@DocumentTable(table = "AGENT_SYS_PATH_ROLE")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170329")
public class AgentSysPathRole extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = 2169025113244531306L;

    private Integer roleId; // 角色ID
    private Long pathId;    // 系统路径ID

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("roleId", roleId),
                newCacheKey("pathId", pathId)
        };
    }
}
