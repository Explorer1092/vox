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

package com.voxlearning.utopia.service.zone.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 这个数据结构用于描述学生所拥有的空间装备。
 *
 * @author Rui Bao
 * @serial
 * @since 14-5-12
 */
@Getter
@Setter
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_CLAZZ_ZONE_BAG")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160902")
public class ClazzZoneBag implements CacheDimensionDocument {
    private static final long serialVersionUID = -1839748271508642358L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID")
    private Long id;

    @DocumentField("USER_ID")
    private Long userId;

    @DocumentField("PRODUCT_ID")
    private Long productId;

    @DocumentField("EXPIRE_DATE")
    private Date expireDate;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ck_userId(userId)
        };
    }

    @JsonIgnore
    public boolean isAvailable() {
        return expireDate != null && expireDate.getTime() > System.currentTimeMillis();
    }

    public static String ck_userId(Long userId) {
        return CacheKeyGenerator.generateCacheKey(ClazzZoneBag.class, "USER_ID", userId);
    }
}
