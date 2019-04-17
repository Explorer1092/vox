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
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.derby.persistence.DerbyPersistence;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorApps;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.vendor.impl.persistence.MDBVendorAppsPersistence")
public class MDBVendorAppsPersistence extends DerbyPersistence<MDBVendorApps, Long> {

    public MDBVendorApps loadByAppKey(String appKey) {
        Criteria criteria = Criteria.where("APP_KEY").is(appKey);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }
}
