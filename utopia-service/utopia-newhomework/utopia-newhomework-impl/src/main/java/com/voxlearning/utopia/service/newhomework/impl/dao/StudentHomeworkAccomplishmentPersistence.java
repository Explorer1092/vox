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
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkLocation;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkAccomplishment;
import org.springframework.dao.DataAccessException;

import javax.inject.Named;
import java.util.*;

/**
 * Default {@link StudentHomeworkAccomplishment} persistence implementation.
 * move from homework by xuesong.zhang
 *
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @author Guohong Tan
 * @author xuesong.zhang
 * @since 2013-08-06 10:33
 */
@Named
@UtopiaCacheSupport(StudentHomeworkAccomplishment.class)
public class StudentHomeworkAccomplishmentPersistence extends StaticPersistence<Long, StudentHomeworkAccomplishment> {

    @Override
    protected void calculateCacheDimensions(StudentHomeworkAccomplishment source, Collection<String> dimensions) {
    }

    @Override
    public Long persist(final StudentHomeworkAccomplishment entity) throws DataAccessException {
        Long id = super.persistIntoDatabase(entity);
        entity.setId(id);
        if (entity.getDisabled() == null) entity.setDisabled(Boolean.FALSE);
        if (entity.getIp() == null) entity.setIp("");
        if (entity.getRepair() == null) entity.setRepair(Boolean.FALSE);
        String cacheKey = StudentHomeworkAccomplishment.ck_location(entity.toHomeworkLocation());
        getCache().getCacheObjectModifier().modify(cacheKey, entityCacheExpirationInSeconds(),
                new ChangeCacheObject<List<StudentHomeworkAccomplishment>>() {
                    @Override
                    public List<StudentHomeworkAccomplishment> changeCacheObject(List<StudentHomeworkAccomplishment> currentValue) {
                        currentValue = new LinkedList<>(currentValue);
                        currentValue.add(entity);
                        return currentValue;
                    }
                });
        return id;
    }

    @Override
    public Collection<Long> persist(Collection<StudentHomeworkAccomplishment> entities) throws DataAccessException {
        throw new UnsupportedOperationException();
    }

    @UtopiaCacheable
    public List<StudentHomeworkAccomplishment> findByLocation(@UtopiaCacheKey(name = "L") HomeworkLocation location) {
        String sql = "WHERE HOMEWORK_ID=? AND SUBJECT=?";
        return withSelectFromTable(sql)
                .useParamsArgs(location.getHomeworkId(), location.getSubject().name())
                .queryAll();
    }

    public List<String> findByAccomplishTime(Date startDate, Date endDate, Subject subject) {
        return withSelectFromTable("DISTINCT HOMEWORK_ID",
                "WHERE ACCOMPLISH_TIME>=? and ACCOMPLISH_TIME<=? and SUBJECT=?")
                .useParamsArgs(startDate, endDate, subject).queryColumnValues(String.class);
    }

    public List<StudentHomeworkAccomplishment> findByStudentIdAndAccomplishTime(Long studentId, Date dayStart) {
        return withSelectFromTable("WHERE STUDENT_ID=? AND ACCOMPLISH_TIME>=?")
                .useParamsArgs(studentId, dayStart).queryAll();
    }

    public Integer countByStudentIdAndAccomplishTime(Long studentId, Date start, Date end) {
        String sql = "SELECT COUNT(DISTINCT ID) FROM VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT WHERE STUDENT_ID=? AND ACCOMPLISH_TIME>? AND ACCOMPLISH_TIME<=?";
        return utopiaSql.withSql(sql).useParamsArgs(studentId, start, end).queryValue(Integer.class);
    }

    public List<Long> findUserIdByAccomplishTime(Date startDate, Date endDate) {
        return withSelectFromTable("DISTINCT STUDENT_ID",
                "WHERE ACCOMPLISH_TIME>=? and ACCOMPLISH_TIME<=? ")
                .useParamsArgs(startDate, endDate).queryColumnValues(Long.class);
    }

    public void deletes(List<Long> ids) {
        StringBuilder sql = new StringBuilder("delete from VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT where ID in (");
        if (ids.size() > 0) {
            for (int i = 0; i < ids.size(); i++) {
                sql.append("?,");
            }
            sql.delete(sql.length() - 1, sql.length()).append(")");
        } else {
            return;
        }
        utopiaSql.withSql(sql.toString()).useParams(ids).executeUpdate();
    }
}
