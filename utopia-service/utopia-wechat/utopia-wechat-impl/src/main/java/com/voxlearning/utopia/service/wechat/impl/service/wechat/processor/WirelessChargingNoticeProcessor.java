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

package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticePersistence;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @since 7/23/2015
 */
@Named
@Lazy(false)
@NoArgsConstructor
public class WirelessChargingNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEACHER_TEMPLATE_WIRELESS_CHARGING_NOTICE.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(new Date());
        notice.setExpireTime(getDefaultExpireTime());
        notice.setMessage(JsonUtils.toJson(extensionInfo));
        wechatNoticePersistence.persist(notice);
    }

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.WirelessChargingNotice;
    }
}
