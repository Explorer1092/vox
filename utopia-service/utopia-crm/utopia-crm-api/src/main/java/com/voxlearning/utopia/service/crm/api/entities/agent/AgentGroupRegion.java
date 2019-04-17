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
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AGENT REGION
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-7-4
 */
@Data
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_GROUP_REGION")
@UtopiaCacheExpiration
@UtopiaCacheRevision("201702241")
public class AgentGroupRegion extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    private static final long serialVersionUID = 2169025113244531302L;

    @UtopiaSqlColumn Long groupId;                  // 组ID
    @UtopiaSqlColumn Integer regionCode;            // 区域代码
    @UtopiaSqlColumn String regionName;             // 区域名称
    // schoolLevel字段从停止使用  20170209  wangsong 部门只配置区域，区域不指定中小学， 字典表中配置在该区域下的学校为该区域负责的学校
    @Getter @Setter @UtopiaSqlColumn Integer schoolLevel;                // 1:小学  2:中学  12:全部 99 未指定

    public static String ck_group(Long groupId) {
        return CacheKeyGenerator.generateCacheKey(AgentGroupRegion.class, "groupId", groupId);
    }

    public static String ck_region(Integer regionCode) {
        return CacheKeyGenerator.generateCacheKey(AgentGroupRegion.class, "regionCode", regionCode);
    }

    public static String ck_All() {
        return CacheKeyGenerator.generateCacheKey(AgentGroupRegion.class, "ALL");
    }

}
