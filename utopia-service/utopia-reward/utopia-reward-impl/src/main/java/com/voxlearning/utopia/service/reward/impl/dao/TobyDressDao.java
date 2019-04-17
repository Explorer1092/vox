package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyDress;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TobyDress.class)
public class TobyDressDao extends AlpsStaticJdbcDao<TobyDress, Long> {
    @Override
    protected void calculateCacheDimensions(TobyDress document, Collection<String> dimensions) {
        dimensions.add(TobyDress.ck_all());
        dimensions.add(TobyDress.ck_Id(document.getId()));
    }

    @CacheMethod(key = "ALL")
    public List<TobyDress> loadAll() {
        return query();
    }

    @CacheMethod
    public TobyDress loadOne(@CacheParameter(value = "ID") long id) {
        return this.load(id);
    }

}
