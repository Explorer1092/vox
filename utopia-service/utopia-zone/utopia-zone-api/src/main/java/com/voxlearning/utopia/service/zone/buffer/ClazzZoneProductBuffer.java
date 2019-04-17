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

package com.voxlearning.utopia.service.zone.buffer;

import com.voxlearning.utopia.service.zone.api.constant.ClazzZoneProductSpecies;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;

import java.util.List;

public interface ClazzZoneProductBuffer {

    void attach(VersionedClazzZoneProductData data);

    VersionedClazzZoneProductData dump();

    long getVersion();

    ClazzZoneProduct load(Long id);

    List<ClazzZoneProduct> findBySpecies(ClazzZoneProductSpecies species);

    default List<ClazzZoneProduct> loadBubbles() {
        return findBySpecies(ClazzZoneProductSpecies.BUBBLE);
    }

    interface Aware {

        ClazzZoneProductBuffer getClazzZoneProductBuffer();

        void resetClazzZoneProductBuffer();

    }
}
