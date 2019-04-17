package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.PublicGoodStyle;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PublicGoodStyle.class)
public class PublicGoodStyleDao extends AlpsStaticJdbcDao<PublicGoodStyle,Long>{

    @CacheMethod
    public List<PublicGoodStyle> loadByModel(@CacheParameter("MODEL") String model){
        Criteria criteria = Criteria.where("MODEL").is(model);
        return query(Query.query(criteria));
    }

    @CacheMethod(key = "ALL")
    public List<PublicGoodStyle> loadAll() {
        return query();
    }

    @Override
    protected void calculateCacheDimensions(PublicGoodStyle document, Collection<String> dimensions) {

    }
}
