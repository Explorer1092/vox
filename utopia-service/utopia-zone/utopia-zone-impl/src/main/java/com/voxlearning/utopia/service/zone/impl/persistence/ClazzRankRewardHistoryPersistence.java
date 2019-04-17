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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankRewardHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 14-11-13.
 */
@Named("com.voxlearning.utopia.service.zone.impl.persistence.ClazzRankRewardHistoryPersistence")
@CacheBean(type = ClazzRankRewardHistory.class)
public class ClazzRankRewardHistoryPersistence extends StaticMySQLPersistence<ClazzRankRewardHistory, Long> {

    @Override
    protected void calculateCacheDimensions(ClazzRankRewardHistory document, Collection<String> dimensions) {
        dimensions.add(ClazzRankRewardHistory.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<ClazzRankRewardHistory> findByUserId(@CacheParameter("userId") Long userId) {
        return query(Query.query(Criteria.where("USER_ID").is(userId)));
    }
}
