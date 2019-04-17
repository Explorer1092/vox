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

package com.voxlearning.utopia.service.mizar.impl.dao.oa;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsTarget;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * DAO of {@link OfficialAccountsTarget}.
 *
 * @author Summer Yang
 * @since Jul 4, 2016
 */
@Named
@CacheBean(type = OfficialAccountsTarget.class)
public class OfficialAccountsTargetPersistence extends AlpsStaticJdbcDao<OfficialAccountsTarget, Long> {

    @Override
    protected void calculateCacheDimensions(OfficialAccountsTarget document, Collection<String> dimensions) {
        dimensions.add(OfficialAccountsTarget.ck_accountId(document.getAccountId()));
    }

    @CacheMethod
    public List<OfficialAccountsTarget> findByAccountId(@CacheParameter("accountId") Long accountId) {
        Criteria criteria = Criteria.where("ACCOUNT_ID").is(accountId).and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public int clearAccountTarget(Long accountId, Integer type) {
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ACCOUNT_ID").is(accountId).and("TARGET_TYPE").is(type);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            getCache().delete(OfficialAccountsTarget.ck_accountId(accountId));
        }
        return rows;
    }

    public int disable(Long id) {
        OfficialAccountsTarget original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            getCache().delete(OfficialAccountsTarget.ck_accountId(original.getAccountId()));
        }
        return rows;
    }
}
