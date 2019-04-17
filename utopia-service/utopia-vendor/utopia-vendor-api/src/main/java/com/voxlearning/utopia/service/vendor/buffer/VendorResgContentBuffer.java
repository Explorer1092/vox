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

import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;

import java.util.LinkedHashMap;
import java.util.Map;

public interface VendorResgContentBuffer {

    void attach(VersionedVendorResgContentList data);

    VersionedVendorResgContentList dump();

    long getVersion();

    default Map<Long, VendorResgContent> loadAll() {
        Map<Long, VendorResgContent> map = new LinkedHashMap<>();
        for (VendorResgContent content : dump().getContentList()) {
            map.put(content.getId(), content);
        }
        return map;
    }

    interface Aware {

        VendorResgContentBuffer getVendorResgContentBuffer();

        void resetVendorResgContentBuffer();
    }
}
