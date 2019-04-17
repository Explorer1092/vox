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

package com.voxlearning.utopia.service.afenti.impl.dao;


import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserBookRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Persistence implementation of {@code AfentiLearningPlanUserBookRef} data structure.
 *
 * @author Maofeng Lu
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @since 2013-09-09 11:47AM
 */
@Named
@UtopiaCacheSupport(value = AfentiLearningPlanUserBookRef.class, cacheSystem = CacheSystem.CBS)
public class AfentiLearningPlanUserBookRefPersistence extends StaticPersistence<Long, AfentiLearningPlanUserBookRef> {

    @Override
    protected void calculateCacheDimensions(AfentiLearningPlanUserBookRef source, Collection<String> dimensions) {
        dimensions.add(AfentiLearningPlanUserBookRef.ck_uid_s(source.getUserId(), source.getSubject()));
    }

    @UtopiaCacheable
    public List<AfentiLearningPlanUserBookRef> findByUserIdAndSubject(@UtopiaCacheKey(name = "UID") Long userId,
                                                                      @UtopiaCacheKey(name = "S") Subject subject) {
        if (userId == null) return Collections.emptyList();
        return withSelectFromTable("WHERE USER_ID=? AND SUBJECT=?").useParamsArgs(userId, subject).queryAll();
    }

    public boolean inactivate(Long userId, Subject subject, AfentiLearningType type) {
        String sql = "UPDATE VOX_AFENTI_LEARNING_PLAN_USER_BOOK_REF SET ACTIVE=FALSE,UPDATETIME=NOW() " +
                "WHERE USER_ID=? AND SUBJECT=? AND TYPE=? AND ACTIVE=TRUE";
        int rows = getUtopiaSql().getJdbcOperations().update(sql, userId, subject.name(), type.name());
        if (rows > 0) getCache().delete(AfentiLearningPlanUserBookRef.ck_uid_s(userId, subject));
        return rows > 0;
    }

    public boolean inactivate(Long userId, Subject subject, String newBookId) {
        String sql = "UPDATE VOX_AFENTI_LEARNING_PLAN_USER_BOOK_REF SET ACTIVE=FALSE,UPDATETIME=NOW() " +
                "WHERE USER_ID=? AND NEW_BOOK_ID=? AND SUBJECT=? AND ACTIVE=TRUE";
        int rows = getUtopiaSql().getJdbcOperations().update(sql, userId, newBookId, subject.name());
        if (rows > 0) getCache().delete(AfentiLearningPlanUserBookRef.ck_uid_s(userId, subject));
        return rows > 0;
    }

    public boolean inactivate(Long userId, Subject subject, String newBookId, AfentiLearningType type) {
        String sql = "UPDATE VOX_AFENTI_LEARNING_PLAN_USER_BOOK_REF SET ACTIVE=FALSE,UPDATETIME=NOW() " +
                "WHERE USER_ID=? AND NEW_BOOK_ID=? AND SUBJECT=? AND TYPE=? AND ACTIVE=TRUE";
        int rows = getUtopiaSql().getJdbcOperations().update(sql, userId, newBookId, subject.name(), type.name());
        if (rows > 0) getCache().delete(AfentiLearningPlanUserBookRef.ck_uid_s(userId, subject));
        return rows > 0;
    }

    public boolean activate(Long userId, Subject subject, String newBookId, AfentiLearningType type) {
        String sql = "UPDATE VOX_AFENTI_LEARNING_PLAN_USER_BOOK_REF SET ACTIVE=TRUE,UPDATETIME=NOW() " +
                "WHERE USER_ID=? AND NEW_BOOK_ID=? AND SUBJECT=? AND SUBJECT=?";
        int rows = getUtopiaSql().getJdbcOperations().update(sql, userId, newBookId, subject.name(), type);
        if (rows > 0) getCache().delete(AfentiLearningPlanUserBookRef.ck_uid_s(userId, subject));
        return rows > 0;
    }
}
