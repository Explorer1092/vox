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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.alps.spi.common.DataLoader;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import com.voxlearning.utopia.service.zone.data.VersionedClazzZoneProductData;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneProductPersistence")
public class ClazzZoneProductPersistence extends NoCacheStaticMySQLPersistence<ClazzZoneProduct, Long>
        implements DataLoader<VersionedClazzZoneProductData> {

    @Inject private ClazzZoneProductVersion clazzZoneProductVersion;

    @Override
    public void insert(ClazzZoneProduct document) {
        $insert(document);
        clazzZoneProductVersion.increment();
    }

    @Override
    public VersionedClazzZoneProductData load() {
        VersionedClazzZoneProductData data = new VersionedClazzZoneProductData();
        data.setVersion(clazzZoneProductVersion.current());
        data.setClazzZoneProductList(query());
        return data;
    }
}
