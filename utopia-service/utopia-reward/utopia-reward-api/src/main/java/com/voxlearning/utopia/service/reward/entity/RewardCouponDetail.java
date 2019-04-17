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

package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC;

/**
 * Reward coupon detail data structure.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Jul 30, 2014
 */
@DocumentTable(table = "VOX_REWARD_COUPON_DETAIL")
@DocumentConnection(configName = "hs_reward")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160728")
public class RewardCouponDetail implements Serializable, CacheDimensionDocument {
    private static final long serialVersionUID = -2986744538360640305L;

    @Getter @Setter @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = AUTO_INC) private Long id;
    @DocumentCreateTimestamp
    @Getter @Setter @UtopiaSqlColumn(name = "CREATE_DATETIME") private Date createDatetime;
    @Getter @Setter @UtopiaSqlColumn private String couponNo;
    @Getter @Setter @UtopiaSqlColumn private Long productId;
    @Getter @Setter @UtopiaSqlColumn private Boolean used;
    @Getter @Setter @UtopiaSqlColumn private Boolean exchanged;
    @Getter @Setter @UtopiaSqlColumn private Date exchangedDate;
    @Getter @Setter @UtopiaSqlColumn private Date usedDate;
    @Getter @Setter @UtopiaSqlColumn private Long userId;
    @Getter @Setter @UtopiaSqlColumn(name = "MOBILE") private String sensitiveMobile;
    @Getter @Setter @UtopiaSqlColumn private Boolean rebated;
    @Getter @Setter @UtopiaSqlColumn private Date rebatedDate;
    @Getter @Setter @UtopiaSqlColumn private Long orderId;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("productId", productId),
                newCacheKey("userId", userId)
        };
    }
}
