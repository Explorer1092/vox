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
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.activity.TeacherNewTermActivityProgress;

import javax.inject.Named;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 2017秋季开学老师端活动
 *
 * @author yuechen.wang
 * @since 2017-08-14
 */
@Named
@CacheBean(type = TeacherNewTermActivityProgress.class)
public class TeacherNewTermActivityProgressPersistence extends StaticCacheDimensionDocumentJdbcDao<TeacherNewTermActivityProgress, Long> {

    @CacheMethod
    public List<TeacherNewTermActivityProgress> loadTop100ByRank(@CacheParameter("top100") Long activityId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId)
                .and("DAILY_RANK").lte(100);
        return query(Query.query(criteria))
                .stream()
                .filter(t -> t.getRank() > 0)
                .sorted(Comparator.comparingInt(TeacherNewTermActivityProgress::getRank))
                .collect(Collectors.toList());
    }

    @CacheMethod
    public TeacherNewTermActivityProgress loadProgress(@CacheParameter("A") Long activityId, @CacheParameter("T") Long teacherId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId)
                .and("TEACHER_ID").is(teacherId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    // This is only for job
    public List<Long> loadAllTeacherId(Long activityId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId);
        Query query = Query.query(criteria);
        query.field().includes("TEACHER_ID");
        return query(query).stream().map(TeacherNewTermActivityProgress::getTeacherId).collect(Collectors.toList());
    }

    public int updateRank(Long id, Integer rank) {
        if (id == null) {
            return 0;
        }
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("DAILY_RANK", rank);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            TeacherNewTermActivityProgress mock = $load(id);
            evictDocumentCache(mock);
        }
        return rows;
    }

    // This is only for job
    public List<Long> loadRankTop100(Long activityId) {
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId)
                .and("AUTH_STU_CNT").gt(0);
        Sort stuSort = new Sort(Sort.Direction.DESC, "AUTH_STU_CNT");
        Sort ctSort = new Sort(Sort.Direction.ASC, "CREATE_TIME");
        Query query = Query.query(criteria)
                .with(stuSort.and(ctSort))
                .limit(100);
        query.field().includes("ID");
        return query(query).stream().map(TeacherNewTermActivityProgress::getId).collect(Collectors.toList());
    }

    public void clearRankTop100Cache(Long activityId) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(TeacherNewTermActivityProgress.class, "top100", activityId);
        getCache().delete(cacheKey);
    }

}
