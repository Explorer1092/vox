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

package com.voxlearning.utopia.service.vendor.provider.module.management;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedFairylandProducts;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorAppsResgRefList;
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList;
import com.voxlearning.utopia.service.vendor.impl.service.FairylandProductServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsResgRefServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.VendorResgContentServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/vendor-provider")
final public class VendorController {

    public static final VendorController INSTANCE = new VendorController();

    @RequestMapping(value = "index.do", method = RequestMethod.GET)
    public String index(Model model) {
        VersionedFairylandProducts fairylandProductBufferData = ApplicationContextScanner.getInstance()
                .getBean(FairylandProductServiceImpl.class)
                .getFairylandProductBuffer()
                .dump();
        model.addAttribute("fairylandProductBufferData", fairylandProductBufferData);

        VersionedVendorAppsList vendorAppsBufferData = ApplicationContextScanner.getInstance()
                .getBean(VendorAppsServiceImpl.class)
                .getVendorAppsBuffer()
                .dump();
        model.addAttribute("vendorAppsBufferData", vendorAppsBufferData);

        VersionedVendorAppsResgRefList vendorAppsResgRefBufferData = ApplicationContextScanner.getInstance()
                .getBean(VendorAppsResgRefServiceImpl.class)
                .getVendorAppsResgRefBuffer()
                .dump();
        model.addAttribute("vendorAppsResgRefBufferData", vendorAppsResgRefBufferData);

        VersionedVendorResgContentList vendorResgContentBufferData = ApplicationContextScanner.getInstance()
                .getBean(VendorResgContentServiceImpl.class)
                .getVendorResgContentBuffer()
                .dump();
        model.addAttribute("vendorResgContentBufferData", vendorResgContentBufferData);

//        VersionedJxtNewsAlbumList jxtNewsAlbumBufferData = ApplicationContextScanner.getInstance()
//                .getBean(JxtNewsAlbumServiceImpl.class)
//                .getJxtNewsAlbumBuffer()
//                .dump();
//        model.addAttribute("jxtNewsAlbumBufferData", jxtNewsAlbumBufferData);

        return "vendor-provider/index";
    }
}
