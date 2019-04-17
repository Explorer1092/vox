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

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentUserClue;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author shiwei.liao
 * @since 2015/7/20.
 */
@Named
public class AgentUserCluePersistence extends AlpsStaticJdbcDao<AgentUserClue, Long> {

    @Override
    protected void calculateCacheDimensions(AgentUserClue document, Collection<String> dimensions) {
    }

    public AgentUserClue getAgentUserClueBySchoolId(Long schoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    public List<AgentUserClue> getAgentUserClueBySchoolIdList(List<Long> schoolIds) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolIds);
        Sort sort = new Sort(Sort.Direction.DESC, "UPDATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }
}
