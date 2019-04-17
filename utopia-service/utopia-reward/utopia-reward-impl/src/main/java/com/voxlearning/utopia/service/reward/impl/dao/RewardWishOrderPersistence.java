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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardWishOrder;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Persistence implementation of entity {@link RewardWishOrder}.
 *
 * @author Xiaopeng Yang
 * @since Jul 14, 2014
 */
@Named
@CacheBean(type = RewardWishOrder.class)
public class RewardWishOrderPersistence extends AlpsStaticJdbcDao<RewardWishOrder, Long> {

    @Override
    protected void calculateCacheDimensions(RewardWishOrder document, Collection<String> dimensions) {
        dimensions.add(RewardWishOrder.ck_id(document.getId()));
        dimensions.add(RewardWishOrder.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public Map<Long, List<RewardWishOrder>> loadByUserIds(@CacheParameter(value = "userId", multiple = true)
                                                                  Collection<Long> userIds) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds)
                .and("DISABLED").is(false)
                .and("ACHIEVED").is(false);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(RewardWishOrder::getUserId));
    }

    public int deleteWishOrderById(Long id, Long userId) {
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(id).and("USER_ID").is(userId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            cacheKeys.add(RewardWishOrder.ck_id(id));
            cacheKeys.add(RewardWishOrder.ck_userId(userId));
            getCache().delete(cacheKeys);
        }
        return rows;
    }

    public int achievedWishOrderById(final Long id) {
        RewardWishOrder original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("ACHIEVED", true).currentDate("ACHIEVED_DATETIME");
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }
}
