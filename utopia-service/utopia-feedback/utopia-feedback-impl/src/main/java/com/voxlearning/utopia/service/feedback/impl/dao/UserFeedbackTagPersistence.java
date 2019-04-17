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

package com.voxlearning.utopia.service.feedback.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Shuai Huan on 2014/6/17.
 */
@Named("com.voxlearning.utopia.service.feedback.impl.dao.UserFeedbackTagPersistence")
@CacheBean(type = UserFeedbackTag.class)
public class UserFeedbackTagPersistence extends AlpsStaticJdbcDao<UserFeedbackTag, Long> {

    @Override
    protected void calculateCacheDimensions(UserFeedbackTag document, Collection<String> dimensions) {
        dimensions.add(UserFeedbackTag.ck_all());
    }

    @CacheMethod(key = "ALL")
    public List<UserFeedbackTag> findAllTags() {
        return query(Query.query(Criteria.where("DISABLED").is(false)));
    }
}
