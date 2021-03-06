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

package com.voxlearning.utopia.schedule.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.entity.user.PremiseAuthenticateTeacher;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-11-5.
 */
@Named
public class PremiseAuthenticateTeacherPersistence extends NoCacheStaticMySQLPersistence<PremiseAuthenticateTeacher, Long> {

    public PremiseAuthenticateTeacher loadByTeacherId(Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public void deleteByTeacherId(Long teacherId) {
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId);
        Update update = Update.update("DISABLED", true);
        $update(update, criteria);
    }
}
