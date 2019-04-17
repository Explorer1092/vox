package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategoryRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-10 14:10
 **/
@Named
@CacheBean(type = ProductCategoryRef.class)
public class ProductCategoryRefDao extends AlpsStaticJdbcDao<ProductCategoryRef, Long> {

    @Override
    protected void calculateCacheDimensions(ProductCategoryRef productCategoryRef, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(ProductCategoryRef.class, "CID", productCategoryRef.getCategoryId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductCategoryRef.class, "PID", productCategoryRef.getProductId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductCategoryRef.class, "ALL"));
    }

    @CacheMethod(key = "ALL")
    public List<ProductCategoryRef> loadAll() {
        Query query = new Query(Criteria.where("DISABLED").is(false));
        return query(query);
    }

    public Integer deleteByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        List<ProductCategoryRef> originals = query(new Query(criteria));
        for (ProductCategoryRef original : originals) {
            original.setDisabled(true);
            upsert(original);
        }
        return originals.size();
    }

    @CacheMethod
    public List<ProductCategoryRef> loadByCategoryId(@CacheParameter("CID")Long categoryId) {
        Criteria criteria = Criteria.where("CATEGORY_ID").is(categoryId).and("DISABLED").is(false);
        return query(new Query(criteria));
    }

    @CacheMethod
    public ProductCategoryRef loadByProductId(@CacheParameter("PID")Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        return query(new Query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, List<ProductCategoryRef>> loadByCategoryIds(@CacheParameter(value = "CID", multiple = true) Collection<Long> categoryIds) {
        Criteria criteria = Criteria.where("CATEGORY_ID").in(categoryIds).and("DISABLED").is(false);
        List<ProductCategoryRef> result = query(new Query(criteria));
        if (CollectionUtils.isEmpty(result)) {
            return Collections.emptyMap();
        }
        return result.stream().collect(Collectors.groupingBy(ProductCategoryRef::getCategoryId, Collectors.toList()));
    }
}
