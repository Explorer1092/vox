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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticePersistence;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 家长端 满分小状元 模板消息
 * Created by Summer Yang on 2015/7/3.
 */
@Named
@NoArgsConstructor
public class ParentLittleChampionNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.ParentLittleChampionNotice;
    }

    @Override
    public void processWechat(Long userId, String openId, Map extensionInfo) {
        if(userId == null){
            return;
        }
        String sendTime = "2015-07-04 10:00:00";
        // 测试账号 线上 24680223
        if(24680223 == userId){
            sendTime = "2015-07-03 22:50:00";
        }
        WechatNotice notice = new WechatNotice();
        notice.setMessage(JsonUtils.toJson(extensionInfo));
        notice.setMessageType(WechatNoticeType.TEMPLATE_PARENT_LITTLE_CHAMPION_NOTICE.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setDisabled(false);
        notice.setSendTime(DateUtils.stringToDate(sendTime));//2015-07-04 10:00:00
        notice.setExpireTime(getDefaultExpireTime());
        wechatNoticePersistence.persist(notice);
    }
}
