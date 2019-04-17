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

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 家长端抢购商品提醒消息
 * Created by Shuai Huan on 2015/4/20.
 */
@Named
@NoArgsConstructor
public class RushToPurchaseRemindNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.RushToPurchaseRemindNotice;
    }

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEMPLATE_RUSH_TO_PURCHASE_REMIND.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        if(extensionInfo.get("sendTime") != null) {
            Date sendTime = (Date) extensionInfo.get("sendTime");
            notice.setSendTime(sendTime);
        }
        notice.setExpireTime(getDefaultExpireTime());
        Map<String, Object> map = new HashMap<>();
        map.put("realName", extensionInfo.get("realName"));
        map.put("startTime", extensionInfo.get("startTime"));
        notice.setMessage(JsonUtils.toJson(map));
        wechatNoticePersistence.persist(notice);
    }
}
