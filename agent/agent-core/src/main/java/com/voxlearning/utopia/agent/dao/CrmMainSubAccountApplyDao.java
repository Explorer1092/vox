/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmMainSubAccountApply;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;

import javax.inject.Named;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 包班申请DAO
 *
 * @author Yuechen.wang
 * @since 2016-07-22
 */
@Named("agent.CrmMainSubAccountApplyDao")
@CacheBean(type = CrmMainSubAccountApply.class)
public class CrmMainSubAccountApplyDao extends AlpsStaticMongoDao<CrmMainSubAccountApply, String> {
    @Override
    protected void calculateCacheDimensions(CrmMainSubAccountApply source, Collection<String> dimensions) {
        dimensions.add(CrmMainSubAccountApply.ck_uid(source.getApplicantId()));
    }

    public Page<CrmMainSubAccountApply> findByPage(Pageable pageable, Long teacherId, String status, Date startDate, Date endDate, String applicant, String auditor) {
        Criteria criteria = Criteria.where("disabled").is(false);
        if (teacherId != null && teacherId != 0L) {
            criteria = criteria.and("teacherId").is(teacherId);
        }
        if (StringUtils.isNotBlank(status)) {
            criteria = criteria.and("auditStatus").is(status);
        }
        if (StringUtils.isNotBlank(applicant)) {
            if (SafeConverter.toLong(applicant) > 0L) {
                criteria = criteria.and("applicantId").is(SafeConverter.toLong(applicant));
            } else {
                criteria = criteria.and("applicantName").regex(Pattern.compile(".*" + applicant + ".*"));
            }
        }
        if (StringUtils.isNotBlank(auditor)) {
            if (SafeConverter.toLong(auditor) > 0L) {
                criteria = criteria.and("auditor").is(SafeConverter.toLong(auditor));
            } else {
                criteria = criteria.and("auditorName").regex(Pattern.compile(".*" + auditor + ".*"));
            }
        }
        if (startDate != null && endDate != null) {
            criteria = criteria.and("createTime").gte(startDate).lte(endDate);
        }
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "updateTime"));
        return new PageImpl<>(query(query.with(pageable)), pageable, count(query));
    }

    /**
     * 根据申请人员ID 查询出所有的包班申请记录
     */
    @CacheMethod
    public Map<Long, List<CrmMainSubAccountApply>> findByApplicant(@CacheParameter(value = "UID", multiple = true) Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("applicantId").in(userIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        List<CrmMainSubAccountApply> list = query(query);
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(CrmMainSubAccountApply::getApplicantId, Collectors.toList()));
    }

    public List<CrmMainSubAccountApply> findByTeacherId(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<CrmMainSubAccountApply> findByPeriod(Date start, Date end) {
        if (start == null || end == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("createTime").gte(start).lte(end)
                .and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

}
