package com.voxlearning.utopia.service.reward.api.newversion;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.reward.entity.RewardProductTarget;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.entity.newversion.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ServiceVersion(version = "20181206")
@ServiceTimeout(timeout = 1, unit = TimeUnit.MINUTES)
@ServiceRetries
public interface NewRewardLoader extends IPingable {

    Set<Long> getProductTagIdByProductId(Long productId);

    List<ProductCategory> loadAllProductCategory();

    default List<ProductCategory> loadProductCategoryByLevel(Integer level) {
        return loadAllProductCategory().stream()
                .filter(i -> Objects.equals(level, i.getLevel()))
                .collect(Collectors.toList());
    }

    ProductCategory loadProductCategoryById(Long id);

    List<ProductCategoryRef> loadAllProductCategoryRef();

    ProductCategoryRef loadProductCategoryRefByProductId(Long productId);

    List<ProductCategory> loadProductCategoryByParentId(Long parentId);

    List<Long> loadProductIdListByCategoryId(Long categoryId);

    List<Long> loadProductIdListBySetId(Long setId);

    List<Long> loadProductIdListByCategoryIds(Collection<Long> categoryIds);

    VersionedBufferData<List<ProductCategoryRef>> loadProductCategoryRefBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetProductCategoryRefBuffer();

    VersionedBufferData<List<ProductTag>> loadProductTagBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetProductTagBuffer();

    VersionedBufferData<List<ProductTagRef>> loadProductTagRefBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetProductTagRefBuffer();

    VersionedBufferData<List<ProductSetRef>> loadProductSetRefBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetProductSetRefBuffer();

    VersionedBufferData<List<ProductCategory>> loadProductCategoryBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetProductCategoryBuffer();

    VersionedBufferData<List<RewardProductTarget>> loadProductTargetBufferData(Long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void resetProductTargetBuffer();

    ProductTag loadProductTagById(Long id);

    Map<Long, ProductTag> loadProductTagByIds(Collection tagIds);

    List<ProductTag> loadProductTagByParent(Long parentId, Integer parentType);

    List<ProductTagRef> loadProductTagRefByTagId(Long tagId);

    Map<Long, List<ProductTagRef>> loadProductTagRefByTagIdList(Collection<Long> tagIdList);

    List<ProductTagRef> loadProductTagRefByProductId(Long productId);

    List<ProductSet> loadAllProductSet();

    ProductSet loadProductSetById(Long id);

    Map<Long, ProductSet> loadProductSetByProductId(Long productId);

    List<ProductSetRef> loadAllProductSetRef();

    List<RewardSku> loadAllRewardSku();

    Map<Long, Long> loadCouponStock();

    Integer getOneLevelCategoryType(Long oneLevelCategoryId);

    Boolean isSHIWU(Long oneLevelCategoryId);
    Boolean isFlowPacket(Long oneLevelCategoryId);
    Boolean isTeachingResources(Long oneLevelCategoryId);
    Boolean isTobyWear(Long oneLevelCategoryId);
    Boolean isHeadWear(Long oneLevelCategoryId);
    Boolean isMiniCourse(Long oneLevelCategoryId);
    Boolean isCoupon(Long oneLevelCategoryId);
}
