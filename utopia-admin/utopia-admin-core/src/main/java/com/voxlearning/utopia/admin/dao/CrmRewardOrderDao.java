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

package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CrmRewardOrderDao extends AlpsStaticJdbcDao<RewardOrder, Long> {
    @Override
    protected void calculateCacheDimensions(RewardOrder document, Collection<String> dimensions) {
    }

    public Page<RewardOrder> find(Pageable pageable, Criteria criteria) {
        Objects.requireNonNull(pageable);
        Objects.requireNonNull(criteria);

        Query query = Query.query(criteria).with(pageable);
        List<RewardOrder> content = query(query);
        long total = count(Query.query(criteria));
        return new PageImpl<>(content, pageable, total);
    }

    public Map<Long, List<RewardOrder>> loadAllByUserIds(Collection<Long> userIds) {
        Criteria criteria = Criteria.where("BUYER_ID").in(userIds);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort)).stream()
                .collect(Collectors.groupingBy(RewardOrder::getBuyerId));
    }


    public Page<RewardOrder> find(Long buyerId,
                                  Date startTime,
                                  Date endTime,
                                  String status,
                                  Pageable pageable){

        List<Criteria> list = new LinkedList<>();
        if (buyerId != 0) {
            list.add(Criteria.where("BUYER_ID").is(buyerId));
        }

        if (startTime != null) {
            list.add(Criteria.where("CREATE_DATETIME").gte(startTime));
        }

        if (endTime != null) {
            list.add(Criteria.where("CREATE_DATETIME").lte(endTime));
        }

        if (StringUtils.isNotBlank(status)) {
            list.add(Criteria.where("STATUS").is(status));
        }

        list.add(Criteria.where("DISABLED").is(false));
        Criteria criteria = Criteria.and(list);

        // 走从库查询
        return RoutingPolicyExecutorBuilder.getInstance()
                .<PageImpl<RewardOrder>>newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> {

                    Query query = Query.query(criteria).with(pageable);
                    List<RewardOrder> content = query(query);

                    long total = count(Query.query(criteria));
                    return new PageImpl<>(content, pageable, total);

                }).execute();
    }
}
