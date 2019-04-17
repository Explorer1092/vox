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

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import com.voxlearning.utopia.service.vendor.impl.persistence.VendorResgContentVersion;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * The 3rd Vendor Order Information Persistence Class
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since 2014-06-9
 */
@Named("com.voxlearning.utopia.service.vendor.impl.dao.VendorResgContentPersistence")
public class VendorResgContentPersistence extends NoCacheStaticMySQLPersistence<VendorResgContent, Long> {

    @Inject private VendorResgContentVersion vendorResgContentVersion;

    @Override
    public void insert(VendorResgContent document) {
        $insert(document);
        vendorResgContentVersion.increment();
    }

    @Override
    public VendorResgContent upsert(VendorResgContent document) {
        VendorResgContent modified = $upsert(document);
        if (modified != null) {
            vendorResgContentVersion.increment();
        }
        return modified;
    }

    @Override
    public VendorResgContent replace(VendorResgContent document) {
        VendorResgContent modified = $replace(document);
        if (modified != null) {
            vendorResgContentVersion.increment();
        }
        return modified;
    }

    @Override
    public boolean remove(Long id) {
        boolean ret = $remove(id);
        if (ret) {
            vendorResgContentVersion.increment();
        }
        return ret;
    }

    public int deleteByResgId(final Long resgId) {
        Criteria criteria = Criteria.where("RESG_ID").is(resgId);
        int rows = (int) $remove(criteria);
        if (rows > 0) {
            vendorResgContentVersion.increment();
        }
        return rows;
    }
}
