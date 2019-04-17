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
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * AGENT用户组表
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-7-4
 */
// @Data FIXME: 别再加Data了！这里加根本没用
@Getter
@Setter
@ToString
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_GROUP")
@UtopiaCacheExpiration
public class AgentGroup extends AbstractDatabaseEntityWithDisabledField implements Serializable {

    private static final long serialVersionUID = 2169025113244531301L;

    @UtopiaSqlColumn Long parentId;                      // 父组ID
    @UtopiaSqlColumn String groupName;                   // 用户组名
    @UtopiaSqlColumn String description;                 // 用户描述
    @UtopiaSqlColumn Integer roleId;                     // 所属大区级别ID AgentGroupRoleType
    @UtopiaSqlColumn String logo;                        // 大区徽标
    @UtopiaSqlColumn Integer headCount;                 // 职员数量
    @UtopiaSqlColumn String serviceType;                 // 业务类型

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof AgentGroup) {
            AgentGroup agentGroup = (AgentGroup) obj;
            if (agentGroup.getId().equals(this.getId())) {
                return true;
            }

        }
        return false;
    }

    public AgentGroupRoleType fetchGroupRoleType() {
        return AgentGroupRoleType.of(roleId);
    }


    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AgentGroup.class, "ALL");
    }

    public static String ck_pid(Long parentId) {
        return CacheKeyGenerator.generateCacheKey(AgentGroup.class, "pid", parentId);
    }

    public static String ck_rid(Integer roleId) {
        return CacheKeyGenerator.generateCacheKey(AgentGroup.class, "rid", roleId);
    }


    public static String ck_gn(String groupName) {
        return CacheKeyGenerator.generateCacheKey(AgentGroup.class, "gn", groupName);
    }

    public static String ck_id(Long groupId) {
        return CacheKeyGenerator.generateCacheKey(AgentGroup.class, groupId);
    }

    public List<AgentServiceType> fetchServiceTypeList(){
        return AgentServiceType.toTypeList(this.serviceType);
    }

}
