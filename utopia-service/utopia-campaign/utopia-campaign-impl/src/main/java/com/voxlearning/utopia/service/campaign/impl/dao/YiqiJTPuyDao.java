package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTPuy;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class YiqiJTPuyDao  extends AlpsStaticJdbcDao<YiqiJTPuy, Long> {

    @Override
    protected void calculateCacheDimensions(YiqiJTPuy document, Collection<String> dimensions) {
        dimensions.add(YiqiJTPuy.ck_user_course(document.getUserId(),document.getCourseId()));
    }

    @CacheMethod
    public YiqiJTPuy loadOne(@CacheParameter("USER_ID") long userId, @CacheParameter("COURSE_ID") long curseId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId).and("COURSE_ID").is(curseId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
