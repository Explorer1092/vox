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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ServiceVersion(version = "2017.07.03")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface FairylandProductService {

    @Async
    AlpsFuture<List<FairylandProduct>> loadAllFairylandProductsFromDB();

    @Async
    AlpsFuture<FairylandProduct> loadFairylandProductFromDB(Long id);

    @Async
    AlpsFuture<VersionedFairylandProducts> loadFairylandProductBufferData(long version);

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void reloadFairylandProductBuffer();

    // ========================================================================
    // static helper utilities
    // ========================================================================

    static List<FairylandProduct> filterFairylandProducts(List<FairylandProduct> list,
                                                          FairyLandPlatform platformType,
                                                          FairylandProductType productType) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(m -> platformType == null || platformType.name().equals(m.getPlatform()))
                .filter(m -> productType == null || productType.name().equals(m.getProductType()))
                .sorted((o1, o2) -> {
                    int r1 = SafeConverter.toInt(o1.getRank());
                    int r2 = SafeConverter.toInt(o2.getRank());
                    return Integer.compare(r2, r1);
                })
                .collect(Collectors.toList());
    }
}
