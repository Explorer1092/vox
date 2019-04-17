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
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Reward sku entity data structure.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @serial
 * @since Jul 14, 2014
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_SKU")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160905")
public class RewardSku implements CacheDimensionDocument {
    private static final long serialVersionUID = 2350940679328015749L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentUpdateTimestamp
    @DocumentField("UPDATE_DATETIME") private Date updateDatetime;
    @DocumentField private Long productId;
    @DocumentField private String skuName;
    @DocumentField private Integer inventorySellable;
    @DocumentField private Integer displayOrder;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id),
                newCacheKey("productId", productId),
                newCacheKey("ALL")
        };
    }

    public static String ck_has_invetory() {
        return CacheKeyGenerator.generateCacheKey(RewardSku.class, "has_inventory_products");
    }

    public static void main(String[] args) {
        System.out.println(ck_has_invetory());
    }

}
