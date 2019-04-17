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

package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * @author Xin Xin
 * @since 10/21/15
 */
public class TemplateMsgHandler_Teacher extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_STATUS;
    }

    @Override
    public String handle(MessageContext context) {
        WechatNoticeState state = WechatNoticeState.SENDED;
        String error = null;
        if (context.getStatus().equals("success")) {
            state = WechatNoticeState.SUCCESS;
        } else if (context.getStatus().equals("failed:user block")) {
            state = WechatNoticeState.FAILED;
            error = "usrblk";
        } else if (context.getStatus().equals("failed: system failed")) {
            state = WechatNoticeState.FAILED;
            error = "syserr";
        }

        if (state != WechatNoticeState.SENDED) {
            wechatServiceClient.updateNoticeState2(context.getFromUserName(), context.getMsgId(), state, error);
        }

        return "success";
    }
}
