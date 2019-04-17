/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.api.FairylandLoader;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;
import com.voxlearning.utopia.service.vendor.client.FairylandProductServiceClient;
import lombok.Getter;

import javax.inject.Inject;
import java.util.List;

/**
 * @author peng.zhang.a
 * @since 16-8-26
 */
@Deprecated
public class FairylandLoaderClient implements FairylandLoader {

    @Getter
    @ImportService(interfaceClass = FairylandLoader.class)
    private FairylandLoader fairylandLoader;

    @Inject private FairylandProductServiceClient fairylandProductServiceClient;

    @Override
    @Deprecated
    public List<FairylandProduct> loadFairylandProducts(FairyLandPlatform platformType, FairylandProductType productType) {
        return fairylandProductServiceClient.getFairylandProductBuffer()
                .loadFairylandProducts(platformType, productType);
    }

    @Override
    @Deprecated
    public VersionedFairylandProducts loadVersionedFairylandProducts(long version) {
        return fairylandLoader.loadVersionedFairylandProducts(version);
    }

    @Override
    @Deprecated
    public List<FairylandProduct> $loadFairylandProducts() {
        return fairylandLoader.$loadFairylandProducts();
    }

    @Override
    @Deprecated
    public FairylandProduct $loadFairylandProduct(Long id) {
        return fairylandLoader.$loadFairylandProduct(id);
    }

    @Deprecated
    public FairylandProduct loadFairylandProduct(FairyLandPlatform platform, FairylandProductType productType, String orderProductServiceType) {
        return fairylandProductServiceClient.getFairylandProductBuffer()
                .loadFairylandProduct(platform, productType, orderProductServiceType);
    }
}
