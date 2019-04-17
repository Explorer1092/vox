/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.statistics.MonthPaymentRateData;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 15-1-31.
 */
@Named
public class MonthPaymentRateDataDao extends StaticMongoDao<MonthPaymentRateData, String> {

    @Override
    protected void calculateCacheDimensions(MonthPaymentRateData source, Collection<String> dimensions) {
    }

    /**
     * 查询某段时间内相关区域的数据,降序排列
     */
    public List<MonthPaymentRateData> findPaymentRate(Integer startDate, Set<Integer> regionCodeSet) {
        if (CollectionUtils.isEmpty(regionCodeSet)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.where("region_code").in(regionCodeSet)
                .and("month").gte(startDate)
                .and("status").is(1);
        Find find = Find.find(filter)
                .with(new Sort(Sort.Direction.ASC, "region_code"));
        return __find_OTF(find);
    }

    /**
     * 查询某段时间内相关区域的数据,降序排列
     */
    public List<MonthPaymentRateData> findPaymentRate(Integer startMonth) {
        Filter filter = filterBuilder.where("month").gte(startMonth)
                .and("status").is(1);
        Find find = Find.find(filter);
        return __find_OTF(find);
    }

}