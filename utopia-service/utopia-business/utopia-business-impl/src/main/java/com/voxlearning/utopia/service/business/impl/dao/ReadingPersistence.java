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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.api.constant.ReadingStatus;
import com.voxlearning.utopia.entity.content.Reading;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tanguohong on 14-6-20.
 */
@Named
@UtopiaCacheSupport(Reading.class)
public class ReadingPersistence extends AlpsStaticJdbcDao<Reading, Long> {

    @Override
    protected void calculateCacheDimensions(Reading document, Collection<String> dimensions) {
        dimensions.add(Reading.ck_id(document.getId()));
        dimensions.add(Reading.ck_status_difficultyLevel(document.getStatus(), document.getDifficultyLevel()));
        dimensions.add(Reading.ck_all());
        dimensions.add(Reading.ck_nullDraft());
    }

    @UtopiaCacheable(key = "allReadings")
    public List<Reading> getAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable(key = "nullDraftReadings")
    public List<Reading> getReadingDraft() {
        Criteria criteria = Criteria.where("DRAFT_ID").notExists();
        Sort sort = new Sort(Sort.Direction.DESC, "ID");
        return query(Query.query(criteria).with(sort));
    }

    @UtopiaCacheable
    public List<Reading> findByDifficultyLevel(@UtopiaCacheKey(name = "status") String status,
                                               @UtopiaCacheKey(name = "difficultyLevel") Integer difficultyLevel) {
        Criteria criteria = Criteria.where("STATUS").is(status)
                .and("DIFFICULTY_LEVEL").is(difficultyLevel);
        Sort sort = new Sort(Sort.Direction.DESC, "UPDATE_DATETIME");
        return query(Query.query(criteria).with(sort));
    }

    public int offlineReading(final String draftId) {
        Criteria criteria = Criteria.where("DRAFT_ID").is(draftId);
        List<Reading> originals = query(Query.query(criteria));
        if (originals.isEmpty()) {
            return 0;
        }
        Update update = Update.update("STATUS", ReadingStatus.offline);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            originals.forEach(e -> calculateCacheDimensions(e, cacheKeys));
            getCache().delete(cacheKeys);
        }
        return rows;
    }
}
