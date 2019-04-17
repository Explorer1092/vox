package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTagRef;

import java.util.*;

public class ProductTagRefBuffer extends NearBuffer<List<ProductTagRef>> {

    private static final Set<Long> emptySet = new HashSet<>();

    private Map<Long, Set<Long>> productTagMap = new HashMap<>();
    private Map<Long, Set<Long>> tagProductMap = new HashMap<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ProductTagRef> data) {
        productTagMap.clear();
        tagProductMap.clear();

        data.forEach(item -> {
            Long productId = item.getProductId();
            Long tagId = item.getTagId();
            if(productTagMap.containsKey(productId)) {
                productTagMap.get(productId).add(tagId);
            } else {
                HashSet categoryIdSet = new HashSet();
                categoryIdSet.add(tagId);
                productTagMap.putIfAbsent(productId, categoryIdSet);
            }

            if (tagProductMap.containsKey(tagId)) {
                tagProductMap.get(tagId).add(productId);
            } else {
                Set<Long> productIdSet = new HashSet<>();
                productIdSet.add(productId);
                tagProductMap.putIfAbsent(tagId, productIdSet);
            }
        });
    }

    public Set<Long> getProductTag(Long productId) {
        return supplyWithReadLock(() -> productTagMap.getOrDefault(productId, emptySet));
    }

    public Set<Long> getTagProduct(Long tagId) {
        return supplyWithReadLock(() -> tagProductMap.getOrDefault(tagId, emptySet));
    }
}
