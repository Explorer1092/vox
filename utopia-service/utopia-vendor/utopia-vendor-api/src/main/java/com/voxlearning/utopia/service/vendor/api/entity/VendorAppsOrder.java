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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.VendorAppsOrderPayType;
import com.voxlearning.utopia.core.ObjectIdEntity;
import lombok.*;
import org.bson.types.ObjectId;

/**
 * The 3rd Vendor App Order Information
 *
 * @author Zhilong Hu
 * @serial
 * @since 2014-06-9
 */
@Getter
@Setter
@DocumentTable(table = "VOX_VENDOR_APPS_ORDER")
@DocumentConnection(configName = "hs_vendor")
@UtopiaCacheExpiration(7200)
@UtopiaCacheRevision("20150611")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
public class VendorAppsOrder extends ObjectIdEntity {
    private static final long serialVersionUID = 4306459661360543712L;

    @UtopiaSqlColumn @NonNull private Long appId;               // appid
    @UtopiaSqlColumn @NonNull private String appKey;            // AppKey
    @UtopiaSqlColumn @NonNull private Long userId;              // 用户ID
    @UtopiaSqlColumn @NonNull private String sessionKey;        // 用户访问时的SessionKey
    @UtopiaSqlColumn @NonNull private Long orderSeq;            // 订单序号
    @UtopiaSqlColumn @NonNull private Long productId;           // 商品ID
    @UtopiaSqlColumn @NonNull private String productName;       // 商品名
    @UtopiaSqlColumn private VendorAppsOrderPayType payType;    // 支付类型  INTEGRAL:学豆支付 VOXHWCOIN:作业币
    @UtopiaSqlColumn private Double amount;                     // 支付金额
    @UtopiaSqlColumn private String status;                     // 订单状态
    @UtopiaSqlColumn @NonNull private String orderToken;        // 订单Token

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(VendorAppsOrder.class, id);
    }

    public static String ck_appKey_sessionKey_orderSeq(String appKey, String sessionKey, Long orderSeq) {
        return CacheKeyGenerator.generateCacheKey(VendorAppsOrder.class,
                new String[]{"appKey", "sessionKey", "orderSeq"},
                new Object[]{appKey, sessionKey, orderSeq});
    }

    public static String ck_appKey_userId(String appKey, Long userId) {
        return CacheKeyGenerator.generateCacheKey(VendorAppsOrder.class,
                new String[]{"appKey", "userId"},
                new Object[]{appKey, userId});
    }

    public static VendorAppsOrder newOrder() {
        VendorAppsOrder appsOrder = new VendorAppsOrder();
        appsOrder.setId(generateId());
        return appsOrder;
    }

    @JsonIgnore
    public boolean isPaid() {
        return status != null && status.equals("paid");
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public static VendorAppsOrder mockInstance() {
        VendorAppsOrder inst = new VendorAppsOrder();
        inst.appId = 0L;
        inst.appKey = "";
        inst.userId = 0L;
        inst.sessionKey = new ObjectId().toString();
        inst.orderSeq = 0L;
        inst.productId = 0L;
        inst.productName = "";
        inst.orderToken = "";
        return inst;
    }
}
