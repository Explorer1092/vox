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

package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Named
@UtopiaCacheSupport(AgentGroupUser.class)
public class AgentGroupUserPersistence extends StaticPersistence<Long, AgentGroupUser> {

    @Override
    protected void calculateCacheDimensions(AgentGroupUser source, Collection<String> dimensions) {
        dimensions.add(AgentGroupUser.ck_all());
        dimensions.add(AgentGroupUser.ck_gid(source.getGroupId()));
        dimensions.add(AgentGroupUser.ck_uid(source.getUserId()));
    }

    @UtopiaCacheable(key = "ALL")
    public List<AgentGroupUser> findAll() {
        return withSelectFromTable("WHERE DISABLED=FALSE").queryAll();
    }

    @UtopiaCacheable
    public List<AgentGroupUser> findByUserId(@UtopiaCacheKey(name = "uid") Long userId) {
        return withSelectFromTable("WHERE USER_ID=? AND DISABLED=FALSE").useParamsArgs(userId).queryAll();
    }

    @UtopiaCacheable
    public Map<Long, List<AgentGroupUser>> findByUserIds(@UtopiaCacheKey(name = "uid", multiple = true) Collection<Long> userIds) {
        List<AgentGroupUser> groupUserList = withSelectFromTable("WHERE USER_ID IN (:userIds) AND DISABLED=FALSE").useParams(MiscUtils.map("userIds", userIds)).queryAll();
        return groupUserList.stream().collect(Collectors.groupingBy(AgentGroupUser::getUserId));
    }

    @UtopiaCacheable
    public List<AgentGroupUser> findByGroupId(@UtopiaCacheKey(name = "gid") Long groupId) {
        return withSelectFromTable("WHERE GROUP_ID=? AND DISABLED=FALSE").useParamsArgs(groupId).queryAll();
    }

    @CacheMethod
    public Map<Long, List<AgentGroupUser>> findByGroupIds(@CacheParameter(value = "gid", multiple = true) Collection<Long> groupIds) {
        List<AgentGroupUser> groupUserList = withSelectFromTable("WHERE GROUP_ID IN (:groupIds) AND DISABLED=FALSE").useParams(MiscUtils.map("groupIds", groupIds)).queryAll();
        return groupUserList.stream().collect(Collectors.groupingBy(AgentGroupUser::getGroupId));
    }

    public List<AgentGroupUser> findByRoleId(Integer userRoleId) {
        return withSelectFromTable("WHERE USER_ROLE_ID=? AND DISABLED=FALSE").useParamsArgs(userRoleId).queryAll();
    }

    public int delete(Long id) {
        Collection<String> keys = calculateDimensions(id);
        String sql = "SET UPDATE_DATETIME=NOW(),DISABLED=TRUE WHERE ID=?";
        int rows = utopiaSql.withSql(sql).useParamsArgs(id).executeUpdate();
        if (rows > 0) {
            getCache().delete(keys);
        }
        return rows;
    }

}
