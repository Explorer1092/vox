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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;

/**
 * @author xin.xin
 * @since 14-5-21 下午12:17
 * FIXME: DISABLED字段可以设置数据库缺省值FALSE
 */
@DocumentConnection(configName = "wechat")
@DocumentTable(table = "VOX_WECHAT_NOTICE")
public class WechatNotice extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 63695344319881637L;

    @NonNull @UtopiaSqlColumn(name = "USER_ID") @Getter @Setter Long userId;
    @NonNull @UtopiaSqlColumn(name = "OPEN_ID") @Getter @Setter String openId;
    @NonNull @UtopiaSqlColumn(name = "MESSAGE") @Getter @Setter String message;
    @NonNull @UtopiaSqlColumn(name = "MESSAGE_TYPE") @Getter @Setter Integer messageType;
    @NonNull @UtopiaSqlColumn(name = "STATE") @Getter @Setter Integer state;
    @UtopiaSqlColumn(name = "MESSAGE_ID") @Getter @Setter String messageId;
    @NonNull @UtopiaSqlColumn(name = "SEND_TIME") @Getter @Setter Date sendTime;
    @NonNull @UtopiaSqlColumn(name = "EXPIRE_TIME") @Getter @Setter Date expireTime;
    @UtopiaSqlColumn(name = "ERROR_CODE") @Getter @Setter String errorCode;

    public static WechatNotice newInstance(Long userId,
                                           String openId,
                                           String message,
                                           WechatNoticeType messageType,
                                           WechatNoticeState state) {
        if (userId == null) throw new NullPointerException();
        if (openId == null) throw new NullPointerException();
        if (message == null) throw new NullPointerException();
        if (messageType == null) throw new NullPointerException();
        if (state == null) throw new NullPointerException();
        WechatNotice inst = new WechatNotice();
        inst.setDisabled(false);
        inst.setSendTime(new Date());
        inst.setExpireTime(DateUtils.calculateDateDay(new Date(), 1));
        inst.setUserId(userId);
        inst.setOpenId(openId);
        inst.setMessage(message);
        inst.setMessageType(messageType.getType());
        inst.setState(state.getType());
        return inst;
    }
}
