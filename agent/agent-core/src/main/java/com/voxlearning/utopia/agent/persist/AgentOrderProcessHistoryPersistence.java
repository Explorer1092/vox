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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Shuai.Huan on 2014/7/14.
 */
@Named
@CacheBean(type = AgentOrderProcessHistory.class)
public class AgentOrderProcessHistoryPersistence extends AlpsStaticJdbcDao<AgentOrderProcessHistory, Long> {

    @Override
    protected void calculateCacheDimensions(AgentOrderProcessHistory document, Collection<String> dimensions) {
        dimensions.add(AgentOrderProcessHistory.ck_id(document.getId()));
        dimensions.add(AgentOrderProcessHistory.ck_orderId(document.getOrderId()));
        dimensions.add(AgentOrderProcessHistory.ck_processor(document.getProcessor()));
    }

    @CacheMethod
    public List<AgentOrderProcessHistory> findByOrderId(@CacheParameter("orderId") Long orderId) {
        Criteria criteria = Criteria.where("ORDER_ID").is(orderId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentOrderProcessHistory> findByProcessor(@CacheParameter("processor") Long userId) {
        Criteria criteria = Criteria.where("PROCESSOR").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

}
