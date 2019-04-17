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
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * {@link UserFeedback} persistence implementation.
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-21
 */
@Named("com.voxlearning.utopia.service.feedback.impl.dao.UserFeedbackPersistence")
@CacheBean(type = UserFeedback.class)
public class UserFeedbackPersistence extends AlpsStaticJdbcDao<UserFeedback, Long> {
    @Override
    protected void calculateCacheDimensions(UserFeedback document, Collection<String> dimensions) {
        dimensions.add(UserFeedback.ck_id(document.getId()));
        dimensions.add(UserFeedback.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<UserFeedback> findByUserId(@CacheParameter("userId") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(Query.query(criteria));
    }
}
