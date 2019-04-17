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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.business.api.BusinessVendorService;
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

/**
 * Created by Summer Yang on 2016/4/12.
 */
public class BusinessVendorServiceClient implements BusinessVendorService {

    @ImportService(interfaceClass = BusinessVendorService.class)
    private BusinessVendorService remoteReference;

    public BusinessVendorService getRemoteReference() {
        return remoteReference;
    }

    @Override
    public List<VendorApps> getStudentPcAvailableApps(Long studentId) {
        return remoteReference.getStudentPcAvailableApps(studentId);
    }

    @Override
    public List<VendorApps> getStudentMobileAvailableApps(StudentDetail studentDetail, String version, AppSystemType systemType) {
        return remoteReference.getStudentMobileAvailableApps(studentDetail, version, systemType);
    }

    @Override
    public List<Map<String, Object>> getShoppingInfo(Long parentId, OrderProduct product, AppSystemType appSystemType, String version) {
        return remoteReference.getShoppingInfo(parentId, product, appSystemType, version);
    }

    @Override
    public List<VendorApps> getParentAvailableApps(User parent, StudentDetail children) {
        return remoteReference.getParentAvailableApps(parent, children);
    }


    @Override
    public List<Map<String, Object>> getShoppingInfo(Long studentId, Long parentId, String orderProductServiceType, AppSystemType appSystemType, String version) {
        return remoteReference.getShoppingInfo(studentId, parentId, orderProductServiceType, appSystemType, version);
    }

    @Override
    public List<FairylandProduct> getParentAvailableFairylandProducts(User parent, StudentDetail studentDetail, FairyLandPlatform fairyLandPlatform, FairylandProductType fairylandProductType) {
        return remoteReference.getParentAvailableFairylandProducts(parent, studentDetail, fairyLandPlatform, fairylandProductType);
    }

    public Map<String, String> fetchUserUseNumDesc(List<String> serviceTypes, StudentDetail studentDetail) {
        return remoteReference.fetchUserUseNumDesc(serviceTypes, studentDetail);
    }

    @Override
    public Map<String, Integer> loadUseNum(AppUseNumCalculateType appUseNumCalculateType, List<String> serviceTypes, StudentDetail studentDetail) {
        return remoteReference.loadUseNum(appUseNumCalculateType, serviceTypes, studentDetail);
    }

    @Override
    public Map<String, Integer> loadNationNum(List<String> serviceTypes) {
        return remoteReference.loadNationNum(serviceTypes);
    }

}
