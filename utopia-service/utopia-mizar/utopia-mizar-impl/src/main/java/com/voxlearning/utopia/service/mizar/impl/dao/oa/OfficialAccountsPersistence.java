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
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2016/7/4.
 */
@Named
@CacheBean(type = OfficialAccounts.class)
public class OfficialAccountsPersistence extends AlpsStaticJdbcDao<OfficialAccounts, Long> {

    @Override
    protected void calculateCacheDimensions(OfficialAccounts document, Collection<String> dimensions) {
        dimensions.add(OfficialAccounts.ck_id(document.getId()));
        dimensions.add(OfficialAccounts.ck_all());
        dimensions.add(OfficialAccounts.ck_key(document.getAccountsKey()));
    }

    @UtopiaCacheable(key = "ALL")
    public List<OfficialAccounts> findAll() {
        return query();
    }

    @CacheMethod
    public OfficialAccounts loadByAccountsKey(@CacheParameter("AK") String accountsKey) {
        Criteria criteria = Criteria.where("ACCOUNTS_KEY").is(accountsKey);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    public int updateStatus(Long id, OfficialAccounts.Status status) {
        OfficialAccounts original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("STATUS", status);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows;
    }
}
