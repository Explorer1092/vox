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

package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.spi.cache.CacheValueLoaderExecutor;
import com.voxlearning.alps.spi.cache.ExternalLoader;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Persistence implementation of entity {@link RewardCouponDetail}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 30, 2014
 */
@Named
@UtopiaCacheSupport(RewardCouponDetail.class)
public class RewardCouponDetailPersistence extends StaticCacheDimensionDocumentJdbcDao<RewardCouponDetail, Long> {

    @CacheMethod
    public Map<Long, List<RewardCouponDetail>> loadByProductIds(@CacheParameter(value = "productId", multiple = true)
                                                                final Collection<Long> productIds) {
        Criteria criteria = Criteria.where("PRODUCT_ID").in(productIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(RewardCouponDetail::getProductId));
    }

    @CacheMethod
    public List<RewardCouponDetail> loadByProductId(@CacheParameter(value = "productId") final Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<RewardCouponDetail>> loadByUserIds(@CacheParameter(value = "userId", multiple = true)
                                                             final Collection<Long> userIds) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(RewardCouponDetail::getUserId));
    }

    public boolean couponExchanged(RewardCouponDetail couponDetail, Long userId, String mobile) {
        Criteria criteria = Criteria.where("ID").is(couponDetail.getId()).and("USER_ID").notExists();
        RewardCouponDetail original = query(Query.query(criteria)).stream().findFirst().orElse(null);
        if (original == null) {
            return false;
        }
        Update update = Update.update("USER_ID", userId)
                .set("MOBILE", mobile)
                .set("EXCHANGED", true)
                .currentDate("EXCHANGED_DATE");
        long rows = $update(update, criteria);
        if (rows > 0) {
            RewardCouponDetail modified = $load(couponDetail.getId());
            evictDocumentCache(Arrays.asList(original, modified));
        }
        return rows > 0;
    }

    public boolean couponExchanged(RewardCouponDetail couponDetail, Long userId, String mobile, Long orderId) {
        Criteria criteria = Criteria.where("ID").is(couponDetail.getId()).and("USER_ID").notExists();
        RewardCouponDetail original = query(Query.query(criteria)).stream().findFirst().orElse(null);
        if (original == null) {
            return false;
        }
        Update update = Update.update("USER_ID", userId)
                .set("ORDER_ID", orderId)
                .set("MOBILE", mobile)
                .set("EXCHANGED", true)
                .currentDate("EXCHANGED_DATE");
        long rows = $update(update, criteria);
        if (rows > 0) {
            RewardCouponDetail modified = $load(couponDetail.getId());
            evictDocumentCache(Arrays.asList(original, modified));
        }
        return rows > 0;
    }

    public boolean couponRebated(RewardCouponDetail couponDetail) {
        RewardCouponDetail document = new RewardCouponDetail();
        document.setId(couponDetail.getId());
        document.setRebated(true);
        document.setRebatedDate(new Date());
        return replace(document) != null;
    }

    public boolean couponUsed(RewardCouponDetail couponDetail) {
        RewardCouponDetail document = new RewardCouponDetail();
        document.setId(couponDetail.getId());
        document.setUsed(true);
        return replace(document) != null;
    }

    /**
     * 给 crm 查询优惠券库存使用, 非实时生效
     */
    public Map<Long, Long> loadCouponStock() {
        String KEY_ALL = "ALL"; // 假的,只是为了用缓存框架虚拟出来的
        String sql = "select PRODUCT_ID as PID,count(PRODUCT_ID) as CT from VOX_REWARD_COUPON_DETAIL force index(IDX_CRD_ECD) where CREATE_DATETIME > '2014-01-01' and  EXCHANGED = 0 group by PRODUCT_ID";

        ExternalLoader<String, Map<Long, Long>> externalLoader = missedSources -> {
            Map<Long, Long> result = new HashMap<>();

            getJdbcTemplate().query(sql, rs -> {
                long pid = rs.getLong("PID");
                long count = rs.getLong("CT");
                result.put(pid, count);
            });

            Map<String, Map<Long, Long>> res = new HashMap<>();
            res.put(KEY_ALL, result);
            return res;
        };

        // 缓存3小时
        CacheValueLoaderExecutor<String, Map<Long, Long>> cacheValueLoader = getCache().createCacheValueLoader();
        Map<Long, Long> result = cacheValueLoader.keyGenerator(id -> "reward:loadCouponStock" + "-" + id)
                .keys(Collections.singleton(KEY_ALL))
                .loads()
                .externalLoader(externalLoader)
                .loadsMissed()
                .expiration(3 * 60 * 60)
                .write()
                .getAndResortResult()
                .get(KEY_ALL);

        return result;
    }
}
