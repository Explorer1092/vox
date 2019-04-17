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
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.DynamicCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.document.UserGrowthRewardLog;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 10/8/2016
 */
@Named("com.voxlearning.utopia.service.action.impl.dao.UserGrowthRewardLogDao")
@CacheBean(type = UserGrowthRewardLog.class)
public class UserGrowthRewardLogDao extends DynamicCacheDimensionDocumentMongoDao<UserGrowthRewardLog, String> {
    @Override
    protected String calculateDatabase(String template, UserGrowthRewardLog document) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, UserGrowthRewardLog document) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(document.getId());
        String[] ids = StringUtils.split(document.getId(), "-");
        if (ids.length != 2) throw new IllegalArgumentException();
        Long userId = SafeConverter.toLong(ids[0]);
        if (0 == userId) throw new IllegalArgumentException();
        long mod = userId % 100;
        return StringUtils.formatMessage(template, mod);
    }

    @CacheMethod
    public List<UserGrowthRewardLog> findByUserId(@CacheParameter("UID") Long userId) {
        MongoConnection connection = calculateMongoConnection(userId);
        Pattern pattern = Pattern.compile("^" + userId + "-");
        Criteria criteria = Criteria.where("_id").regex(pattern);
        return executeQuery(connection, Query.query(criteria)).stream()
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
                .collect(Collectors.toList());
    }

    private MongoConnection calculateMongoConnection(Long userId) {
        String mockId = userId + "-0";
        MongoNamespace mongoNamespace = calculateIdMongoNamespace(mockId);
        return createMongoConnection(mongoNamespace);
    }
}
