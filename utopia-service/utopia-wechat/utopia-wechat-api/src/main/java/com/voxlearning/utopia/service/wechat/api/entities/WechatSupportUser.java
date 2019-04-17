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
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * 一起作业技术支持微信公众号的关注用户表(公司内部人员)
 *
 * @author xin
 * @serial
 * @since 2014-08-18 1:56PM
 */
@Getter
@Setter
@UtopiaCacheExpiration(3600)
@UtopiaCacheRevision("20160728")
@DocumentTable(table = "VOX_WECHAT_SUPPORT_USER")
public class WechatSupportUser extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = -1802452706690819341L;

    private String openId;
    private String name;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("openId", openId)
        };
    }
}
