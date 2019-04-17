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

package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Vendor notify entity data structure.
 *
 * @author Zhulong Hu
 * @author Xiaohai Zhang
 * @serial
 * @since Nov 10, 2014
 */
@Getter
@Setter
@DocumentTable(table = "VOX_VENDOR_NOTIFY")
@UtopiaCacheExpiration(7200)
@UtopiaCacheRevision("20150611")
@DocumentConnection(configName = "hs_vendor")
public class VendorNotify extends AbstractDatabaseEntity {
    private static final long serialVersionUID = 4316459361163545762L;

    @UtopiaSqlColumn private String appKey;                          // APP Key
    @UtopiaSqlColumn private String targetUrl;                       // 投递URL
    @UtopiaSqlColumn private String notify;                          // 消息内容
    @UtopiaSqlColumn private Integer retryCount;                     // 重试次数
    @UtopiaSqlColumn private Integer status;                         // 状态 0:未投递,1:已投递

    public static String generateCacheKey(Long id) {
        return CacheKeyGenerator.generateCacheKey(VendorNotify.class, id);
    }

    /**
     * Create a mock instance for supporting unit tests.
     */
    public static VendorNotify mockInstance() {
        VendorNotify inst = new VendorNotify();
        inst.appKey = "";
        inst.targetUrl = "";
        inst.notify = "";
        return inst;
    }
}
