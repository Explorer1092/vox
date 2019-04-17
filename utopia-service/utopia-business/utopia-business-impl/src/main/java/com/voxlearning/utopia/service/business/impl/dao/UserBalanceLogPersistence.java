/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.UserBalanceLog;

import javax.inject.Named;
import java.util.List;

@Named
@UtopiaCacheSupport(UserBalanceLog.class)
public class UserBalanceLogPersistence extends StaticCacheDimensionDocumentJdbcDao<UserBalanceLog, Long> {

    @UtopiaCacheable
    public List<UserBalanceLog> findByUserId(@UtopiaCacheKey(name = "userId") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public boolean isUserBalanceLogOperationExtIdExisting(UserBalanceLog log) {
        Criteria criteria = Criteria.where("USER_ID").is(log.getUserId());

        if (null != log.getOperationExtId()) {
            criteria.and("OPERATION_EXT_ID").is(log.getOperationExtId());
        } else {
            criteria.and("OPERATION_EXT_ID").notExists();
        }

        if (null != log.getOperationMethod()) {
            criteria.and("OPERATION_METHOD").is(log.getOperationMethod());
        } else {
            criteria.and("OPERATION_METHOD").notExists();
        }

        return count(Query.query(criteria)) > 0;
    }
}

