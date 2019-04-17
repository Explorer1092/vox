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

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsOrder;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The 3rd Vendor Order Information Persistence Class
 *
 * @author Zhilong Hu
 * @since 2014-06-9
 */
@Named
@CacheBean(type = VendorAppsOrder.class)
public class VendorAppsOrderPersistence extends AlpsStaticJdbcDao<VendorAppsOrder, String> {

    @Override
    protected void calculateCacheDimensions(VendorAppsOrder document, Collection<String> dimensions) {
        dimensions.add(VendorAppsOrder.ck_id(document.getId()));
        dimensions.add(VendorAppsOrder.ck_appKey_sessionKey_orderSeq(document.getAppKey(), document.getSessionKey(), document.getOrderSeq()));
        dimensions.add(VendorAppsOrder.ck_appKey_userId(document.getAppKey(), document.getUserId()));
    }

    @CacheMethod
    public VendorAppsOrder find(@CacheParameter("appKey") String appKey,
                                @CacheParameter("sessionKey") String sessionKey,
                                @CacheParameter("orderSeq") Long orderSeq) {
        Criteria criteria = Criteria.where("APP_KEY").is(appKey)
                .and("SESSION_KEY").is(sessionKey)
                .and("ORDER_SEQ").is(orderSeq);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<VendorAppsOrder> find(@CacheParameter("appKey") String appKey,
                                      @CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("APP_KEY").is(appKey).and("USER_ID").is(userId);
        return query(Query.query(criteria));
    }

    public Integer findUserPaidHwcoinOrderCount(String appKey, Long userId) {
        Criteria criteria = Criteria.where("APP_KEY").is(appKey)
                .and("USER_ID").is(userId)
                .and("PAY_TYPE").is("HWCOIN")
                .and("STATUS").is("paid");
        return (int) count(Query.query(criteria));
    }

    public int finishPaymnet(final String orderId) {
        VendorAppsOrder original = $load(orderId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("STATUS", "paid");
        Criteria criteria = Criteria.where("ID").is(orderId).and("STATUS").is("unpaid");
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> dimensions = new HashSet<>();
            calculateCacheDimensions(original, dimensions);
            getCache().delete(dimensions);
        }
        return rows;
    }

}
