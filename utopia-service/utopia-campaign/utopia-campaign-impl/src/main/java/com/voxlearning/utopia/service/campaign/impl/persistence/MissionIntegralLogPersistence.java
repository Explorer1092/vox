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

package com.voxlearning.utopia.service.campaign.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.entity.mission.MissionIntegralLog;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/9/2015
 */
@Named("com.voxlearning.utopia.service.campaign.impl.persistence.MissionIntegralLogPersistence")
@CacheBean(type = MissionIntegralLog.class)
public class MissionIntegralLogPersistence extends StaticMySQLPersistence<MissionIntegralLog, Long> {

    @Override
    protected void calculateCacheDimensions(MissionIntegralLog source, Collection<String> dimensions) {
        dimensions.add(MissionIntegralLog.ck_studentId(source.getStudentId()));
    }

    @CacheMethod
    public List<MissionIntegralLog> findByStudentId(@CacheParameter("S") Long studentId) {
        Criteria criteria = Criteria.where("STUDENT_ID").is(studentId);
        return query(Query.query(criteria));
    }
}
