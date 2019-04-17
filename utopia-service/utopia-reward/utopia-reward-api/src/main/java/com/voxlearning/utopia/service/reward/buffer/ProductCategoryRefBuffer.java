package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategoryRef;

import java.util.*;

public class ProductCategoryRefBuffer extends NearBuffer<List<ProductCategoryRef>> {

    private static final Set<Long> emptySet = new HashSet<>();

    private Map<Long, Set<Long>> productCategoryMap = new HashMap<>();
    private Map<Long, Set<Long>> categoryProductMap = new HashMap<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ProductCategoryRef> data) {
        productCategoryMap.clear();
        categoryProductMap.clear();

        data.forEach(item -> {
            Long productId = item.getProductId();
            Long categoryId = item.getCategoryId();
            if(productCategoryMap.containsKey(productId)) {
                productCategoryMap.get(productId).add(categoryId);
            } else {
                HashSet categoryIdSet = new HashSet();
                categoryIdSet.add(categoryId);
                productCategoryMap.putIfAbsent(productId, categoryIdSet);
            }

            if (categoryProductMap.containsKey(categoryId)) {
                categoryProductMap.get(categoryId).add(productId);
            } else {
                Set<Long> productIdSet = new HashSet<>();
                productIdSet.add(productId);
                categoryProductMap.putIfAbsent(categoryId, productIdSet);
            }
        });
    }

    public Set<Long> getProductCategory(Long productId) {
        return supplyWithReadLock(() -> productCategoryMap.getOrDefault(productId, emptySet));
    }

    public Set<Long> getCategoryProduct(Long categoryId) {
        return supplyWithReadLock(() -> categoryProductMap.getOrDefault(categoryId, emptySet));
    }
}
