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
import com.voxlearning.utopia.agent.persist.entity.statistics.AppStatisticsDay;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 15-3-10.
 */
@Named
public class AppStatisticsDayPersistence extends AlpsStaticJdbcDao<AppStatisticsDay, Long> {
    @Override
    protected void calculateCacheDimensions(AppStatisticsDay document, Collection<String> dimensions) {
    }

    public List<AppStatisticsDay> loadByAppKeyAndDay(String appKey, int startDate, int endDate) {
        Criteria criteria = Criteria.where("APP_KEY").is(appKey)
                .and("DAY").gte(startDate).lte(endDate)
                .and("STATUS").is(true);
        Sort sort = new Sort(Sort.Direction.DESC, "DAY");
        return query(Query.query(criteria).with(sort));
    }
}
