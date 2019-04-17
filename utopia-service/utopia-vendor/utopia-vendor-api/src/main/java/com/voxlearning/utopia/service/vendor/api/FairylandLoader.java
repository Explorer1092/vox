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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.zhang.a
 * @since 16-8-26
 */
@ServiceVersion(version = "20160825")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@Deprecated
public interface FairylandLoader {

    @Deprecated
    List<FairylandProduct> loadFairylandProducts(FairyLandPlatform platformType, FairylandProductType productType);

    @Deprecated
    VersionedFairylandProducts loadVersionedFairylandProducts(long version);

    @Deprecated
    List<FairylandProduct> $loadFairylandProducts();

    @Deprecated
    FairylandProduct $loadFairylandProduct(Long id);
}
