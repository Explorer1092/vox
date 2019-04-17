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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserFootprint;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Named
@CacheBean(type = AfentiLearningPlanUserFootprint.class, useValueWrapper = true)
public class AfentiLearningPlanUserFootprintPersistence extends StaticCacheDimensionDocumentJdbcDao<AfentiLearningPlanUserFootprint, Long> {

    @CacheMethod
    public AfentiLearningPlanUserFootprint findByUserIdAndSubject(@CacheParameter("UID") final Long userId,
                                                                  @CacheParameter("S") final Subject subject) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("SUBJECT").is(subject);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, AfentiLearningPlanUserFootprint> findByUserIdsAndSubject(@CacheParameter(value = "UID", multiple = true) Collection<Long> userIds,
                                                                              @CacheParameter("S") Subject subject) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds).and("SUBJECT").is(subject);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.toMap(AfentiLearningPlanUserFootprint::getUserId, Function.identity()));
    }

    public boolean update(AfentiLearningPlanUserFootprint entity) {
        Criteria criteria = Criteria.where("ID").is(entity.getId());
        Update update = Update.update("NEW_BOOK_ID", entity.getNewBookId())
                .set("NEW_UNIT_ID", entity.getNewUnitId())
                .set("RANK", entity.getRank());
        long rows = $update(update, criteria);
        if (rows > 0) {
            getCache().createCacheValueModifier()
                    .key(CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUserFootprint.class,
                            new String[]{"UID", "S"}, new Object[]{entity.getUserId(), entity.getSubject()}))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> {
                        if (currentValue instanceof AfentiLearningPlanUserFootprint) {
                            AfentiLearningPlanUserFootprint footprint = (AfentiLearningPlanUserFootprint) currentValue;
                            footprint.setNewBookId(entity.getNewBookId());
                            footprint.setNewUnitId(entity.getNewUnitId());
                            footprint.setRank(entity.getRank());
                            footprint.setUpdateTime(new Date());
                            return footprint;
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    })
                    .execute();
        }
        return rows > 0;
    }
}
