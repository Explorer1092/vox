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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkStat;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import javax.inject.Named;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Persistence implementation of {@link StudentHomeworkStat} entity.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @version 0.1
 * @since 14-1-13
 */
@Named
@UtopiaCacheSupport(StudentHomeworkStat.class)
public class StudentHomeworkStatPersistence extends StaticPersistence<Long, StudentHomeworkStat> {

    @Override
    protected void calculateCacheDimensions(StudentHomeworkStat source, Collection<String> dimensions) {
        dimensions.add(StudentHomeworkStat.ck_teacherId_clazzId_studentId(source.getTeacherId(), source.getClazzId(), source.getStudentId()));
        dimensions.add(StudentHomeworkStat.ck_clazzId(source.getClazzId()));
        dimensions.add(StudentHomeworkStat.ck_teacherId_clazzId(source.getTeacherId(), source.getClazzId()));
        dimensions.add(StudentHomeworkStat.ck_teacherId(source.getTeacherId()));
    }

    @Override
    public Long persist(final StudentHomeworkStat entity) throws DataAccessException {
        Long id = super.persistIntoDatabase(entity);
        entity.setId(id);
        if (entity.getFinishHomeworkCount() == null) entity.setFinishHomeworkCount(0L);
        if (entity.getFinishQuizCount() == null) entity.setFinishQuizCount(0L);
        if (entity.getFinishSubjectiveCount() == null) entity.setFinishSubjectiveCount(0L);
        if (entity.getFinishOralCount() == null) entity.setFinishOralCount(0L);
        if (entity.getFinishO2OOfflineCount() == null) entity.setFinishO2OOfflineCount(0L);
        if (entity.getFinishO2OOnlineCount() == null) entity.setFinishO2OOnlineCount(0L);
        if (entity.getFinishVhCount() == null) entity.setFinishVhCount(0L);

        String ck = StudentHomeworkStat.ck_teacherId_clazzId_studentId(entity.getTeacherId(), entity.getClazzId(), entity.getStudentId());
        getCache().safeAdd(ck, entityCacheExpirationInSeconds(), entity);

        ck = StudentHomeworkStat.ck_clazzId(entity.getClazzId());
        getCache().getCacheObjectModifier().modify(ck, entityCacheExpirationInSeconds(), currentValue -> CollectionUtils.addToSet(currentValue, entity.toUnique()));

        ck = StudentHomeworkStat.ck_teacherId_clazzId(entity.getTeacherId(), entity.getClazzId());
        getCache().getCacheObjectModifier().modify(ck, entityCacheExpirationInSeconds(), currentValue -> CollectionUtils.addToSet(currentValue, entity.toUnique()));

        ck = StudentHomeworkStat.ck_teacherId(entity.getTeacherId());
        getCache().getCacheObjectModifier().modify(ck, entityCacheExpirationInSeconds(), currentValue -> CollectionUtils.addToSet(currentValue, entity.toUnique()));

        return id;
    }

    @UtopiaCacheable
    public StudentHomeworkStat load(@UtopiaCacheKey(name = "T") Long teacherId,
                                    @UtopiaCacheKey(name = "C") Long clazzId,
                                    @UtopiaCacheKey(name = "S") Long studentId) {
        return withSelectFromTable("WHERE TEACHER_ID=? AND CLAZZ_ID=? AND STUDENT_ID=?")
                .useParamsArgs(teacherId, clazzId, studentId)
                .queryObject();
    }

    public Map<String, StudentHomeworkStat> yetAnotherLoads(Collection<String> uniques) {
        if (CollectionUtils.isEmpty(uniques)) return Collections.emptyMap();
        CacheObjectLoader.Loader<String, StudentHomeworkStat> loader = getCache()
                .getCacheObjectLoader().createLoader(source -> {
                    String[] segments = StringUtils.split(source, "-");
                    long t = NumberUtils.toLong(segments[0]);
                    long c = NumberUtils.toLong(segments[1]);
                    long s = NumberUtils.toLong(segments[2]);
                    return StudentHomeworkStat.ck_teacherId_clazzId_studentId(t, c, s);
                });
        return loader.loads(uniques).loadsMissed(this::internalLoads).write(entityCacheExpirationInSeconds()).getResult();
    }

    @UtopiaCacheable
    public Collection<String> queryIdsByClazzId(@UtopiaCacheKey(name = "C") final Long clazzId) {
        String sql = "SELECT TEACHER_ID,STUDENT_ID " +
                "FROM VOX_STUDENT_HOMEWORK_STAT " +
                "WHERE CLAZZ_ID=?";
        Collection<String> result = new LinkedHashSet<>();
        List<String> uniques = utopiaSql.withSql(sql).useParamsArgs(clazzId).queryAll((resultSet, i) -> {
            long t = resultSet.getLong("TEACHER_ID");
            long s = resultSet.getLong("STUDENT_ID");
            return t + "-" + clazzId + "-" + s;
        });
        result.addAll(uniques);
        return result;
    }

    @UtopiaCacheable
    public Collection<String> queryIdsByTeacherId(@UtopiaCacheKey(name = "T") final Long teacherId) {
        String sql = "SELECT CLAZZ_ID,STUDENT_ID " +
                "FROM VOX_STUDENT_HOMEWORK_STAT " +
                "WHERE TEACHER_ID=?";
        Collection<String> result = new LinkedHashSet<>();
        List<String> uniques = utopiaSql.withSql(sql).useParamsArgs(teacherId).queryAll((resultSet, i) -> {
            long c = resultSet.getLong("CLAZZ_ID");
            long s = resultSet.getLong("STUDENT_ID");
            return teacherId + "-" + c + "-" + s;
        });
        result.addAll(uniques);
        return result;
    }


    @UtopiaCacheable
    public Collection<String> queryIdsByTeacherIdAndClazzId(@UtopiaCacheKey(name = "T") final Long teacherId,
                                                            @UtopiaCacheKey(name = "C") final Long clazzId) {
        String sql = "SELECT STUDENT_ID " +
                "FROM VOX_STUDENT_HOMEWORK_STAT " +
                "WHERE TEACHER_ID=? AND CLAZZ_ID=?";
        Collection<String> result = new LinkedHashSet<>();
        List<String> uniques = utopiaSql.withSql(sql).useParamsArgs(teacherId, clazzId).queryAll(new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                long s = resultSet.getLong("STUDENT_ID");
                return teacherId + "-" + clazzId + "-" + s;
            }
        });
        result.addAll(uniques);
        return result;
    }

    public boolean incFinishHomeworkCount(Long teacherId, Long clazzId, Long studentId) {
        final Date current = new Date();
        String sql = "UPDATE VOX_STUDENT_HOMEWORK_STAT " +
                "SET UPDATE_DATETIME=?,FINISH_HOMEWORK_COUNT=FINISH_HOMEWORK_COUNT+1 " +
                "WHERE TEACHER_ID=? AND CLAZZ_ID=? AND STUDENT_ID=?";
        int rows = utopiaSql.withSql(sql).useParamsArgs(current, teacherId, clazzId, studentId).executeUpdate();
        if (rows > 0) {
            String cacheKey = StudentHomeworkStat.ck_teacherId_clazzId_studentId(teacherId, clazzId, studentId);
            getCache().getCacheObjectModifier().modify(cacheKey,
                    entityCacheExpirationInSeconds(), 5, new ChangeCacheObject<StudentHomeworkStat>() {
                        @Override
                        public StudentHomeworkStat changeCacheObject(StudentHomeworkStat currentValue) {
                            currentValue.setUpdateDatetime(current);
                            long c = currentValue.getFinishHomeworkCount() == null ? 0 : currentValue.getFinishHomeworkCount();
                            currentValue.setFinishHomeworkCount(c + 1);
                            return currentValue;
                        }
                    });
        }
        return rows > 0;
    }
    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private Map<String, StudentHomeworkStat> internalLoads(Collection<String> uniques) {
        if (CollectionUtils.isEmpty(uniques)) return Collections.emptyMap();
        Set<Long> teacherIds = new LinkedHashSet<>();
        Set<Long> clazzIds = new LinkedHashSet<>();
        Set<Long> studentIds = new LinkedHashSet<>();
        for (String unique : uniques) {
            String[] segments = StringUtils.split(unique, "-");
            long t = NumberUtils.toLong(segments[0]);
            long c = NumberUtils.toLong(segments[1]);
            long s = NumberUtils.toLong(segments[2]);
            teacherIds.add(t);
            clazzIds.add(c);
            studentIds.add(s);
        }
        Map<String, Object> parameters = new LinkedHashMap<>();
        String sql = " WHERE 1=1 ";
        if (CollectionUtils.isNotEmpty(teacherIds)) {
            parameters.put("teacherIds", teacherIds);
            sql+=" AND TEACHER_ID IN (:teacherIds) ";
        }
        if (CollectionUtils.isNotEmpty(clazzIds)) {
            parameters.put("clazzIds", clazzIds);
            sql+=" AND CLAZZ_ID IN (:clazzIds) ";
        }
        if (CollectionUtils.isNotEmpty(studentIds)) {
            parameters.put("studentIds", studentIds);
            sql+=" AND STUDENT_ID IN (:studentIds) ";
        }
        if (MapUtils.isEmpty(parameters)) {
            return Collections.emptyMap();
        }
        List<StudentHomeworkStat> list = withSelectFromTable(sql).useParams(parameters).queryAll();
        Map<String, StudentHomeworkStat> result = new LinkedHashMap<>();
        for (StudentHomeworkStat stat : list) {
            String unique = stat.toUnique();
            if (uniques.contains(unique)) {
                result.put(unique, stat);
            }
        }
        return result;
    }
}
