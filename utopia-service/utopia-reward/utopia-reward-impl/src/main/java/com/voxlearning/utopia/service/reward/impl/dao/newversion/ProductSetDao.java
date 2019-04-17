package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductSet;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = ProductSet.class)
public class ProductSetDao extends AlpsStaticJdbcDao<ProductSet, Long> {

    @Override
    protected void calculateCacheDimensions(ProductSet productSet, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(ProductSet.class, "ALL"));
    }

    @CacheMethod(key = "ALL")
    public List<ProductSet> loadAll() {
        Query query = new Query(Criteria.where("DISABLED").is(false));
        return query(query);
    }

    public Boolean disable(Long id) {
        ProductSet load = load(id);
        if (load != null) {
            load.setDisabled(true);
            upsert(load);
        }
        return load != null;
    }

}
