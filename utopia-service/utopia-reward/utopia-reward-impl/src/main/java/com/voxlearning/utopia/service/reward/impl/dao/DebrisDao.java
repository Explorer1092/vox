package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.Debris;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = Debris.class)
public class DebrisDao extends AlpsStaticJdbcDao<Debris, String> {

    @Override
    protected void calculateCacheDimensions(Debris debris, Collection<String> collection) {
        collection.addAll(Arrays.asList(debris.generateCacheDimensions()));
    }

    @CacheMethod
    public Debris loadByUserId(@CacheParameter("U") Long userId) {
        Criteria criteria = Criteria.where("USER_ID").is(userId);
        return query(new Query(criteria)).stream().findFirst().orElse(null);
    }

}
