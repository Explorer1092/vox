package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeacherActivityRef.class, useValueWrapper = true)
public class TeacherActivityRefDao extends StaticMySQLPersistence<TeacherActivityRef, Long> {

    @Override
    protected void calculateCacheDimensions(TeacherActivityRef document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public TeacherActivityRef loadUserIdTypeId(@CacheParameter("UID") Long userId, @CacheParameter("TYPE") TeacherActivityEnum type) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("TYPE").is(type.name());
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
