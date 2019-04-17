package com.voxlearning.utopia.service.reward.client.newversion;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.api.newversion.NewRewardLoader;
import com.voxlearning.utopia.service.reward.buffer.*;
import com.voxlearning.utopia.service.reward.entity.RewardProductTarget;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NewRewardBufferLoaderClient implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(NewRewardBufferLoaderClient.class);

    @ImportService(interfaceClass = NewRewardLoader.class)
    private NewRewardLoader remoteReference;

    private ManagedNearBuffer<List<ProductCategoryRef>, ProductCategoryRefBuffer> productCategoryRefBuffer;
    private ManagedNearBuffer<List<ProductTagRef>, ProductTagRefBuffer> productTagRefBuffer;
    private ManagedNearBuffer<List<ProductSetRef>, ProductSetRefBuffer> productSetRefBuffer;
    private ManagedNearBuffer<List<ProductCategory>, ProductCategoryBuffer> productCategoryBuffer;
    private ManagedNearBuffer<List<ProductTag>, ProductTagBuffer> productTagBuffer;

    private ManagedNearBuffer<List<RewardProductTarget>, ProductTargetBuffer> productTargetBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        builderProductCategoryRefBuffer();
        builderProductTagRefBuffer();
        builderProductSetRefBuffer();
        builderProductTagBuffer();
        builderProductCategoryBuffer();
        builderRewardProductTargetBuffer();
    }

    private void builderProductTagBuffer() {
        NearBufferBuilder<List<ProductTag>, ProductTagBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ProductTagBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadProductTagBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadProductTagBufferData(version));
        productTagBuffer = builder.build();
    }

    private void builderProductCategoryRefBuffer() {
        NearBufferBuilder<List<ProductCategoryRef>, ProductCategoryRefBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ProductCategoryRefBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadProductCategoryRefBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadProductCategoryRefBufferData(version));
        productCategoryRefBuffer = builder.build();
    }

    private void builderProductTagRefBuffer() {
        NearBufferBuilder<List<ProductTagRef>, ProductTagRefBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ProductTagRefBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadProductTagRefBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadProductTagRefBufferData(version));
        productTagRefBuffer = builder.build();
    }

    private void builderProductSetRefBuffer() {
        NearBufferBuilder<List<ProductSetRef>, ProductSetRefBuffer> builder = NearBufferBuilder.newBuilder();

        builder.nearBufferClass(ProductSetRefBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadProductSetRefBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadProductSetRefBufferData(version));
        productSetRefBuffer = builder.build();
    }

    private void builderProductCategoryBuffer() {
        NearBufferBuilder<List<ProductCategory>, ProductCategoryBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ProductCategoryBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadProductCategoryBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadProductCategoryBufferData(version));
        productCategoryBuffer = builder.build();
    }

    private void builderRewardProductTargetBuffer() {
        NearBufferBuilder<List<RewardProductTarget>, ProductTargetBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(ProductTargetBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.lt(Mode.STAGING) ? 2 : 5, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadProductTargetBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadProductTargetBufferData(version));
        productTargetBuffer = builder.build();
    }

    public ProductCategoryRefBuffer getProductCategoryRefBuffer() {
        return productCategoryRefBuffer.getNativeBuffer();
    }

    public ProductCategoryBuffer getProductCategoryBuffer() {
        return productCategoryBuffer.getNativeBuffer();
    }

    public ProductTagBuffer getProductTagBuffer() {
        return productTagBuffer.getNativeBuffer();
    }

    public ProductTagRefBuffer getProductTagRefBuffer() {
        return productTagRefBuffer.getNativeBuffer();
    }

    public ProductSetRefBuffer getProductSetRefBuffer() {
        return productSetRefBuffer.getNativeBuffer();
    }

    public ProductTargetBuffer getProductTargetBuffer() {
        return productTargetBuffer.getNativeBuffer();
    }

    public Set<Long> loadProductIdByCategoryId(Long categoryId) {
        return productCategoryRefBuffer.getNativeBuffer().getCategoryProduct(categoryId);
    }

    public Set<Long> loadProductIdBySetId(Long setId) {
        return productSetRefBuffer.getNativeBuffer().getSetProduct(setId);
    }

    public Set<Long> loadProductIdByTagId(Long tagId) {
        return productTagRefBuffer.getNativeBuffer().getTagProduct(tagId);
    }

    public Set<Long> loadTagIdByProductId(Long productId) {
        return productTagRefBuffer.getNativeBuffer().getProductTag(productId);
    }

    /**
     * 根据传入的一级分类查找所属二级分类下所有商品ID
     */
    public Set<Long> loadProductIdByCategoryParentId(Long parentId) {
        List<ProductCategory> twoCategorys = getProductCategoryBuffer().loadByParentId(parentId);
        if (CollectionUtils.isEmpty(twoCategorys)) {
            return Collections.emptySet();
        }
        Set<Long> result = new HashSet<>();
        for (ProductCategory twoCategory : twoCategorys) {
            Set<Long> twoCategoryProductId = getProductCategoryRefBuffer().getCategoryProduct(twoCategory.getId());
            if (CollectionUtils.isNotEmpty(twoCategoryProductId)) {
                result.addAll(twoCategoryProductId);
            }
        }
        return result;
    }

}
