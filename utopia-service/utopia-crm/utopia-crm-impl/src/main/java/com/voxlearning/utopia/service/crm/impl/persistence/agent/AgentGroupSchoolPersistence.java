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

package com.voxlearning.utopia.service.crm.impl.persistence.agent;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Shuai.Huan on 2014/7/11.
 */
@Named
@CacheBean(type = AgentGroupSchool.class)
public class AgentGroupSchoolPersistence extends StaticMySQLPersistence<AgentGroupSchool, Long> {
    @Override
    protected void calculateCacheDimensions(AgentGroupSchool source, Collection<String> dimensions) {
        dimensions.add(AgentGroupSchool.ck_gid(source.getGroupId()));
        dimensions.add(AgentGroupSchool.ck_sid(source.getSchoolId()));
    }

    @CacheMethod
    public List<AgentGroupSchool> findByGroupId(@CacheParameter("gid") Long groupId){
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<AgentGroupSchool>> findByGroupIds(@CacheParameter(value = "gid", multiple = true) Collection<Long> groupIds){
        Criteria criteria = Criteria.where("GROUP_ID").in(groupIds).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentGroupSchool::getGroupId));
    }

    @CacheMethod
    public AgentGroupSchool findBySchoolId(@CacheParameter("sid") Long schoolId){
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, AgentGroupSchool> findBySchoolIds(@CacheParameter(value = "sid", multiple = true) Collection<Long> schoolIds) {
        Criteria criteria = Criteria.where("SCHOOL_ID").in(schoolIds).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentGroupSchool::getSchoolId, Function.identity(), (o1, o2) -> o1));
    }

    public int deleteByGroupId(Long groupId){
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("DISABLED").is(false);
        List<AgentGroupSchool> groupSchools = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(groupSchools)) {
            return 0 ;
        }
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(groupSchools);
        }
        return rows;
    }

    public int deleteByGroupAndRegion(Long groupId, Integer regionCode){
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("DISABLED").is(false).and("REGION_CODE").is(regionCode);
        List<AgentGroupSchool> groupSchools = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(groupSchools)) {
            return 0 ;
        }
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(groupSchools);
        }
        return rows;
    }

    public int deleteBySchoolId(Long schoolId){
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId).and("DISABLED").is(false);
        List<AgentGroupSchool> groupSchools = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(groupSchools)) {
            return 0 ;
        }
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(groupSchools);
        }
        return rows;
    }


    public int deleteByGroupAndRegions(Long groupId, Collection<Integer> regionCodes){
        if(CollectionUtils.isEmpty(regionCodes)){
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("DISABLED").is(false).and("REGION_CODE").in(regionCodes);
        List<AgentGroupSchool> groupSchools = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(groupSchools)) {
            return 0 ;
        }
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(groupSchools);
        }
        return rows;
    }

}
