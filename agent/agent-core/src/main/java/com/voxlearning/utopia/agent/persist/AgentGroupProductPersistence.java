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

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentGroupProduct;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Shuai.Huan on 2014/7/17.
 */
@Named
public class AgentGroupProductPersistence extends AlpsStaticJdbcDao<AgentGroupProduct, Long> {

    @Override
    protected void calculateCacheDimensions(AgentGroupProduct document, Collection<String> dimensions) {
    }

    public List<AgentGroupProduct> findByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return query(Query.query(criteria));
    }

    public List<AgentGroupProduct> findByGroupIds(List<Long> groupIds) {
        Criteria criteria = Criteria.where("GROUP_ID").in(groupIds);
        Sort sort = new Sort(Sort.Direction.ASC, "PRODUCT_ID");
        return query(Query.query(criteria).with(sort));
    }

    public int deleteByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return (int) $remove(criteria);
    }

    public int deleteByProductIdAndGroupId(Long productId, Long groupId) {
        Criteria criteria = Criteria.where("GROUP_ID").is(groupId).and("PRODUCT_ID").is(productId);
        return (int) $remove(criteria);
    }
}
