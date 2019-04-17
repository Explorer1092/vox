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
 * 微信签到
 * Created by Shuai Huan on 2014/12/2.
 */
@Getter
@Setter
@UtopiaCacheExpiration(3600)
@UtopiaCacheRevision("20160728")
@DocumentTable(table = "VOX_WECHAT_SIGN")
public class WechatSign extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = -6209568010188379232L;

    private Long userId;        //用户ID
    private String signDate;    //签到日期(目前只有按月签，eg.201411)

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"userId", "signDate"}, new Object[]{userId, signDate})
        };
    }
}
