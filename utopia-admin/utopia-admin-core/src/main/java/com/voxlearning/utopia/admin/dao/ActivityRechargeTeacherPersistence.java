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

package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.activity.ActivityRechargeTeacher;

import javax.inject.Named;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 15-3-16.
 */
@Named
public class ActivityRechargeTeacherPersistence extends NoCacheStaticMySQLPersistence<ActivityRechargeTeacher, Long> {

    public ActivityRechargeTeacher loadByTeacherIdAndMonth(Long teacherId, Integer month) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId)
                .and("MONTH").is(month);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public Page<ActivityRechargeTeacher> loadByMonthAndStatus(Pageable pageable, int month, int status) {
        Criteria criteria = Criteria.where("MONTH").is(month)
                .and("STATUS").is(status);
        long count = count(Query.query(criteria));

        Query query = Query.query(criteria)
                .with(new Sort(Sort.Direction.DESC, "RECHARGED"))
                .with(pageable);
        List<ActivityRechargeTeacher> content = query(query);
        return new PageImpl<>(content, pageable, count);
    }
}
