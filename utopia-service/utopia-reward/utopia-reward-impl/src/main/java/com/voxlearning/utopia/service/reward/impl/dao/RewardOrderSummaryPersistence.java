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

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardOrderSummary;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2015/10/26.
 */
@Named
@UtopiaCacheSupport(RewardOrderSummary.class)
public class RewardOrderSummaryPersistence extends AlpsStaticJdbcDao<RewardOrderSummary, Long> {

    @Override
    protected void calculateCacheDimensions(RewardOrderSummary document, Collection<String> dimensions) {
        dimensions.add(RewardOrderSummary.ck_month(document.getMonth()));
    }

    @UtopiaCacheable
    public List<RewardOrderSummary> loadByMonth(@UtopiaCacheKey(name = "month") Integer month) {
        Criteria criteria = Criteria.where("MONTH").is(month);
        return query(Query.query(criteria));
    }
}
