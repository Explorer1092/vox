package com.voxlearning.utopia.service.reward.impl.dao.newversion;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTagRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-10-10 14:10
 **/
@Named
@CacheBean(type = ProductTagRef.class)
public class ProductTagRefDao extends AlpsStaticJdbcDao<ProductTagRef, Long> {

    @Override
    protected void calculateCacheDimensions(ProductTagRef productTagRef, Collection<String> collection) {
        collection.add(CacheKeyGenerator.generateCacheKey(ProductTagRef.class, "TID", productTagRef.getTagId()));
        collection.add(CacheKeyGenerator.generateCacheKey(ProductTagRef.class, "PID", productTagRef.getProductId()));
    }

    public List<ProductTagRef> loadAll() {
        Query query = new Query(Criteria.where("DISABLED").is(false));
        return query(query);
    }

    @CacheMethod
    public List<ProductTagRef> loadByTagId(@CacheParameter("TID")Long tagId) {
        Criteria criteria = Criteria.where("TAG_ID").is(tagId).and("DISABLED").is(false);
        return query(new Query(criteria));
    }

    @CacheMethod
    public Map<Long, List<ProductTagRef>> loadByTagIdList(@CacheParameter(value = "TID", multiple = true) Collection<Long> tagIdList) {
        Criteria criteria = Criteria.where("TAG_ID").in(tagIdList).and("DISABLED").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(ProductTagRef::getTagId,Collectors.toList()));
    }

    @CacheMethod
    public List<ProductTagRef> loadByProductId(@CacheParameter("PID")Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        return query(new Query(criteria));
    }

    public Integer deleteByProductId(Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("DISABLED").is(false);
        List<ProductTagRef> originals = query(new Query(criteria));
        for (ProductTagRef original : originals) {
            original.setDisabled(true);
            upsert(original);
        }
        return originals.size();
    }

    public Integer deleteByProductIdAndTagIds(Long productId, Collection<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return 0;
        }
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId).and("TAG_ID").in(tagIds).and("DISABLED").is(false);
        List<ProductTagRef> originals = query(new Query(criteria));
        for (ProductTagRef original : originals) {
            original.setDisabled(true);
            upsert(original);
        }
        return originals.size();
    }

    public Integer deleteByTagId(Long tagId) {
        Criteria criteria = Criteria.where("TAG_ID").is(tagId).and("DISABLED").is(false);
        List<ProductTagRef> originals = query(new Query(criteria));
        for (ProductTagRef original : originals) {
            original.setDisabled(true);
            upsert(original);
        }
        return originals.size();
    }

}
