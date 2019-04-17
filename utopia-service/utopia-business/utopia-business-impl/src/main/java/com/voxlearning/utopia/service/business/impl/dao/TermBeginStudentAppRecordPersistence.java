/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.entity.activity.TermBeginStudentAppRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Summer Yang on 2015/7/30.
 */
@Named
@CacheBean(type = TermBeginStudentAppRecord.class)
public class TermBeginStudentAppRecordPersistence extends AlpsStaticJdbcDao<TermBeginStudentAppRecord, Long> {

    private final Date startDate = DateUtils.stringToDate("2015-09-19 00:00:00");//新一轮大礼包抽奖活动重新开启

    @Override
    protected void calculateCacheDimensions(TermBeginStudentAppRecord document, Collection<String> dimensions) {
        dimensions.add(TermBeginStudentAppRecord.ck_teacherId(document.getTeacherId()));
    }

    @CacheMethod
    public List<TermBeginStudentAppRecord> findByTeacherId(@CacheParameter("teacherId") Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId)
                .and("CREATE_DATETIME").gte(startDate);
        return query(Query.query(criteria));
    }
}