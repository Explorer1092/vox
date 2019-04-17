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

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Alex on 14-11-10.
 */
@Named
public class VendorNotifyPersistence extends AlpsStaticJdbcDao<VendorNotify, Long> {
    @Override
    protected void calculateCacheDimensions(VendorNotify document, Collection<String> dimensions) {
    }

    public List<VendorNotify> findUndeliveriedNotify() {
        // WHERE STATUS=0 AND RETRY_COUNT>0 AND RETRY_COUNT<4 ORDER BY UPDATE_DATETIME ASC LIMIT 3000
        Criteria criteria = Criteria.where("STATUS").is(0).and("RETRY_COUNT").gte(0).lt(4);
        Sort sort = new Sort(Sort.Direction.ASC, "UPDATE_DATETIME");
        return query(Query.query(criteria).with(sort).limit(3000));
    }

    public List<VendorNotify> findTodayDeliveryFailedNotify() {
        // WHERE STATUS=0 AND RETRY_COUNT > 3 AND APP_KEY <> '17Student' AND DATEDIFF(CREATE_DATETIME, now())=0
        DayRange current = DayRange.current();
        Criteria criteria = Criteria.where("STATUS").is(0).and("RETRY_COUNT").gt(3)
                .and("APP_KEY").ne("17Student")
                .and("CREATE_DATETIME").gte(current.getStartDate()).lte(current.getEndDate());
        return query(Query.query(criteria));
    }

    public int findTodayDeliveryFailedNotifyCount() {
        // 不查学生了,太多了
        // WHERE STATUS=0 AND RETRY_COUNT>3 AND APP_KEY <> '17Student' AND DATEDIFF(CREATE_DATETIME, now())=0
        DayRange current = DayRange.current();
        Criteria criteria = Criteria.where("STATUS").is(0).and("RETRY_COUNT").gt(3)
                .and("APP_KEY").ne("17Student")
                .and("CREATE_DATETIME").gte(current.getStartDate()).lte(current.getEndDate());
        return (int) count(Query.query(criteria));
    }

    public int updateNotifyDeliveried(Long notifyId) {
        Update update = Update.update("STATUS", 1);
        Criteria criteria = Criteria.where("ID").is(notifyId);
        return (int) $update(update, criteria);
    }

    public int updateNotifyDeliveryFailed(Long notifyId) {
        Update update = new Update().inc("RETRY_COUNT", 1);
        Criteria criteria = Criteria.where("ID").is(notifyId);
        return (int) $update(update, criteria);
    }
}
