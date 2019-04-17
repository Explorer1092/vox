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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.statistics.VendorAppRef;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by XiaoPeng.Yang on 15-3-10.
 */
@Named
@CacheBean(type = VendorAppRef.class)
public class VendorAppRefPersistence extends AlpsStaticJdbcDao<VendorAppRef, Long> {

    @Override
    protected void calculateCacheDimensions(VendorAppRef document, Collection<String> dimensions) {
        dimensions.add(VendorAppRef.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<VendorAppRef> loadAppsByVendor(@CacheParameter("vendorId") Long vendorId) {
        Criteria criteria = Criteria.where("USER_ID").is(vendorId);
        return query(Query.query(criteria));
    }
}
