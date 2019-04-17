package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductSetRef;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTagRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-10 14:10
 **/
@Named
@CacheBean(type = ProductSetRef.class)
public class ProductSetRefDao extends AlpsStaticJdbcDao<ProductSetRef, Long> {

    @Override
    protected void calculateCacheDimensions(ProductSetRef productSetRef, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(ProductSetRef.class, "SID", productSetRef.getSetId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductSetRef.class, "PID", productSetRef.getProductId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductSetRef.class, "ALL"));
    }

    @CacheMethod(key = "ALL")
    public List<ProductSetRef> loadAll() {
        Query query = new Query(Criteria.where("DISABLED").is(false));
        return query(query);
    }

    @CacheMethod
    public List<ProductSetRef> loadByProductId(@CacheParameter("PID")Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        return query(new Query(criteria));
    }

    @CacheMethod
    public List<ProductSetRef> loadBySetId(@CacheParameter("SID")Long setId) {
        Criteria criteria = Criteria.where("SET_ID").is(setId).and("DISABLED").is(false);
        return query(new Query(criteria));
    }

    public Integer deleteByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        List<ProductSetRef> originals = query(new Query(criteria));
        for (ProductSetRef original : originals) {
            original.setDisabled(true);
            upsert(original);
        }
        return originals.size();
    }

    public Integer deleteByProductIdAndSetIds(Long productId, Collection<Long> setIds) {
        if (CollectionUtils.isEmpty(setIds)) {
            return 0;
        }
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("SET_ID").in(setIds).and("DISABLED").is(false);
        List<ProductSetRef> originals = query(new Query(criteria));
        for (ProductSetRef original : originals) {
            original.setDisabled(true);
            upsert(original);
        }
        return originals.size();
    }

}
