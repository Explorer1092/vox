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
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsArticle;

import javax.inject.Named;
import java.util.*;

/**
 * Created by Summer Yang on 2016/7/4.
 */
@Named
@CacheBean(type = OfficialAccountsArticle.class)
public class OfficialAccountsArticlePersistence extends AlpsStaticJdbcDao<OfficialAccountsArticle, Long> {

    @Override
    protected void calculateCacheDimensions(OfficialAccountsArticle document, Collection<String> dimensions) {
        dimensions.add(OfficialAccountsArticle.ck_id(document.getId()));
        dimensions.add(OfficialAccountsArticle.ck_accountId(document.getAccountId()));
    }

    @CacheMethod
    public List<OfficialAccountsArticle> loadByAccountsId(@CacheParameter("accountId") Long accountId) {
        // 默认查询30天
        Date startDate = DateUtils.calculateDateDay(new Date(), -30);
        Criteria criteria = Criteria.where("ACCOUNT_ID").is(accountId)
                .and("DISABLED").is(false).and("CREATE_DATETIME").gte(startDate);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public List<OfficialAccountsArticle> loadByAccountsId(
            Long accountId,
            Date starDate,
            Date endDate,
            Collection<String> statusList){

        Criteria criteria = Criteria
                .where("ACCOUNT_ID").is(accountId)
                .and("STATUS").in(statusList)
                .and("PUBLISH_DATETIME").gte(starDate).lte(endDate);

        return query(Query.query(criteria));
    }

    public int updateStatus(Long accountId, String bundleId, OfficialAccountsArticle.Status status) {
        Criteria criteria = Criteria.where("ACCOUNT_ID").is(accountId)
                .and("BUNDLE_ID").is(bundleId);
        List<OfficialAccountsArticle> articles = query(Query.query(criteria));

        Update update = Update.update("STATUS", status);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> dimensions = new HashSet<>();
            articles.forEach(e -> calculateCacheDimensions(e, dimensions));
            getCache().delete(dimensions);
        }
        return rows;
    }

    public List<OfficialAccountsArticle> loadByBundleId(String bundleId) {
        Criteria c = Criteria.where("BUNDLE_ID").is(bundleId).and("DISABLED").is(false);
        return query(Query.query(c));
    }
}
