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
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.entity.activity.TeacherActivityProgress;

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
@CacheBean(type = TeacherActivityProgress.class)
public class TeacherActivityProgressPersistence extends StaticCacheDimensionDocumentJdbcDao<TeacherActivityProgress, Long> {

    @CacheMethod(key = "top100")
    public List<TeacherActivityProgress> loadTop100ByRank() {
        Criteria criteria = Criteria.where("DAILY_RANK").lte(100);
        return query(Query.query(criteria))
                .stream()
                .filter(t -> t.getRank() > 0)
                .sorted(Comparator.comparingInt(TeacherActivityProgress::getRank))
                .collect(Collectors.toList());
    }

    // This is only for job
    public List<Long> loadAllTeacherId() {
        String sql = " SELECT `ID` FROM `VOX_TEACHER_ACTIVITY_TERM_2017`; ";
        return getDataSourceConnection()
                .getJdbcTemplate()
                .queryForList(sql, Long.class);
    }

    public int updateRank(Long id, Integer rank) {
        if (id == null) {
            return 0;
        }
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("DAILY_RANK", rank);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            TeacherActivityProgress mock = new TeacherActivityProgress(id);
            evictDocumentCache(mock);
        }
        return rows;
    }

    // This is only for job
    public List<Long> loadRankTop10000() {
        String sql = " SELECT `ID` FROM `VOX_TEACHER_ACTIVITY_TERM_2017` WHERE `AUTH_STU_CNT` > 0 " +
                " ORDER BY `AUTH_STU_CNT` DESC , `CREATE_TIME` ASC " +
                " LIMIT 10000; ";
        return getDataSourceConnection()
                .getJdbcTemplate()
                .queryForList(sql, Long.class);
    }

    public void clearRankTop100Cache() {
        String cacheKey = CacheKeyGenerator.generateCacheKey(TeacherActivityProgress.class, "top100");
        getCache().delete(cacheKey);
    }

}
