package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.reward.constant.OneLevelCategoryType;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductCategoryBuffer extends NearBuffer<List<ProductCategory>> {

    private Map<Long, ProductCategory> map = new HashMap<>();
    private Map<Integer, ProductCategory> oneLevelCategoryTypeMap = new HashMap<>();
    private Map<Integer, ProductCategory> twoLevelCategoryTypeMap = new HashMap<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<ProductCategory> data) {
        map.clear();
        oneLevelCategoryTypeMap.clear();
        twoLevelCategoryTypeMap.clear();
        data.forEach(item -> map.put(item.getId(), item));
        data.forEach(item -> oneLevelCategoryTypeMap.put(item.getOneLevelCategoryType(), item));
        data.forEach(item -> twoLevelCategoryTypeMap.put(item.getTwoLevelCategoryType(), item));
    }

    public ProductCategory load(Long id) {
        return supplyWithReadLock(() -> map.get(id));
    }


    public Boolean isSHIWU(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_SHIWU);
    }

    public Boolean isFlowPacket(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_FLOW_PACKET);
    }

    public Boolean isTobyWear(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_TOBY);
    }

    public Boolean isHeadWear(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_HEADWEAR);
    }

    public Boolean isMiniCourse(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_MINI_COURSE);
    }

    public Boolean isCoupon(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_COUPON);
    }

    public Boolean isTeachingResources(Long oneLevelCategoryId) {
        return checkCategoryType(oneLevelCategoryId, OneLevelCategoryType.JPZX_TEACHING_RESOURCES);
    }

    private Boolean checkCategoryType(Long id, OneLevelCategoryType categoryType) {
        ProductCategory load = load(id);
        if (load == null) {
            return false;
        }
        return Objects.equals(load.getOneLevelCategoryType(), categoryType.intType());
    }

    public Integer getOneLevelCategoryType(Long oneLevelCategoryId) {
        ProductCategory productCategory = map.get(oneLevelCategoryId);
        if (productCategory == null) {
            return null;
        }
        return productCategory.getOneLevelCategoryType();
    }

    public Long getOneLevelCategoryId(Integer oneLevelCategoryType) {
        ProductCategory productCategory = oneLevelCategoryTypeMap.get(oneLevelCategoryType);
        if (productCategory == null) {
            return null;
        }
        return productCategory.getId();
    }

    public Long getTwoLevelCategoryId(Integer twoLevelCategoryType) {
        ProductCategory productCategory = twoLevelCategoryTypeMap.get(twoLevelCategoryType);
        if (productCategory == null) {
            return null;
        }
        return productCategory.getId();
    }

    public List<ProductCategory> loadByParentId(Long parentId) {
        List<ProductCategory> data = super.dump().getData();
        return data.stream().filter(i -> Objects.equals(i.getParentId(), parentId)).collect(Collectors.toList());
    }

    public List<ProductCategory> loadProductCategoryByLevel(Integer level) {
        List<ProductCategory> data = super.dump().getData();
        return data.stream().filter(i -> Objects.equals(level, i.getLevel())).collect(Collectors.toList());
    }
}
