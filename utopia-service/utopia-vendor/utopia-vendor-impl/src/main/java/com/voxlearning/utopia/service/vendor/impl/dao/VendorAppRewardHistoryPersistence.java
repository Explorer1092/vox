/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppRewardHistory;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
public class VendorAppRewardHistoryPersistence extends NoCacheStaticMySQLPersistence<VendorAppRewardHistory, Long> {

    public List<VendorAppRewardHistory> findByCreateTimeAndUserIdAndAppIdAndRewardType(Date createTime, Long userId, Integer appId, String rewardType) {
        String date = DateUtils.dateToString(createTime, DateUtils.FORMAT_SQL_DATE);
        Date startDate = DateUtils.stringToDate(date + " 00:00:00");
        Date endDate = DateUtils.stringToDate(date + " 23:59:59");
        Criteria criteria = Criteria.where("CREATETIME").gte(startDate).lte(endDate)
                .and("USER_ID").is(userId)
                .and("APP_ID").is(appId)
                .and("REWARD_TYPE").is(rewardType);
        return query(Query.query(criteria));
    }
}
