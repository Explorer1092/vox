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

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.reward.entity.RewardMoonLightBoxHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/10/29.
 */
@Named
@UtopiaCacheSupport(RewardMoonLightBoxHistory.class)
public class RewardMoonLightBoxHistoryPersistence extends AlpsStaticJdbcDao<RewardMoonLightBoxHistory, Long> {

    @Override
    protected void calculateCacheDimensions(RewardMoonLightBoxHistory document, Collection<String> dimensions) {
        dimensions.add(RewardMoonLightBoxHistory.ck_userId(document.getUserId()));
        dimensions.add(RewardMoonLightBoxHistory.ck_userId_awardId(document.getUserId(), document.getAwardId()));
    }

    @UtopiaCacheable
    public List<RewardMoonLightBoxHistory> loadByUserIdAndAwardId(@UtopiaCacheKey(name = "userId") Long userId,
                                                                  @UtopiaCacheKey(name = "awardId") Integer awardId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId)
                .and("AWARD_ID").is(awardId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<RewardMoonLightBoxHistory>> loadByUserIds(@CacheParameter(value = "userId", multiple = true)
                                                                            Collection<Long> userIds) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return query(Query.query(criteria).with(sort)).stream()
                .collect(Collectors.groupingBy(RewardMoonLightBoxHistory::getUserId));
    }

    // 生成发货单查询
    public List<RewardMoonLightBoxHistory> loadByAwardIdAndTime(Integer awardId, Date startDate, Date endDate) {
        Criteria criteria = Criteria.where("AWARD_ID").is(awardId)
                .and("CREATE_DATETIME").gte(startDate).lte(endDate);
        return query(Query.query(criteria));
    }
}
