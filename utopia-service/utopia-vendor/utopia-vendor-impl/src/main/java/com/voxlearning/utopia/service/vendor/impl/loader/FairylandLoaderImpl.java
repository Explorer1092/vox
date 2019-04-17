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

package com.voxlearning.utopia.service.vendor.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.vendor.api.FairylandLoader;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;
import com.voxlearning.utopia.service.vendor.impl.service.FairylandProductServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author peng.zhang.a
 * @since 16-8-26
 */
@Named("com.voxlearning.utopia.service.vendor.impl.loader.FairylandLoaderImpl")
@Service(interfaceClass = FairylandLoader.class)
@ExposeService(interfaceClass = FairylandLoader.class)
@Deprecated
public class FairylandLoaderImpl extends SpringContainerSupport implements FairylandLoader {

    @Inject private FairylandProductServiceImpl fairylandProductService;

    @Override
    @Deprecated
    public List<FairylandProduct> loadFairylandProducts(FairyLandPlatform platformType, FairylandProductType productType) {
        return fairylandProductService.getFairylandProductBuffer().loadFairylandProducts(platformType, productType);
    }

    @Override
    @Deprecated
    public VersionedFairylandProducts loadVersionedFairylandProducts(long version) {
        return fairylandProductService.loadFairylandProductBufferData(version).getUninterruptibly();
    }

    @Override
    @Deprecated
    public List<FairylandProduct> $loadFairylandProducts() {
        return fairylandProductService.loadAllFairylandProductsFromDB().getUninterruptibly();
    }

    @Override
    @Deprecated
    public FairylandProduct $loadFairylandProduct(Long id) {
        return fairylandProductService.loadFairylandProductFromDB(id).getUninterruptibly();
    }
}
