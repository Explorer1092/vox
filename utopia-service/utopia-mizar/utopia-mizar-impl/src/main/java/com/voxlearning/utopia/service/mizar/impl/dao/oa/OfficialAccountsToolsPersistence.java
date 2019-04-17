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
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsTools;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2016/8/2.
 */
@Named
@CacheBean(type = OfficialAccountsTools.class)
public class OfficialAccountsToolsPersistence extends AlpsStaticJdbcDao<OfficialAccountsTools, Long> {

    @Override
    protected void calculateCacheDimensions(OfficialAccountsTools document, Collection<String> dimensions) {
        dimensions.add(OfficialAccountsTools.ck_AccountId(document.getAccountId()));
        dimensions.add(OfficialAccountsTools.ck_id(document.getId()));
    }

    @CacheMethod
    public List<OfficialAccountsTools> loadByAccountsId(@CacheParameter("accountId") Long accountId) {
        Criteria criteria = Criteria.where("ACCOUNT_ID").is(accountId).and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public int deleteTool(Long toolId) {
        OfficialAccountsTools original = $load(toolId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(toolId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            evictDocumentCache(original);
        }
        return rows;
    }
}