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

package com.voxlearning.utopia.service.vendor.buffer;

import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.api.FairylandProductService;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;

import java.util.List;
import java.util.Objects;

public interface FairylandProductBuffer {

    void attach(VersionedFairylandProducts data);

    VersionedFairylandProducts dump();

    long getVersion();

    default List<FairylandProduct> loadFairylandProducts(FairyLandPlatform platformType,
                                                         FairylandProductType productType) {
        List<FairylandProduct> list = dump().getFairylandProducts();
        return FairylandProductService.filterFairylandProducts(list, platformType, productType);
    }

    default FairylandProduct loadFairylandProduct(FairyLandPlatform platform, FairylandProductType productType, String orderProductServiceType) {
        return loadFairylandProducts(platform, productType)
                .stream()
                .filter(p -> Objects.equals(p.getAppKey(), orderProductServiceType))
                .findFirst()
                .orElse(null);

    }

    interface Aware {

        FairylandProductBuffer getFairylandProductBuffer();

        void resetFairylandProductBuffer();
    }
}
