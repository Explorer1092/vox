package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.PublicGoodElementType;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PublicGoodElementType.class)
public class PublicGoodElementTypeDao extends AlpsStaticJdbcDao<PublicGoodElementType,Long>{

    @Override
    protected void calculateCacheDimensions(PublicGoodElementType document, Collection<String> dimensions) {

    }

    @CacheMethod(key = "ALL")
    public List<PublicGoodElementType> loadAll(){
        return query();
    }

}
