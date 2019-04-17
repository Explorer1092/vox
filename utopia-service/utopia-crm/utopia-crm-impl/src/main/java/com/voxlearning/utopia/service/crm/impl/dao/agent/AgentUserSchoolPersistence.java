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

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/11.
 */
@Named
@UtopiaCacheSupport(AgentUserSchool.class)
public class AgentUserSchoolPersistence extends StaticPersistence<Long, AgentUserSchool> {

    @Override
    protected void calculateCacheDimensions(AgentUserSchool source, Collection<String> dimensions) {
        dimensions.add(AgentUserSchool.ck_all());
        dimensions.add(AgentUserSchool.ck_school(source.getSchoolId()));
        dimensions.add(AgentUserSchool.ck_user(source.getUserId()));
    }

    @UtopiaCacheable(key = "ALL")
    public List<AgentUserSchool> findAll() {
        return withSelectFromTable("WHERE DISABLED=FALSE").queryAll();
    }

    @UtopiaCacheable
    public List<AgentUserSchool> findByUser(@UtopiaCacheKey(name = "U") Long userId) {
        return withSelectFromTable("WHERE USER_ID=? AND DISABLED=FALSE").useParamsArgs(userId).queryAll();
    }

    @UtopiaCacheable
    public Map<Long, List<AgentUserSchool>> findByUsers(@UtopiaCacheKey(name = "U", multiple = true) Collection<Long> userIds) {
        return withSelectFromTable("WHERE USER_ID IN (:userIds) AND DISABLED=FALSE").useParams(MiscUtils.map("userIds", userIds)).queryAll()
                .stream().collect(Collectors.groupingBy(AgentUserSchool::getUserId));
    }

    @UtopiaCacheable
    public List<AgentUserSchool> findBySchool(@UtopiaCacheKey(name = "S") Long schoolId) {
        return withSelectFromTable("WHERE SCHOOL_ID=? AND DISABLED=FALSE").useParamsArgs(schoolId).queryAll();
    }

    @UtopiaCacheable
    public Map<Long, List<AgentUserSchool>> findBySchools(@UtopiaCacheKey(name = "S", multiple = true) Collection<Long> schoolIds) {
        return withSelectFromTable("WHERE SCHOOL_ID IN (:schoolIds) AND DISABLED=FALSE").useParams(MiscUtils.map("schoolIds", schoolIds)).queryAll()
                .stream().collect(Collectors.groupingBy(AgentUserSchool::getSchoolId));
    }

    public int delete(final Long id) {
        Collection<String> keys = calculateDimensions(id);
        String sql = "SET UPDATE_DATETIME=NOW(),DISABLED=TRUE WHERE ID=?";
        int rows = utopiaSql.withSql(sql).useParamsArgs(id).executeUpdate();
        if (rows > 0) {
            getCache().delete(keys);
        }
        return rows;
    }
}
