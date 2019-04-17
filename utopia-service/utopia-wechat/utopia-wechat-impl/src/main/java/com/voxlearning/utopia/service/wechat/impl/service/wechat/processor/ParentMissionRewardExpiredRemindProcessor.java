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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanguohong on 2015/3/24.
 */
@Named
@NoArgsConstructor
public class ParentMissionRewardExpiredRemindProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", extensionInfo.get("studentId"));
        map.put("studentName", extensionInfo.get("studentName"));
        map.put("expiredDay", extensionInfo.get("expiredDay"));
        Date sendTime = new Date();
        if (extensionInfo.get("sendTime") != null) {
            sendTime = (Date) extensionInfo.get("sendTime");
        }
        notice.setSendTime(sendTime);
        notice.setExpireTime(DateUtils.calculateDateDay(sendTime, 1));
        notice.setMessage(JsonUtils.toJson(map));
        notice.setMessageType(WechatNoticeType.TEMPLATE_PARENT_MISSION_REWARD_EXPIRED_REMIND.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessage(JsonUtils.toJson(map));
        notice.setDisabled(false);
        wechatNoticePersistence.persist(notice);
    }

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.ParentMissionRewardExpiredRemindNotice;
    }
}
