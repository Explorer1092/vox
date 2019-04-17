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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.action.api.document.UserAchievementLog;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author xinxin
 * @since 12/8/2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.UserAchievementLogDao")
@CacheBean(type = UserAchievementLog.class, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class UserAchievementLogDao extends DynamicCacheDimensionDocumentMongoDao<UserAchievementLog, String> {
    @Override
    protected String calculateDatabase(String template, UserAchievementLog document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserAchievementLog document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = StringUtils.split(document.getId(), "-");
        if (ids.length != 3) throw new IllegalArgumentException();
        long userId = SafeConverter.toLong(ids[0]);
        if (userId == 0) throw new IllegalArgumentException();
        return StringUtils.formatMessage(template, userId % 100);
    }

    @CacheMethod
    public List<UserAchievementLog> findByUserId(@CacheParameter("UID") Long userId) {
        MongoConnection connection = calculateMongoConnection(userId);
        Pattern pattern = Pattern.compile("^" + userId + "-");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        Sort sort = new Sort(Sort.Direction.ASC, "type", "level");
        Query query = Query.query(criteria).with(sort);
        return executeQuery(connection, query);
    }

    private MongoConnection calculateMongoConnection(Long userId) {
        String mockId = userId + "-0-0";
        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(mongoNamespace);
    }
}
