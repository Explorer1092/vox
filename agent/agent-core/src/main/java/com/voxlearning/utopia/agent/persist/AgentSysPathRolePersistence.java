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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPathRole;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Named
@UtopiaCacheSupport(AgentSysPathRole.class)
public class AgentSysPathRolePersistence extends StaticCacheDimensionDocumentJdbcDao<AgentSysPathRole, Long> {

    @UtopiaCacheable
    public List<AgentSysPathRole> findByRoleId(@UtopiaCacheKey(name = "roleId") Integer roleId) {
        Criteria criteria = Criteria.where("ROLE_ID").is(roleId);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable
    public List<AgentSysPathRole> findByPathId(@UtopiaCacheKey(name = "pathId") Long pathId) {
        Criteria criteria = Criteria.where("PATH_ID").is(pathId);
        return query(Query.query(criteria));
    }

    public int delete(final Long id) {
        return remove(id) ? 1 : 0;
    }
}
