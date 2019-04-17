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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.content.KnowledgePoint;

import javax.inject.Named;
import java.util.*;

/**
 * @author xin.xin
 * @since 2014-04-01
 */
@Named("business.KnowledgePointPersistence")
@CacheBean(type = KnowledgePoint.class)
public class KnowledgePointPersistence extends AlpsStaticJdbcDao<KnowledgePoint, Long> {

    @Override
    protected void calculateCacheDimensions(KnowledgePoint document, Collection<String> dimensions) {
        dimensions.add(KnowledgePoint.ck_id(document.getId()));
        dimensions.add(KnowledgePoint.ck_subjectId_newKnowledgePoint(document.getSubjectId(), document.getNewKnowledgePoint()));
        dimensions.add(KnowledgePoint.ck_pointName_pointType_newKnowledgePoint(document.getPointName(), document.getPointType(), document.getNewKnowledgePoint()));
        dimensions.add(KnowledgePoint.ck_subjectId_pointType_newKnowledgePoint(document.getSubjectId(), document.getPointType(), document.getNewKnowledgePoint()));
        dimensions.add(KnowledgePoint.ck_parentId_subjectId(document.getParentId(), document.getSubjectId()));
    }

    @UtopiaCacheable
    public List<KnowledgePoint> findBySubjectId(@UtopiaCacheKey(name = "subjectId") Integer subjectId,
                                                @UtopiaCacheKey(name = "isNew") boolean isNew) {
        Criteria criteria = Criteria.where("SUBJECT_ID").is(subjectId)
                .and("NEW_KNOWLEDGE_POINT").is(isNew)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable
    public KnowledgePoint findByNameAndType(@UtopiaCacheKey(name = "pointName") String pointName,
                                            @UtopiaCacheKey(name = "pointType") String pointType,
                                            @UtopiaCacheKey(name = "isNew") boolean isNew) {
        Criteria criteria = Criteria.where("POINT_TYPE").is(pointType)
                .and("BINARY POINT_NAME").is(pointName)
                .and("NEW_KNOWLEDGE_POINT").is(isNew)
                .and("DISABLED").is(false);
        return query(Query.query(criteria).limit(1)).stream().findFirst().orElse(null);
    }

    @UtopiaCacheable
    public List<KnowledgePoint> findBySubjectAndType(@UtopiaCacheKey(name = "subjectId") Integer subjectId,
                                                     @UtopiaCacheKey(name = "pointType") String pointType,
                                                     @UtopiaCacheKey(name = "isNew") boolean isNew) {
        Criteria criteria = Criteria.where("SUBJECT_ID").is(subjectId)
                .and("POINT_TYPE").is(pointType)
                .and("NEW_KNOWLEDGE_POINT").is(isNew)
                .and("DISABLED").is(false);
        Sort sort = new Sort(Sort.Direction.ASC, "PARENT_ID");
        return query(Query.query(criteria).with(sort));
    }

    @UtopiaCacheable
    public List<KnowledgePoint> findByParent(@UtopiaCacheKey(name = "parentId") Long parentId,
                                             @UtopiaCacheKey(name = "subjectId") Integer subjectId) {
        Criteria criteria = Criteria.where("SUBJECT_ID").is(subjectId)
                .and("PARENT_ID").is(parentId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public long findChangeCount(Integer subjectId, Date lastLoadTime) {
        Criteria criteria = Criteria.where("SUBJECT_ID").is(subjectId)
                .and("UPDATE_DATETIME").gt(lastLoadTime);
        return count(Query.query(criteria));
    }

    public int updatePointName(final Long id, final String pointName) {
        KnowledgePoint document = new KnowledgePoint();
        document.setId(id);
        document.setPointName(pointName);
        return replace(document) != null ? 1 : 0;
    }

    public int disable(final Long id) {
        KnowledgePoint original = $load(id);
        if (original == null) {
            return 0;
        }
        Update update = Update.update("DISABLED", true);
        Criteria criteria = Criteria.where("ID").is(id);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }

}
