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

package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Alex on 2014/8/14.
 */
@Named
@UtopiaCacheSupport(AgentOrderProduct.class)
public class AgentOrderProductPersistence extends StaticCacheDimensionDocumentJdbcDao<AgentOrderProduct, Long> {

    @UtopiaCacheable
    public List<AgentOrderProduct> findByOrderId(@UtopiaCacheKey(name = "orderId") Long orderId) {
        Criteria criteria = Criteria.where("ORDER_ID").is(orderId);
        Sort sort = new Sort(Sort.Direction.ASC, "RANK");
        return query(Query.query(criteria).with(sort));
    }

    @UtopiaCacheable
    public Map<Long,List<AgentOrderProduct>> findByOrderIds(@UtopiaCacheKey(name = "orderId",multiple = true) List<Long> orderIds) {
        Criteria criteria = Criteria.where("ORDER_ID").in(orderIds);
        Sort sort = new Sort(Sort.Direction.ASC, "RANK");
        List<AgentOrderProduct> resultList = query(Query.query(criteria).with(sort));
        return resultList.stream().collect(Collectors.groupingBy(AgentOrderProduct::getOrderId));
    }

    public int delete(final Long id) {
        return remove(id) ? 1 : 0;
    }
}
