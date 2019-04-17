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
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetitionDetail;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer Yang on 2015/7/13.
 */
@Named
@CacheBean(type = AmbassadorCompetitionDetail.class)
public class AmbassadorCompetitionDetailDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorCompetitionDetail, Long> {

    @CacheMethod
    public List<AmbassadorCompetitionDetail> loadByTeacherId(@CacheParameter("teacherId") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria));
    }

    public void updateCompetitionDetailSchool(Subject subject, Long schoolId, Long targetSchoolId) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId)
                .and("SUBJECT").is(subject);
        List<AmbassadorCompetitionDetail> originals = query(Query.query(criteria));
        Update update = Update.update("SCHOOL_ID", targetSchoolId);
        if ($update(update, criteria) > 0) {
            evictDocumentCache(originals);
        }
    }
}
