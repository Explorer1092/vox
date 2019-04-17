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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentViewUser;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Alex on 14-11-3.
 */
@Named
@UtopiaCacheSupport(AgentViewUser.class)
public class AgentViewUserPersistence extends StaticCacheDimensionDocumentJdbcDao<AgentViewUser, Long> {
    @UtopiaCacheable
    public AgentViewUser findByUserId(@UtopiaCacheKey(name = "userId") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort).limit(1)).stream().findFirst().orElse(null);
    }

    @UtopiaCacheable
    public List<AgentViewUser> findByParentUserId(@UtopiaCacheKey(name = "parentUserId") Long parentUserId) {
        Criteria criteria = Criteria.where("PARENT_USER_ID").is(parentUserId);
        return query(Query.query(criteria));
    }

    public int delete(Long id) {
        return remove(id) ? 1 : 0;
    }
}
