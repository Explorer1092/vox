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
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Named
@CacheBean(type = AgentGroup.class)
public class AgentGroupPersistence extends StaticMySQLPersistence<AgentGroup, Long> {
    @Override
    protected void calculateCacheDimensions(AgentGroup source, Collection<String> dimensions) {
        dimensions.add(AgentGroup.ck_all());
        dimensions.add(AgentGroup.ck_pid(source.getParentId()));
        dimensions.add(AgentGroup.ck_rid(source.getRoleId()));
        dimensions.add(AgentGroup.ck_id(source.getId()));
        dimensions.add(AgentGroup.ck_gn(source.getGroupName()));
    }

    @CacheMethod(key = "ALL")
    public List<AgentGroup> findAllGroups() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentGroup> findByParentId(@CacheParameter("pid") Long parentId) {
        Criteria criteria = Criteria.where("PARENT_ID").is(parentId);
        criteria.and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<AgentGroup>> findByParentIds(@CacheParameter(value = "pid", multiple = true) Collection<Long> parentIds) {
        Criteria criteria = Criteria.where("PARENT_ID").in(parentIds).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentGroup::getParentId));
    }

    @CacheMethod
    public List<AgentGroup> findByRoleId(@CacheParameter("rid") Integer roleId) {
        Criteria criteria = Criteria.where("ROLE_ID").is(roleId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public AgentGroup findByGroupName(@CacheParameter(value = "gn") String groupName) {
        Criteria criteria = Criteria.where("GROUP_NAME").is(groupName).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public void updateGroupRole(Long groupId, AgentGroupRoleType roleType){
        AgentGroup group = load(groupId);
        if(group == null){
            return;
        }
        Update update = new Update();
        if(roleType == null){
            update.unset("ROLE_ID");
        }else {
            update.set("ROLE_ID", roleType.getId());
        }

        Criteria criteria = Criteria.where("ID").is(groupId);
        long rows = $update(update, criteria);
        if(rows > 0){
            evictDocumentCache(group);
        }
    }


}
