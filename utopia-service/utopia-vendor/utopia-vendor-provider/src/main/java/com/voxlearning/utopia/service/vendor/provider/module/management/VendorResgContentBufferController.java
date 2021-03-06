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
import com.voxlearning.utopia.service.vendor.buffer.VersionedVendorResgContentList;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorResgContentVersion;
import com.voxlearning.utopia.service.vendor.impl.service.VendorResgContentServiceImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/vendor-provider/vendorResgContentBuffer")
final public class VendorResgContentBufferController {

    public static final VendorResgContentBufferController INSTANCE = new VendorResgContentBufferController();

    @RequestMapping(value = "inspect.do", method = RequestMethod.GET)
    public String inspect(Model model) {
        VersionedVendorResgContentList vendorResgContentBufferData = ApplicationContextScanner.getInstance()
                .getBean(VendorResgContentServiceImpl.class)
                .getVendorResgContentBuffer()
                .dump();
        model.addAttribute("vendorResgContentBufferData", vendorResgContentBufferData);
        return "vendor-provider/buffer/vendorResgContentBuffer";
    }

    @RequestMapping(value = "increment.do", method = RequestMethod.POST)
    public String increment() {
        ApplicationContextScanner.getInstance().getBean(VendorResgContentVersion.class).increment();
        return "redirect:/vendor-provider/index.do";
    }

    @RequestMapping(value = "reload.do", method = RequestMethod.POST)
    public String reload() {
        ApplicationContextScanner.getInstance()
                .getBean(VendorResgContentServiceImpl.class)
                .reloadVendorResgContentBuffer();
        return "redirect:/vendor-provider/index.do";
    }
}
