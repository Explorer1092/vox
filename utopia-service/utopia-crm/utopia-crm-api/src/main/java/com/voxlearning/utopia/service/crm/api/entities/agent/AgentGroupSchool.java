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
 * Created by Alex on 2015.07.20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_GROUP_SCHOOL")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170807")
public class AgentGroupSchool extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    private static final long serialVersionUID = 3456235367845465341L;

    @UtopiaSqlColumn Long groupId;                  // 组ID
    @UtopiaSqlColumn Integer regionCode;            // 学校所在区域Code
    @UtopiaSqlColumn Long schoolId;                 // 学校ID
//    @UtopiaSqlColumn String schoolName;             // 学校名称

    public static String ck_gid(Long groupId){
        return CacheKeyGenerator.generateCacheKey(AgentGroupSchool.class, "gid", groupId);
    }

    public static String ck_sid(Long schoolId){
        return CacheKeyGenerator.generateCacheKey(AgentGroupSchool.class, "sid", schoolId);
    }


}
