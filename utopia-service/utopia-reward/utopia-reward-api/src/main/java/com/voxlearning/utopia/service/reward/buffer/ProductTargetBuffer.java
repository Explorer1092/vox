package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.reward.entity.RewardProductTarget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductTargetBuffer extends NearBuffer<List<RewardProductTarget>> {

    private Map<Long, List<RewardProductTarget>> productIdMap = new HashMap<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<RewardProductTarget> productTags) {
        productIdMap.clear();
        productIdMap = productTags.stream().collect(
                Collectors.groupingBy(
                        RewardProductTarget::getProductId,
                        Collectors.toList()
                ));
    }

    /**
     * 方法名将错就错
     */
    public Map<Long, List<RewardProductTarget>> loadAllProductTargets() {
        return supplyWithReadLock(() -> productIdMap);
    }
}
