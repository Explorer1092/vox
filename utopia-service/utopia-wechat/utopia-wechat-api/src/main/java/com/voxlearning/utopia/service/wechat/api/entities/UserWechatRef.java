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

package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * web用户与微信关注用户的关联关系
 *
 * @author xin.xin
 * @serial
 * @since 14-4-16 上午10:04
 */
@Getter
@Setter
@UtopiaCacheExpiration(3600)
@DocumentTable(table = "UCT_USER_WECHAT_REF")
@UtopiaCacheRevision("20180602")
public class UserWechatRef extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = -8087903726592055218L;

    @DocumentField("USER_ID") private Long userId;
    @DocumentField("OPEN_ID") private String openId;
    @DocumentField("UNION_ID") private String unionId;
    @DocumentField("SOURCE") private String source;
    @DocumentField("TYPE") private Integer type;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("openId", openId),
                newCacheKey("userId", userId),
                newCacheKey(new String[]{"userId", "type"}, new Object[]{userId, type}),
                newCacheKey(new String[]{"openId", "userId"}, new Object[]{openId, userId}),
                newCacheKey(new String[]{"USER_ID", "TYPE"}, new Object[]{userId, type}),
                newCacheKey("UNION_ID", unionId)
        };
    }

}
