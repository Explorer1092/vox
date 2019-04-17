package com.voxlearning.utopia.service.campaign.impl.dao;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.MoralMedal;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@CacheBean(type = MoralMedal.class)
public class MoralMedalDao extends StaticCacheDimensionDocumentJdbcDao<MoralMedal, Long> {

    @CacheMethod
    public List<MoralMedal> loadByStudentId(@CacheParameter("SID") Long studentId) {
        Criteria criteria = Criteria.where("DISABLED").is(false)
                .and("STUDENT_ID").is(studentId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<MoralMedal>> loadByStudentIds(@CacheParameter(multiple = true, value = "SID") Collection<Long> studentIds) {
        Criteria criteria = Criteria.where("DISABLED").is(false)
                .and("STUDENT_ID").in(studentIds);
        List<MoralMedal> query = query(Query.query(criteria));
        return query.stream().collect(Collectors.groupingBy(MoralMedal::getStudentId));
    }

    @CacheMethod
    public List<MoralMedal> loadByTeacherIdGroupId(@CacheParameter(value = "TID") Long teacherId, @CacheParameter("CID") Long groupId) {
        Criteria criteria = Criteria.where("DISABLED").is(false)
                .and("TEACHER_ID").is(teacherId)
                .and("CLAZZ_ID").is(groupId);
        return query(Query.query(criteria));
    }
}
