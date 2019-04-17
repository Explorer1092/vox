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
import com.voxlearning.alps.dao.jdbc.dao.StaticCacheDimensionDocumentJdbcDao;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResg;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The 3rd Vendor Information Persistence Class
 * FIXME: 小数据量，统统加载到缓存
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since 2014-06-6
 */
@Named
@UtopiaCacheSupport(VendorResg.class)
public class VendorResgPersistence extends StaticCacheDimensionDocumentJdbcDao<VendorResg, Long> {

    @UtopiaCacheable(key = "ALL_RESGS")
    public Map<Long, VendorResg> loadAll() {
        Map<Long, VendorResg> map = new LinkedHashMap<>();
        for (VendorResg resg : query()) {
            map.put(resg.getId(), resg);
        }
        return map;
    }

    public int delete(final Long id) {
        return remove(id) ? 1 : 0;
    }

}
