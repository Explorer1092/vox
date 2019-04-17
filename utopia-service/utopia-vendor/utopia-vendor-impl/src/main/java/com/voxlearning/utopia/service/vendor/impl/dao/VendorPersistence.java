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

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.vendor.api.entity.Vendor;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The 3rd Vendor Information Persistence Class
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since 2014-06-6
 */
@Named
@UtopiaCacheSupport(Vendor.class)
public class VendorPersistence extends StaticCacheDimensionDocumentJdbcDao<Vendor, Long> {

    @UtopiaCacheable(key = "A")
    public Map<Long, Vendor> loadAll() {
        Map<Long, Vendor> map = new LinkedHashMap<>();
        for (Vendor vendor : query()) {
            map.put(vendor.getId(), vendor);
        }
        return map;
    }

    public int disable(final Long id) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = Update.update("DISABLED", true);
        int rows = (int) $update(update, criteria);
        if (rows > 0) {
            getCache().delete(CacheKeyGenerator.generateCacheKey(Vendor.class, "A"));
        }
        return rows;
    }

}
