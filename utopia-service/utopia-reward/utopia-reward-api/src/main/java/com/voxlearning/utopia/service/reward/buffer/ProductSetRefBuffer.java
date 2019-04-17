package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductSetRef;

import java.util.*;

public class ProductSetRefBuffer extends NearBuffer<List<ProductSetRef>> {

    private static final Set<Long> emptySet = new HashSet<>();

    private Map<Long, Set<Long>> productSetMap = new HashMap<>();
    private Map<Long, Set<Long>> setProductMap = new HashMap<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ProductSetRef> data) {
        productSetMap.clear();
        setProductMap.clear();

        data.forEach(item -> {
            productSetMap.getOrDefault(item.getProductId(), emptySet).add(item.getSetId());
            setProductMap.getOrDefault(item.getSetId(), emptySet).add(item.getProductId());

            Long productId = item.getProductId();
            Long setId = item.getSetId();
            if(productSetMap.containsKey(productId)) {
                productSetMap.get(productId).add(setId);
            } else {
                HashSet categoryIdSet = new HashSet();
                categoryIdSet.add(setId);
                productSetMap.putIfAbsent(productId, categoryIdSet);
            }

            if (setProductMap.containsKey(setId)) {
                setProductMap.get(setId).add(productId);
            } else {
                Set<Long> productIdSet = new HashSet<>();
                productIdSet.add(productId);
                setProductMap.putIfAbsent(setId, productIdSet);
            }
        });
    }

    public Set<Long> getProductSet(Long productId) {
        return supplyWithReadLock(() -> productSetMap.getOrDefault(productId, emptySet));
    }

    public Set<Long> getSetProduct(Long setId) {
        return supplyWithReadLock(() -> setProductMap.getOrDefault(setId, emptySet));
    }
}
