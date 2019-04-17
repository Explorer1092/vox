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

package com.voxlearning.utopia.service.action.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.action.api.document.UserGrowthLog;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * DAO implementation of {@link UserGrowthLog}.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.UserGrowthLogDao")
@CacheBean(type = UserGrowthLog.class)
public class UserGrowthLogDao extends DynamicCacheDimensionDocumentMongoDao<UserGrowthLog, String> {
    @Override
    protected String calculateDatabase(String template, UserGrowthLog document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserGrowthLog document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String id = document.getId();
        String[] segments = StringUtils.split(id, "-");
        if (segments.length != 3) throw new IllegalArgumentException();
        long userId = SafeConverter.toLong(segments[0], -1);
        if (userId == -1) throw new IllegalArgumentException();
        return StringUtils.formatMessage(template, userId % 100);
    }

    /**
     * Find all logs belongs to specified user, latest 30 days available.
     *
     * @param userId the user id.
     * @return user growth log list, sort by action time DESC.
     */
    @CacheMethod
    public List<UserGrowthLog> findByUserId(@CacheParameter("UID") Long userId) {
        String mockId = SafeConverter.toLong(userId) + "-19700101-000000000000000000000000";
        MongoNamespace namespace = calculateIdMongoNamespace(mockId);
        MongoConnection connection = createMongoConnection(namespace);
        Date start = DayRange.newInstance(DateUtils.nextDay(new Date(), -30).getTime()).getStartDate();
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("actionTime").gte(start);
        Sort sort = new Sort(Sort.Direction.DESC, "actionTime");
        return executeQuery(connection, Query.query(criteria).with(sort));
    }
}
