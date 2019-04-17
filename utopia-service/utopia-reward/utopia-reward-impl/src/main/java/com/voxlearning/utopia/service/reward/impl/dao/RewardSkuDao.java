/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.reward.entity.RewardSku;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Persistence implementation of entity {@link RewardSku}.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Jul 14, 2014
 */
@Named
@CacheBean(type = RewardSku.class, useValueWrapper = true)
public class RewardSkuDao extends StaticCacheDimensionDocumentJdbcDao<RewardSku, Long> {

    // override insert process to enable cache process
    @Override
    public void insert(RewardSku document) {
        super.insert(document);

        if (SafeConverter.toInt(document.getInventorySellable()) <= 0) {
            return;
        }

        updateInventoryProductsCache(document.getProductId());
    }

    // override replace process to enable cache process
    @Override
    public RewardSku replace(RewardSku document) {
        RewardSku updObject = super.replace(document);

        updateInventoryProductsCache(document.getProductId());

        return updObject;
    }

    // override remove process to enable cache process
    @Override
    public boolean remove(Long skuId) {
        boolean result = super.remove(skuId);

        if (result) {
            String ck = RewardSku.ck_has_invetory();
            getCache().delete(ck);
        }

        return result;
    }

    @CacheMethod
    public List<RewardSku> findByProductId(@CacheParameter(value = "productId") final Long productId) {
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<Long, List<RewardSku>> findByProductIds(@CacheParameter(value = "productId", multiple = true)
                                                       final Collection<Long> productIds) {
        Criteria criteria = Criteria.where("PRODUCT_ID").in(productIds);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(RewardSku::getProductId));
    }

    public List<Long> findHasInventoryProducts() {
        String ck = RewardSku.ck_has_invetory();
        List<Long> cachedProducts = getCache().load(ck);
        if (cachedProducts != null) {
            return cachedProducts;
        }

        Criteria criteria = Criteria.where("INVENTORY_SELLABLE").gt(0);
        List<RewardSku> rewardSkus = query(Query.query(criteria));
        List<Long> products = new LinkedList<>(rewardSkus.stream().collect(Collectors.groupingBy(RewardSku::getProductId)).keySet());

        getCache().set(ck, getDefaultCacheExpirationInSeconds(), products);
        return products;
    }

    /**
     * 增加库存量
     */
    public int increaseInventorySellable(final Long skuId, final int delta) {
        if (delta == 0) return 0;
        if (delta < 0) return decreaseInventorySellable(skuId, -delta);
        RewardSku original = $load(skuId);
        if (original == null) {
            return 0;
        }
        Update update = new Update().inc("INVENTORY_SELLABLE", delta);
        Criteria criteria = Criteria.where("ID").is(skuId);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);

            if (SafeConverter.toInt(original.getInventorySellable()) > 0) {
                updateInventoryProductsCache(original.getProductId());
            }
        }
        return rows;
    }

    /**
     * 减少库存量，库存量不能减到负数
     */
    public int decreaseInventorySellable(final Long skuId, final int delta) {
        if (delta == 0) return 0;
        if (delta < 0) return increaseInventorySellable(skuId, -delta);
        RewardSku original = $load(skuId);
        if (original == null) {
            return 0;
        }
        Update update = new Update().inc("INVENTORY_SELLABLE", -delta);
        Criteria criteria = Criteria.where("ID").is(skuId).and("INVENTORY_SELLABLE").gte(delta);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            calculateCacheDimensions(original, cacheKeys);
            getCache().delete(cacheKeys);

            if (SafeConverter.toInt(original.getInventorySellable()) - delta <= 0) {
                updateInventoryProductsCache(original.getProductId());
            }
        }
        return rows;
    }

    @CacheMethod(key = "ALL")
    public List<RewardSku> loadAll() {
        return query();
    }

    private void updateInventoryProductsCache(Long productId) {
        if (productId == null) return;

        // do nothing when no cache object
        String ck = RewardSku.ck_has_invetory();
        List<Long> cachedProducts = getCache().load(ck);
        if (cachedProducts == null) {
            return;
        }

        boolean addProduct = false;
        boolean removeProduct = true;

        List<RewardSku> skuList = findByProductId(productId);
        if (CollectionUtils.isNotEmpty(skuList)) {
            for (RewardSku sku : skuList) {
                if (SafeConverter.toInt(sku.getInventorySellable()) > 0) {
                    addProduct = true;
                    removeProduct = false;
                    break;
                }
            }
        }

        final boolean addOperator = addProduct;
        final boolean removeOperator = removeProduct;

        ChangeCacheObject<List<Long>> modifier = products -> {
            if (products == null) products = new LinkedList<>();

            if (addOperator && !products.contains(productId)) {
                products.add(productId);
            }

            if (removeOperator && products.contains(productId)) {
                products.remove(productId);
            }

            return products;
        };

        CacheValueModifierExecutor<List<Long>> executor = getCache().createCacheValueModifier();
        executor.key(ck)
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(modifier)
                .execute();
    }

}
