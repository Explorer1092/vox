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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Maofeng Lu
 * @since 13-9-9 下午1:17
 */
@Named
@UtopiaCacheSupport(value = AfentiLearningPlanUnitRankManager.class, cacheSystem = CacheSystem.CBS)
public class AfentiLearningPlanUnitRankManagerPersistence extends StaticPersistence<Long, AfentiLearningPlanUnitRankManager> {

    @Override
    protected void calculateCacheDimensions(AfentiLearningPlanUnitRankManager source, Collection<String> dimensions) {
        dimensions.add(AfentiLearningPlanUnitRankManager.ck_nbid(source.getNewBookId()));
        dimensions.add(AfentiLearningPlanUnitRankManager.ck_all_nbid());
        dimensions.add(AfentiLearningPlanUnitRankManager.ck_all_nbid_preparation());
        dimensions.add(AfentiLearningPlanUnitRankManager.ck_all_nbid_review());
    }

    @UtopiaCacheable
    public List<AfentiLearningPlanUnitRankManager> findByNewBookId(@UtopiaCacheKey(name = "NBID") String bookId) {
        if (StringUtils.isBlank(bookId)) return Collections.emptyList();

        return withSelectFromTable("WHERE NEW_BOOK_ID=? AND RUNTIME_MODE>=? ORDER BY UNIT_RANK ").useParamsArgs(bookId, RuntimeMode.current().getLevel()).queryAll();
    }

    @UtopiaCacheable(key = "ALL")
    public List<String> findAllBookIds() {
        String sql = "SELECT DISTINCT NEW_BOOK_ID FROM VOX_AFENTI_LEARNING_PLAN_UNIT_RANK_MANAGER WHERE TYPE='" + AfentiLearningType.castle.name() + "'" +
                " AND RUNTIME_MODE>='" + RuntimeMode.current().getLevel() + "'";
        return utopiaSql.withSql(sql).queryColumnValues(String.class);
    }

    @UtopiaCacheable(key = "ALL:P")
    public List<String> findAllBookIdsForPreparation() {
        String sql = "SELECT DISTINCT NEW_BOOK_ID FROM VOX_AFENTI_LEARNING_PLAN_UNIT_RANK_MANAGER WHERE TYPE='" + AfentiLearningType.preparation.name() + "'" +
                " AND RUNTIME_MODE>='" + RuntimeMode.current().getLevel() + "'";
        return utopiaSql.withSql(sql).queryColumnValues(String.class);
    }

    @UtopiaCacheable(key = "ALL:R")
    public List<String> findAllBookIdsForReview() {
        String sql = "SELECT DISTINCT NEW_BOOK_ID FROM VOX_AFENTI_LEARNING_PLAN_UNIT_RANK_MANAGER WHERE TYPE='" + AfentiLearningType.review.name() + "'" +
                " AND RUNTIME_MODE>='" + RuntimeMode.current().getLevel() + "'";
        return utopiaSql.withSql(sql).queryColumnValues(String.class);
    }
}
