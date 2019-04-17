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
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by xiaopeng.yang on 2015/6/5.
 * 微信红包发放历史记录
 */
@Getter
@Setter
@DocumentTable(table = "VOX_WECHAT_RED_PACK_HISTORY")
@NoArgsConstructor
@UtopiaCacheExpiration
public class WechatRedPackHistory extends AbstractDatabaseEntity {

    private static final long serialVersionUID = -1304651946955809956L;
    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private String openId;
    @UtopiaSqlColumn private String packCategory;
    @UtopiaSqlColumn private String errorCode;
    @UtopiaSqlColumn private String errorMsg;
    @UtopiaSqlColumn private String mchId;
    @UtopiaSqlColumn private String appId;
    @UtopiaSqlColumn private String sendListId;//微信流水单号
    @UtopiaSqlColumn private Integer amount;//单位 分
    @UtopiaSqlColumn private Boolean success;
}
