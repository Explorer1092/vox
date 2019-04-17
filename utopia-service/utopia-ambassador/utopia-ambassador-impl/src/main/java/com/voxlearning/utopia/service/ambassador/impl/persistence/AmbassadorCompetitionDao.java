/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.ambassador.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetition;

import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Summer Yang on 2015/7/13.
 */
@Named
@CacheBean(type = AmbassadorCompetition.class)
public class AmbassadorCompetitionDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorCompetition, Long> {

    @CacheMethod
    public AmbassadorCompetition loadByTeacherId(@CacheParameter("teacherId") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<AmbassadorCompetition> loadBySchoolId(@CacheParameter("schoolId") Long schoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public int disabled(Long id) {
        AmbassadorCompetition original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    // ========================================================================
    // VERY LOW PERFORMANCE
    // ========================================================================

    public void updateCompetitionSchool(Subject subject, Long schoolId, Long targetSchoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId).and("SUBJECT").is(subject);
        List<AmbassadorCompetition> originals = query(Query.query(criteria));
        if (originals.isEmpty()) {
            return;
        }
        Update update = Update.update("SCHOOL_ID", targetSchoolId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            criteria = Criteria.where("SCHOOL_ID").is(targetSchoolId).and("SUBJECT").is(subject);
            List<AmbassadorCompetition> modified = query(Query.query(criteria));
            Set<String> cacheKeys = new HashSet<>();
            originals.forEach(e -> calculateCacheDimensions(e, cacheKeys));
            modified.forEach(e -> calculateCacheDimensions(e, cacheKeys));
            getCache().delete(cacheKeys);
        }
    }
}
