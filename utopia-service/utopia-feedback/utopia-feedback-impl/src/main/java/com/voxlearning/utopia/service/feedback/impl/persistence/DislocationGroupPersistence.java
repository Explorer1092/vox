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

package com.voxlearning.utopia.service.feedback.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.user.api.entities.DislocationGroup;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named("com.voxlearning.utopia.service.feedback.impl.persistence.DislocationGroupPersistence")
public class DislocationGroupPersistence extends NoCacheStaticMySQLPersistence<DislocationGroup, Long> {

    public DislocationGroup loadByGroupId(Long groupId) {
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("DISABLED").is(false);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public List<DislocationGroup> findByRealSchoolId(Long realSchoolId) {
        Criteria criteria = Criteria.where("REAL_SCHOOL_ID").is(realSchoolId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public List<DislocationGroup> findByTime(Date beginTime, Date endTime) {
        Criteria criteria = Criteria.where("UPDATE_DATETIME").gte(beginTime).lte(endTime)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public boolean disableByGroupId(Long groupId, String operationNotes, String operator) {
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("DISABLED").is(false);
        Update update = Update.update("NOTES", operationNotes)
                .set("LATEST_OPERATOR", operator)
                .set("DISABLED", true);
        return $update(update, criteria) > 0;
    }
}
