/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.business.api.constant.AppUseNumCalculateType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Summer Yang
 * @author peng.zhang
 * @version 0.1
 * @since 2016/4/12
 */
@ServiceVersion(version = "20181106")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BusinessVendorService extends IPingable {

    List<VendorApps> getStudentPcAvailableApps(Long studentId);

    List<VendorApps> getStudentMobileAvailableApps(StudentDetail studentDetail, String version, AppSystemType systemType);

    List<Map<String, Object>> getShoppingInfo(Long parentId, OrderProduct product, AppSystemType appSystemType, String version);

    // 获取应用信息
    List<Map<String, Object>> getShoppingInfo(Long studentId, Long parentId, String orderProductServiceType, AppSystemType appSystemType, String version);

    List<VendorApps> getParentAvailableApps(User parent, StudentDetail children);

    List<FairylandProduct> getParentAvailableFairylandProducts(User parent, StudentDetail studentDetail, FairyLandPlatform fairyLandPlatform,
                                                               FairylandProductType fairylandProductType);

    Map<String, String> fetchUserUseNumDesc(List<String> serviceTypes, StudentDetail studentDetail);

    Map<String, Integer> loadUseNum(AppUseNumCalculateType appUseNumCalculateType, List<String> serviceTypes, StudentDetail studentDetail);

    Map<String, Integer> loadNationNum(List<String> serviceTypes);
}
