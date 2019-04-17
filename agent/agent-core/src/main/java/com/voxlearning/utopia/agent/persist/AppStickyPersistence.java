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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.agent.persist.entity.statistics.AppSticky;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 15-3-10.
 */
@Named
public class AppStickyPersistence extends AlpsStaticJdbcDao<AppSticky, Long> {
    @Override
    protected void calculateCacheDimensions(AppSticky document, Collection<String> dimensions) {
    }

    //默认查30天粘性数据
    public List<AppSticky> loadByAppKeyAndDay(String appKey, int startDate, int endDate) {
        Date currentEndDate = DateUtils.calculateDateDay(DateUtils.stringToDate(ConversionUtils.toString(endDate), "yyyyMMdd"), 30);
        int currentEnd = ConversionUtils.toInt(DateUtils.dateToString(currentEndDate, "yyyyMMdd"));
        Criteria criteria = Criteria.where("APP_KEY").is(appKey)
                .and("BEGIN_DAY").gte(startDate).lte(endDate)
                .and("CURRENT_DAY").gte(startDate).lte(currentEnd)
                .and("STATUS").is(true);
        return query(Query.query(criteria));
    }
}