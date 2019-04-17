/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author HuanYin Jia
 * @since 2015/5/22
 */
@Getter
@Setter
@DocumentConnection(configName = "wechat")
@DocumentTable(table = "VOX_WECHAT_NOTICE_HISTORY")
public class WechatNoticeHistory extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 63695344319881637L;

    @DocumentField("USER_ID") Long userId;
    @DocumentField("OPEN_ID") String openId;
    @DocumentField("MESSAGE") String message;
    @DocumentField("MESSAGE_TYPE") Integer messageType;
    @DocumentField("STATE") Integer state;
    @DocumentField("MESSAGE_ID") String messageId;
    @DocumentField("SEND_TIME") Date sendTime;
    @DocumentField("EXPIRE_TIME") Date expireTime;
    @DocumentField("ERROR_CODE") String errorCode;
}