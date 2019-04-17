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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Persistence implementation of {@code ClazzZoneBag}.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 2014-05-12
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneBagPersistence")
@CacheBean(type = ClazzZoneBag.class)
public class ClazzZoneBagPersistence extends AlpsStaticJdbcDao<ClazzZoneBag, Long> {

    @Override
    protected void calculateCacheDimensions(ClazzZoneBag document, Collection<String> dimensions) {
        dimensions.add(ClazzZoneBag.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<ClazzZoneBag> findByUserId(@CacheParameter("USER_ID") final Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria));
    }

    public boolean delete(final Long id, final Long userId) {
        Criteria criteria = Criteria.where("ID").is(id).and("USER_ID").is(userId);
        long rows = executeDelete(criteria, getTableName());
        if (rows > 0) {
            ClazzZoneBag mock = new ClazzZoneBag();
            mock.setUserId(userId);
            evictDocumentCache(mock);
        }
        return rows > 0;
    }
}
