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

package com.voxlearning.utopia.admin.dao.feedback;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;

import javax.inject.Named;
import java.util.*;

@Named("com.voxlearning.utopia.admin.dao.feedback.UserFeedbackPersistence")
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

    /**
     * Never change USER_ID in this method.
     *
     * @param update   the update.
     * @param criteria the criteria.
     */
    public void executeUpdate(Update update, Criteria criteria) {
        Objects.requireNonNull(update);
        Objects.requireNonNull(criteria);

        Query query = Query.query(criteria);
        query.field().includes("ID", "USER_ID");
        List<UserFeedback> originals = query(query);
        if (originals.isEmpty()) {
            return;
        }
        if ($update(update, criteria) > 0) {
            Set<String> cacheKeys = new HashSet<>();
            originals.forEach(e -> calculateCacheDimensions(e, cacheKeys));
            getCache().delete(cacheKeys);
        }
    }
}
