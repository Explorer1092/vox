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
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Named
@UtopiaCacheSupport(AgentGroupRegion.class)
public class AgentGroupRegionPersistence extends StaticPersistence<Long, AgentGroupRegion> {

    @Override
    protected void calculateCacheDimensions(AgentGroupRegion source, Collection<String> dimensions) {
        dimensions.add(AgentGroupRegion.ck_All());
        dimensions.add(AgentGroupRegion.ck_group(source.getGroupId()));
        dimensions.add(AgentGroupRegion.ck_region(source.getRegionCode()));
    }

    @UtopiaCacheable
    public List<AgentGroupRegion> findByGroupId(@UtopiaCacheKey(name = "groupId") Long groupId) {
        return withSelectFromTable("WHERE GROUP_ID=? AND DISABLED=FALSE").useParamsArgs(groupId).queryAll();
    }

    @UtopiaCacheable
    public List<AgentGroupRegion> findByRegionCode(@UtopiaCacheKey(name = "regionCode") Integer regionCode) {
        return withSelectFromTable("WHERE REGION_CODE=? AND DISABLED=FALSE").useParamsArgs(regionCode).queryAll();
    }

    @UtopiaCacheable
    public Map<Integer, List<AgentGroupRegion>> findByRegionCodes(@UtopiaCacheKey(name = "regionCode", multiple = true) Collection<Integer> regionCodes) {
        List<AgentGroupRegion> result = withSelectFromTable("WHERE REGION_CODE IN (:regionCodes) AND DISABLED=FALSE").useParams(MiscUtils.map("regionCodes", regionCodes)).queryAll();
        return result.stream().collect(Collectors.groupingBy(AgentGroupRegion::getRegionCode));
    }

    public List<AgentGroupRegion> findByGroupSet(Collection<Long> groupSet) {
        return withSelectFromTable("WHERE GROUP_ID IN (:groupSet) AND DISABLED=FALSE").useParams(MiscUtils.map("groupSet", groupSet)).queryAll();
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

    @UtopiaCacheable(key = "ALL")
    public List<AgentGroupRegion> findAll() {
        return withSelectFromTable("WHERE DISABLED=FALSE").queryAll();
    }

    @UtopiaCacheable
    public Map<Long, List<AgentGroupRegion>> findByGroupIds(@UtopiaCacheKey(name = "groupId", multiple = true) Collection<Long> groupIds) {
        List<AgentGroupRegion> result = withSelectFromTable("WHERE GROUP_ID IN (:groupIds) AND DISABLED=FALSE").useParams(MiscUtils.map("groupIds", groupIds)).queryAll();
        return result.stream().collect(Collectors.groupingBy(AgentGroupRegion::getGroupId));
    }
}
