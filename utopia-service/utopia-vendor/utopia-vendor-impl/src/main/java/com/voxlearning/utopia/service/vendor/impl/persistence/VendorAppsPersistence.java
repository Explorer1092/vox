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

package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The 3rd Vendor Information Persistence Class.
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since 2014-06-6
 */
@Named("com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsPersistence")
public class VendorAppsPersistence extends NoCacheStaticMySQLPersistence<VendorApps, Long> {

    @Inject private VendorAppsVersion vendorAppsVersion;

    @Override
    public void insert(VendorApps document) {
        $insert(document);
        vendorAppsVersion.increment();
    }

    @Override
    public VendorApps upsert(VendorApps document) {
        VendorApps modified = $upsert(document);
        if (modified != null) {
            vendorAppsVersion.increment();
        }
        return modified;
    }

    @Override
    public VendorApps replace(VendorApps document) {
        VendorApps modified = $replace(document);
        if (modified != null) {
            vendorAppsVersion.increment();
        }
        return modified;
    }

    public int disable(final Long id) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("DISABLED", true);
        long rows = $update(update, criteria);
        if (rows > 0) {
            vendorAppsVersion.increment();
        }
        return (int) rows;
    }
}
