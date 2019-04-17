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
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Persistence implementation of entity {@link RewardOrder}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 14, 2014
 */
@Named
@UtopiaCacheSupport(RewardOrder.class)
public class RewardOrderPersistence extends AlpsStaticJdbcDao<RewardOrder, Long> {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql orderSql;
    private UtopiaSql orderSqlReward;

    @Override
    public void afterPropertiesSet() {
        orderSql = utopiaSqlFactory.getUtopiaSql("order");
        orderSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    @Override
    protected void calculateCacheDimensions(RewardOrder document, Collection<String> dimensions) {
        dimensions.add(RewardOrder.ck_id(document.getId()));
        dimensions.add(RewardOrder.ck_buyerId(document.getBuyerId()));
        dimensions.add(RewardOrder.ck_logisticsId(document.getLogisticsId()));
        dimensions.add(RewardOrder.ck_clazzId_categoryCode(document.getClazzId(),document.getProductCategory()));
    }

    @CacheMethod
    public Map<Long, List<RewardOrder>> loadByUserIds(@CacheParameter(value = "userId", multiple = true)
                                                              Collection<Long> userIds) {
        Criteria criteria = Criteria.where("BUYER_ID").in(userIds)
                .and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort)).stream()
                .collect(Collectors.groupingBy(RewardOrder::getBuyerId));
    }

    public int updateOrderStatus(final Long orderId, String reason, final RewardOrderStatus orderStatus) {
        RewardOrder original = $load(orderId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("STATUS", orderStatus).set("REASON", reason);
        Criteria criteria = Criteria.where("ID").is(orderId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    public int updateOrderById(Long orderId, RewardOrderStatus orderStatus, Long completeOrderId) {
        RewardOrder original = $load(orderId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("STATUS", orderStatus).set("COMPLETE_ID", completeOrderId);
        Criteria criteria = Criteria.where("ID").is(orderId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    public int removeOrder(Long orderId) {
        RewardOrder original = $load(orderId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(orderId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    public List<RewardOrder> loadExportOrderByParam(Map<String, Object> param) {
        List<Criteria> list = new LinkedList<>();
        list.add(Criteria.where("DISABLED").is(false));
        if (param.get("status") != null) {
            list.add(Criteria.where("STATUS").is(param.get("status")));
        }
        if (param.get("startDate") != null) {
            list.add(Criteria.where("CREATE_DATETIME").gte(param.get("startDate")));
        }
        if (param.get("endDate") != null) {
            list.add(Criteria.where("CREATE_DATETIME").lte(param.get("endDate")));
        }
        Criteria criteria = Criteria.and(list.toArray(new Criteria[list.size()]));
        return query(Query.query(criteria));
    }

    public int updateRewardOrderLogisticsId(Long orderId, Long logisticsId) {
        RewardOrder original = $load(orderId);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("LOGISTICS_ID", logisticsId);
        Criteria criteria = Criteria.where("ID").is(orderId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    @CacheMethod
    public List<RewardOrder> loadByLogisticId(@CacheParameter(value = "logisticId") Long logisticId) {
        Criteria criteria = Criteria.where("LOGISTICS_ID").is(logisticId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, Integer> loadUserCollectOrdersInClazz(
            @CacheParameter("clazzId") Long clazzId,
            @CacheParameter("categoryCode") String categoryCode,
            Date startDate) {

        // 如果结果不是有序的这个OrderBy就白写了。。。
        return RoutingPolicyExecutorBuilder.getInstance()
                .<Map<Long, Integer>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> orderSqlReward.withSql(
                        " SELECT BUYER_ID,count(BUYER_ID) buyNums " +
                                " FROM VOX_REWARD_ORDER " +
                                " WHERE CLAZZ_ID = ? " +
                                " AND PRODUCT_CATEGORY = ? " +
                                " AND CREATE_DATETIME >= ?" +
                                " GROUP BY BUYER_ID " +
                                " ORDER BY buyNums desc")
                        .useParamsArgs(clazzId, categoryCode, startDate)
                        .queryAll()
                        .stream()
                        .collect(Collectors.toMap(
                                k -> SafeConverter.toLong(k.get("BUYER_ID")),
                                v -> SafeConverter.toInt(v.get("buyNums")),
                                (u, v) -> u, LinkedHashMap::new)
                        ))
                .execute();
    }

}
