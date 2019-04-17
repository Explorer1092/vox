package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = ProductTag.class)
public class ProductTagDao extends AlpsStaticJdbcDao<ProductTag, Long> {

    @Inject private ProductTagVersion productTagVersion;

    @Override
    protected void calculateCacheDimensions(ProductTag productTag, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(ProductTag.class, productTag.getId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductTag.class, new String[] {"PID"}, new Object[] {productTag.getParentId()}));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductTag.class, "ALL"));
    }

    @CacheMethod(key = "ALL")
    public List<ProductTag> loadAll() {
        Query query = new Query(Criteria.where("DISABLED").is(false));
        return query(query);
    }

    @Override
    public ProductTag upsert(ProductTag document) {
        ProductTag upsert = super.upsert(document);
        productTagVersion.increment();
        return upsert;
    }

    public Boolean disable(Long id) {
        ProductTag load = load(id);
        if (load != null) {
            load.setDisabled(true);
            upsert(load);
        }
        return load != null;
    }

}
