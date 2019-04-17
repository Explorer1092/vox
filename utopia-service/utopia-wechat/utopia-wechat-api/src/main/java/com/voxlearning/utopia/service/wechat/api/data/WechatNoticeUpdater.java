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

package com.voxlearning.utopia.service.wechat.api.data;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeUpdateAction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/6/15
 */
@Getter
@Setter
@NoArgsConstructor
public class WechatNoticeUpdater implements Serializable {

    private Long id;
    private String openId;
    private String messageId;
    private WechatNoticeState state;
    private String errorCode;
    private WechatNoticeUpdateAction action;

    public WechatNoticeUpdater(Long id, String messageId) {
        this.id = id;
        this.messageId = messageId;
        this.action = WechatNoticeUpdateAction.UPDATE_MESSAGE_ID;
    }

    public WechatNoticeUpdater(Long id, WechatNoticeState state, String errorCode) {
        this.id = id;
        this.state = state;
        this.errorCode = errorCode;
        this.action = WechatNoticeUpdateAction.UPDATE_BY_ID;
    }

    public WechatNoticeUpdater(String openId, String messageId, WechatNoticeState state, String errorCode) {
        this.openId = openId;
        this.messageId = messageId;
        this.state = state;
        this.errorCode = errorCode;
        this.action = WechatNoticeUpdateAction.UPDATE_BY_MESSAGE_ID;
    }

    public String serialize() {
        return JsonStringSerializer.getInstance().serialize(this);
    }

    public static WechatNoticeUpdater deserialize(String source) throws Exception {
        if (StringUtils.isEmpty(source)) {
            throw new IllegalArgumentException("Empty source");
        }
        return JsonStringDeserializer.getInstance().deserialize(source, WechatNoticeUpdater.class);
    }
}
