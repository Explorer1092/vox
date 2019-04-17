package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCategory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2016/8/26.
 */
@Named
@CacheBean(type = MizarCategory.class)
public class MizarCategoryDao extends AlpsStaticMongoDao<MizarCategory, String> {
    @Override
    protected void calculateCacheDimensions(MizarCategory document, Collection<String> dimensions) {
        dimensions.add(CacheKeyGenerator.generateCacheKey(MizarCategory.class, "all"));
    }

    @CacheMethod(key = "all")
    public List<MizarCategory> findAll() {
        return query();
    }
}
