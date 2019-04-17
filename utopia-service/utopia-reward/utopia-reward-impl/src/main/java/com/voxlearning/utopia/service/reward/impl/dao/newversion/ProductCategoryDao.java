package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = ProductCategory.class)
public class ProductCategoryDao extends AlpsStaticJdbcDao<ProductCategory, Long> {

    @Inject
    private ProductCategoryVersion productCategoryVersion;

    @Override
    protected void calculateCacheDimensions(ProductCategory productCategory, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(ProductCategory.class, productCategory.getId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductCategory.class, "ALL"));
    }

    @CacheMethod(key = "ALL")
    public List<ProductCategory> loadAll() {
        Query query = new Query(Criteria.where("DISABLED").is(false));
        return query(query);
    }

    @Override
    public ProductCategory upsert(ProductCategory document) {
        ProductCategory upsert = super.upsert(document);
        productCategoryVersion.increment();
        return upsert;
    }

    public Boolean disable(Long id) {
        ProductCategory load = load(id);
        if (load != null) {
            load.setDisabled(true);
            upsert(load);
        }
        return load != null;
    }
}
