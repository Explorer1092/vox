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
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorAppsResgRefList;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsResgRefServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.version.VendorAppsResgRefVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/vendor-provider/vendorAppsResgRefBuffer")
final public class VendorAppsResgRefBufferController {

    public static final VendorAppsResgRefBufferController INSTANCE = new VendorAppsResgRefBufferController();

    @RequestMapping(value = "inspect.do", method = RequestMethod.GET)
    public String inspect(Model model) {
        VersionedVendorAppsResgRefList vendorAppsResgRefBufferData = ApplicationContextScanner.getInstance()
                .getBean(VendorAppsResgRefServiceImpl.class)
                .getVendorAppsResgRefBuffer()
                .dump();
        model.addAttribute("vendorAppsResgRefBufferData", vendorAppsResgRefBufferData);
        return "vendor-provider/buffer/vendorAppsResgRefBuffer";
    }

    @RequestMapping(value = "increment.do", method = RequestMethod.POST)
    public String increment() {
        ApplicationContextScanner.getInstance().getBean(VendorAppsResgRefVersion.class).increment();
        return "redirect:/vendor-provider/index.do";
    }

    @RequestMapping(value = "reload.do", method = RequestMethod.POST)
    public String reload() {
        ApplicationContextScanner.getInstance()
                .getBean(VendorAppsResgRefServiceImpl.class)
                .reloadVendorAppsResgRefBuffer();
        return "redirect:/vendor-provider/index.do";
    }
}
