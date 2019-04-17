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

package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;
import com.voxlearning.utopia.service.vendor.impl.version.VendorAppsResgRefVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The 3rd Vendor Order Information Persistence Class
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since 2014-06-9
 */
@Named("com.voxlearning.utopia.service.vendor.impl.persistence.VendorAppsResgRefPersistence")
public class VendorAppsResgRefPersistence extends StaticCacheDimensionDocumentJdbcDao<VendorAppsResgRef, Long> {

    @Inject private VendorAppsResgRefVersion vendorAppsResgRefVersion;

    @Override
    public void insert(VendorAppsResgRef document) {
        $insert(document);
        vendorAppsResgRefVersion.increment();
    }

    @Override
    public VendorAppsResgRef upsert(VendorAppsResgRef document) {
        VendorAppsResgRef modified = $upsert(document);
        if (modified != null) {
            vendorAppsResgRefVersion.increment();
        }
        return modified;
    }

    @Override
    public VendorAppsResgRef replace(VendorAppsResgRef document) {
        VendorAppsResgRef modified = $replace(document);
        if (modified != null) {
            vendorAppsResgRefVersion.increment();
        }
        return modified;
    }

    @Deprecated
    public Map<Long, VendorAppsResgRef> loadAll() {
        Map<Long, VendorAppsResgRef> map = new LinkedHashMap<>();
        for (VendorAppsResgRef ref : query()) {
            map.put(ref.getId(), ref);
        }
        return map;
    }

    public int deleteByAppId(final Long appId) {
        Criteria criteria = Criteria.where("APP_ID").is(appId);
        int rows = (int) $remove(criteria);
        if (rows > 0) {
            vendorAppsResgRefVersion.increment();
        }
        return rows;
    }

    public int deleteByResgId(final Long resgId) {
        Criteria criteria = Criteria.where("RESG_ID").is(resgId);
        int rows = (int) $remove(criteria);
        if (rows > 0) {
            vendorAppsResgRefVersion.increment();
        }
        return rows;
    }

}
