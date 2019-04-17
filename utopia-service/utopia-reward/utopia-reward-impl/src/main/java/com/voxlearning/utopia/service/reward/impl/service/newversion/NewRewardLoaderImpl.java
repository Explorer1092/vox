package com.voxlearning.utopia.service.reward.impl.service.newversion;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.api.newversion.NewRewardLoader;
import com.voxlearning.utopia.service.reward.buffer.*;
import com.voxlearning.utopia.service.reward.entity.RewardProductTarget;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.impl.dao.RewardCouponDetailPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductTargetDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductTargetVersion;
import com.voxlearning.utopia.service.reward.impl.dao.RewardSkuDao;
import com.voxlearning.utopia.service.reward.impl.dao.newversion.*;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = NewRewardLoader.class)
public class NewRewardLoaderImpl extends SpringContainerSupport implements NewRewardLoader, InitializingBean {

    @Inject private ProductCategoryDao productCategoryDao;
    @Inject private ProductCategoryRefDao productCategoryRefDao;
    @Inject private ProductCategoryRefVersion productCategoryRefVersion;
    @Inject private ProductTagDao productTagDao;
    @Inject private ProductTagVersion productTagVersion;
    @Inject private ProductTagRefDao productTagRefDao;
    @Inject private ProductTagRefVersion productTagRefVersion;
    @Inject private ProductSetDao productSetDao;
    @Inject private ProductSetRefDao productSetRefDao;
    @Inject private ProductSetRefVersion productSetRefVersion;
    @Inject private ProductCategoryVersion productCategoryVersion;
    @Inject private RewardProductTargetVersion productTargetVersion;

    @Inject private RewardSkuDao rewardSkuDao;
    @Inject private RewardCouponDetailPersistence rewardCouponDetailPersistence;
    @Inject private RewardProductTargetDao rewardProductTargetDao;

    private ManagedNearBuffer<List<ProductCategoryRef>, ProductCategoryRefBuffer> productCategoryRefBuffer;
    private ManagedNearBuffer<List<ProductTagRef>, ProductTagRefBuffer> productTagRefBuffer;
    private ManagedNearBuffer<List<ProductSetRef>, ProductSetRefBuffer> productSetRefBuffer;
    private ManagedNearBuffer<List<ProductCategory>, ProductCategoryBuffer> productCategoryBuffer;
    private ManagedNearBuffer<List<ProductTag>, ProductTagBuffer> productTagBuffer;

    private ManagedNearBuffer<List<RewardProductTarget>, ProductTargetBuffer> productTargetBuffer;

    public ProductCategoryRefBuffer getProductCategoryRefBuffer() {
        return productCategoryRefBuffer.getNativeBuffer();
    }

    public ProductCategoryBuffer getProductCategoryBuffer() {
        return productCategoryBuffer.getNativeBuffer();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        builderProductCategoryRefBuffer();
        builderProductTagRefBuffer();
        builderProductSetRefBuffer();
        builderProductCategoryBuffer();
        builderProductTagBuffer();
        builderRewardProductTargetBuffer();
    }

    private void builderProductTagBuffer() {
        NearBufferBuilder<List<ProductTag>, ProductTagBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ProductTagBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ProductTagBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 5 : 10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = productTagVersion.current();
            List<ProductTag> list = productTagDao.loadAll();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = productTagVersion.current();
            if (oldVersion < currentVersion) {
                List<ProductTag> list = productTagDao.loadAll();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        productTagBuffer = builder.build();
    }

    private void builderRewardProductTargetBuffer() {
        NearBufferBuilder<List<RewardProductTarget>, ProductTargetBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ProductTargetBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ProductTargetBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = productTargetVersion.current();
            List<RewardProductTarget> list = rewardProductTargetDao.loadAll();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = productTargetVersion.current();
            if (oldVersion < currentVersion) {
                List<RewardProductTarget> list = rewardProductTargetDao.loadAll();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        productTargetBuffer = builder.build();
    }

    private void builderProductCategoryRefBuffer() {
        NearBufferBuilder<List<ProductCategoryRef>, ProductCategoryRefBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ProductCategoryRefBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ProductCategoryRefBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 5 : 10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = productCategoryRefVersion.current();
            List<ProductCategoryRef> list = productCategoryRefDao.loadAll();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = productCategoryRefVersion.current();
            if (oldVersion < currentVersion) {
                List<ProductCategoryRef> list = productCategoryRefDao.loadAll();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        productCategoryRefBuffer = builder.build();
    }

    private void builderProductTagRefBuffer() {
        NearBufferBuilder<List<ProductTagRef>, ProductTagRefBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ProductTagRefBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ProductTagRefBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 5 : 10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = productTagRefVersion.current();
            List<ProductTagRef> list = productTagRefDao.loadAll();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = productTagRefVersion.current();
            if (oldVersion < currentVersion) {
                List<ProductTagRef> list = productTagRefDao.loadAll();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        productTagRefBuffer = builder.build();
    }

    private void builderProductSetRefBuffer() {
        NearBufferBuilder<List<ProductSetRef>, ProductSetRefBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ProductSetRefBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ProductSetRefBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 5 : 10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = productSetRefVersion.current();
            List<ProductSetRef> list = productSetRefDao.loadAll();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = productSetRefVersion.current();
            if (oldVersion < currentVersion) {
                List<ProductSetRef> list = productSetRefDao.loadAll();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        productSetRefBuffer = builder.build();
    }

    private void builderProductCategoryBuffer() {
        NearBufferBuilder<List<ProductCategory>, ProductCategoryBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("ProductCategoryBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(ProductCategoryBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 5 : 10, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = productCategoryVersion.current();
            List<ProductCategory> list = productCategoryDao.loadAll();
            return new VersionedBufferData<>(version, list);
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = productCategoryVersion.current();
            if (oldVersion < currentVersion) {
                List<ProductCategory> list = productCategoryDao.loadAll();
                return new VersionedBufferData<>(currentVersion, list);
            }
            return null;
        });
        productCategoryBuffer = builder.build();
    }

    public Set<Long> getProductTagIdByProductId(Long productId) {
        return productTagRefBuffer.getNativeBuffer().getProductTag(productId);
    }

    @Override
    public List<ProductCategory> loadAllProductCategory() {
        return productCategoryDao.loadAll();
    }

    @Override
    public ProductCategory loadProductCategoryById(Long id) {
        return productCategoryDao.load(id);
    }

    @Override
    public List<ProductCategoryRef> loadAllProductCategoryRef() {
        return productCategoryRefDao.loadAll();
    }

    @Override
    public ProductCategoryRef loadProductCategoryRefByProductId(Long productId) {
        return productCategoryRefDao.loadByProductId(productId);
    }

    @Override
    public List<ProductCategory> loadProductCategoryByParentId(Long parentId) {
        List<ProductCategory> allCategory = productCategoryDao.loadAll();
        return allCategory.stream()
                .filter(p -> Objects.equals(p.getParentId(), parentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> loadProductIdListByCategoryId(Long categoryId) {
        List<ProductCategoryRef> productCategoryRefs = productCategoryRefDao.loadByCategoryId(categoryId);
        if (CollectionUtils.isEmpty(productCategoryRefs)) {
            return Collections.emptyList();
        }
        return productCategoryRefs.stream().map(ProductCategoryRef::getProductId).collect(Collectors.toList());
    }

    @Override
    public List<Long> loadProductIdListBySetId(Long setId) {
        List<ProductSetRef> result = productSetRefDao.loadBySetId(setId);
        if (CollectionUtils.isEmpty(result)) {
            return Collections.emptyList();
        }
        return result.stream().map(ProductSetRef::getProductId).collect(Collectors.toList());
    }

    @Override
    public List<Long> loadProductIdListByCategoryIds(Collection<Long> categoryIds) {
        List<Long> result = Collections.emptyList();
        Map<Long, List<ProductCategoryRef>> map = productCategoryRefDao.loadByCategoryIds(categoryIds);
        if (MapUtils.isNotEmpty(map)) {
            for (Map.Entry<Long, List<ProductCategoryRef>> entry : map.entrySet()) {
                List<Long> subIdList = entry.getValue().stream().map(ProductCategoryRef::getProductId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(subIdList)) {
                    result.addAll(subIdList);
                }
            }
        }
        return result;
    }

    @Override
    public VersionedBufferData<List<ProductCategoryRef>> loadProductCategoryRefBufferData(Long version) {
        ProductCategoryRefBuffer nativeBuffer = productCategoryRefBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetProductCategoryRefBuffer() {
        productCategoryRefBuffer.reset();
    }

    @Override
    public VersionedBufferData<List<ProductTag>> loadProductTagBufferData(Long version) {
        ProductTagBuffer nativeBuffer = productTagBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetProductTagBuffer() {
        productTagBuffer.reset();
    }

    @Override
    public VersionedBufferData<List<ProductTagRef>> loadProductTagRefBufferData(Long version) {
        ProductTagRefBuffer nativeBuffer = productTagRefBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetProductTagRefBuffer() {
        productTagRefBuffer.reset();
    }

    @Override
    public VersionedBufferData<List<ProductSetRef>> loadProductSetRefBufferData(Long version) {
        ProductSetRefBuffer nativeBuffer = productSetRefBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetProductSetRefBuffer() {
        productSetRefBuffer.reset();
    }

    @Override
    public VersionedBufferData<List<ProductCategory>> loadProductCategoryBufferData(Long version) {
        ProductCategoryBuffer nativeBuffer = productCategoryBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetProductCategoryBuffer() {
        productCategoryBuffer.reset();
    }

    @Override
    public VersionedBufferData<List<RewardProductTarget>> loadProductTargetBufferData(Long version) {
        ProductTargetBuffer nativeBuffer = productTargetBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetProductTargetBuffer() {
        productTargetBuffer.reset();
    }

    public ProductTargetBuffer getProductTargetBuffer() {
        return productTargetBuffer.getNativeBuffer();
    }

    @Override
    public ProductTag loadProductTagById(Long id) {
        return productTagDao.load(id);
    }

    @Override
    public Map<Long, ProductTag> loadProductTagByIds(Collection tagIds) {
        return productTagDao.loads(tagIds);
    }

    @Override
    public List<ProductTag> loadProductTagByParent(Long parentId, Integer parentType) {
        return productTagBuffer.getNativeBuffer().loadProductTagByParent(parentId, parentType);
    }

    @Override
    public List<ProductTagRef> loadProductTagRefByTagId(Long tagId) {
        return productTagRefDao.loadByTagId(tagId);
    }

    @Override
    public Map<Long, List<ProductTagRef>> loadProductTagRefByTagIdList(Collection<Long> tagIdList) {
        return productTagRefDao.loadByTagIdList(tagIdList);
    }

    @Override
    public List<ProductTagRef> loadProductTagRefByProductId(Long productId) {
        return productTagRefDao.loadByProductId(productId);
    }

    @Override
    public List<ProductSet> loadAllProductSet() {
        return productSetDao.loadAll();
    }

    @Override
    public ProductSet loadProductSetById(Long id) {
        return productSetDao.loadAll().stream().filter(p -> Objects.equals(p.getId(), id)).findFirst().orElse(null);
    }

    @Override
    public Map<Long, ProductSet> loadProductSetByProductId(Long productId) {
        List<ProductSetRef> productSetRefList = productSetRefDao.loadByProductId(productId);
        if (CollectionUtils.isEmpty(productSetRefList)) {
            return Collections.emptyMap();
        }

        Set<Long> setIds = productSetRefList.stream().map(ProductSetRef::getSetId).collect(Collectors.toSet());
        List<ProductSet> sets = productSetDao.loadAll().stream().filter(p -> setIds.contains(p.getId())).collect(Collectors.toList());
        return sets.stream().collect(Collectors.toMap(ProductSet::getId, Function.identity()));
    }

    @Override
    public List<ProductSetRef> loadAllProductSetRef() {
        return productSetRefDao.loadAll();
    }

    @Override
    public List<RewardSku> loadAllRewardSku() {
        return rewardSkuDao.loadAll();
    }

    @Override
    public Map<Long, Long> loadCouponStock() {
        return rewardCouponDetailPersistence.loadCouponStock();
    }

    @Override
    public Integer getOneLevelCategoryType(Long oneLevelCategoryId) {
        ProductCategory category = productCategoryDao.load(oneLevelCategoryId);
        if (Objects.nonNull(category)) {
            return category.getOneLevelCategoryType();
        }
        return 0;
    }

    @Override
    public Boolean isSHIWU(Long oneLevelCategoryId) {
        return  productCategoryBuffer.getNativeBuffer().isSHIWU(oneLevelCategoryId);
    }

    @Override
    public Boolean isFlowPacket(Long oneLevelCategoryId) {
        return productCategoryBuffer.getNativeBuffer().isFlowPacket(oneLevelCategoryId);
    }

    @Override
    public Boolean isTeachingResources(Long oneLevelCategoryId) {
        return productCategoryBuffer.getNativeBuffer().isTeachingResources(oneLevelCategoryId);
    }

    @Override
    public Boolean isTobyWear(Long oneLevelCategoryId) {
        return productCategoryBuffer.getNativeBuffer().isTobyWear(oneLevelCategoryId);
    }

    @Override
    public Boolean isHeadWear(Long oneLevelCategoryId) {
        return productCategoryBuffer.getNativeBuffer().isHeadWear(oneLevelCategoryId);
    }

    @Override
    public Boolean isMiniCourse(Long oneLevelCategoryId) {
        return productCategoryBuffer.getNativeBuffer().isMiniCourse(oneLevelCategoryId);
    }

    @Override
    public Boolean isCoupon(Long oneLevelCategoryId) {
        return productCategoryBuffer.getNativeBuffer().isCoupon(oneLevelCategoryId);
    }
}
