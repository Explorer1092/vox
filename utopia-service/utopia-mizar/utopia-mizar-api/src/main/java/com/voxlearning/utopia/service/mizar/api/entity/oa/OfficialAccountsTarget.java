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

package com.voxlearning.utopia.service.mizar.api.entity.oa;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer Yang on 2016/7/4.
 * 公众号投放策略配置
 */
@DocumentTable(table = "VOX_OFFICIAL_ACCOUNTS_TARGET")
@NoArgsConstructor
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160726")
@DocumentConnection(configName = "hs_misc")
public class OfficialAccountsTarget extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 3625523471072573120L;
    @Getter @Setter @UtopiaSqlColumn private Long accountId;      // 公众号ID
    @Getter @Setter @UtopiaSqlColumn private Integer targetType;  // 公众号投放对象类型 1-地区编码 2-用户ID 3-Tag标签 4广告投放标签  AdvertisementTargetType
    @Getter @Setter @UtopiaSqlColumn private String targetStr;    // 广告对象 地区编码/用户ID/Tag标签
    @Getter @Setter @UtopiaSqlColumn private Boolean disabled;

    public static String ck_accountId(Long accountId) {
        return CacheKeyGenerator.generateCacheKey(OfficialAccountsTarget.class, "accountId", accountId);
    }
}
