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

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;

import javax.inject.Named;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-10-30
 */
@Named("com.voxlearning.utopia.service.ambassador.impl.persistence.SchoolAmbassadorPersistence")
public class SchoolAmbassadorPersistence extends NoCacheStaticMySQLPersistence<SchoolAmbassador, Long> {

    public SchoolAmbassador findByUserId(Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }
}
