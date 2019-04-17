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

package com.voxlearning.utopia.service.vendor.consumer.extension;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResg;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import lombok.Setter;

/**
 * Extension vendor loader client implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 11, 2015
 */
public class ExtensionVendorLoaderClient {

    @Setter private VendorLoaderClient vendorLoaderClient;

    public ExtensionVendorLoaderClient() {
    }

    public VendorApps loadVendorApp(String appKey) {
        if (appKey == null) {
            return null;
        }
        VendorApps result = null;
        for (VendorApps each : vendorLoaderClient.loadVendorAppsIncludeDisabled().values()) {
            if (StringUtils.equals(appKey, each.getAppKey())) {
                result = each;
                break;
            }
        }
        if (result == null || result.isDisabledTrue()) {
            return null;
        }
        if (!result.isVisible(RuntimeModeLoader.getInstance().current().getLevel())) {
            return null;
        }
        return result;
    }

    public VendorResg loadVendorResg(Long id) {
        if (id == null) {
            return null;
        }
        VendorResg resg = vendorLoaderClient.loadVendorResgsIncludeDisabled().get(id);
        return (resg == null || resg.isDisabledTrue()) ? null : resg;
    }

}
