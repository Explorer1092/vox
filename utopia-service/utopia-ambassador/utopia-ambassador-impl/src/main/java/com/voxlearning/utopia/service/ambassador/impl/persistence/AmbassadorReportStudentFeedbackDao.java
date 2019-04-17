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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportStudentFeedback;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer Yang on 2015/12/11.
 */
@Named
@CacheBean(type = AmbassadorReportStudentFeedback.class)
public class AmbassadorReportStudentFeedbackDao extends StaticCacheDimensionDocumentJdbcDao<AmbassadorReportStudentFeedback, Long> {

    @CacheMethod
    public List<AmbassadorReportStudentFeedback> loadByTeacherId(@CacheParameter("teacherId") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AmbassadorReportStudentFeedback> loadByTeacherIdAndStudentId(@CacheParameter("teacherId") Long teacherId,
                                                                             @CacheParameter("studentId") Long studentId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId).and("STUDENT_ID").is(studentId);
        return query(Query.query(criteria));
    }

}
