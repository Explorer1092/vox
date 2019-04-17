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

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.spi.common.DisabledAccessor;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * App 应用内部消息（全局消息，每个用户都会有）
 * 这个表里不会有多少条数据的，每发一次全局消息会存一条数据，几个月也不会发一条
 * Created by Shuai Huan on 2015/10/8.
 */
@Getter
@Setter
@DocumentTable(table = "VOX_VENDOR_APP_GLOBAL_MESSAGE")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class VendorAppGlobalMessage extends AbstractDatabaseEntity implements DisabledAccessor {

    private static final long serialVersionUID = -2210196275493282119L;

    @UtopiaSqlColumn private String notifyContent;                   // jpush内容
    @UtopiaSqlColumn private String title;                           // 消息title
    @UtopiaSqlColumn private String summary;                         // 消息概要
    @UtopiaSqlColumn private String imgUrl;                          // 消息概要中的图片地址
    @UtopiaSqlColumn private String link;                            // 消息详细内容地址
    @UtopiaSqlColumn private Boolean disabled;                       // 是否已被删除
}
