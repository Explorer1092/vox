package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.activity.TangramActivityStudent;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 七巧板活动学生相关 DAO
 * Created by Yuechen.Wang on 2017/12/08.
 */
@Named
@CacheBean(type = TangramActivityStudent.class)
public class TangramActivityStudentDao extends StaticCacheDimensionDocumentJdbcDao<TangramActivityStudent, Long> {

    @CacheMethod
    public List<TangramActivityStudent> findByTeacher(@CacheParameter("T") Long teacherId) {
        if (teacherId == null || teacherId == 0L) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("TEACHER_ID").is(teacherId)
                .and("DISABLED").is(false);

        return query(Query.query(criteria).with(new Sort(Sort.Direction.ASC, "ID")));
    }

    @CacheMethod
    public List<TangramActivityStudent> findBySchool(@CacheParameter("S") Long schoolId) {
        if (schoolId == null || schoolId == 0L) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId)
                .and("DISABLED").is(false);

        return query(Query.query(criteria).with(new Sort(Sort.Direction.ASC, "ID")));
    }

    public int disable(Long studentId) {
        if (studentId == null || studentId <= 0L) {
            return 0;
        }
        TangramActivityStudent original = $load(studentId);
        if (original == null) {
            return 0;
        }
        Criteria criteria = Criteria.where("ID").is(studentId);
        Update update = Update.update("DISABLED", true)
                .currentDate("UPDATE_TIME");

        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);
        }
        return rows;
    }
}
