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

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticePersistence;
import com.voxlearning.utopia.service.wechat.impl.support.WechatNoticeTemplateIds;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2016/6/1
 */
@Named
@NoArgsConstructor
public class ParentOperationalNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEMPLATE_PARENT_OPERATIONAL_NOTICE.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        if (extensionInfo.containsKey("sendTime")) {
            Date curTime = new Date();

            Date reqSendTime = SafeConverter.toDate(extensionInfo.get("sendTime"));
            Date sendTime = curTime.after(reqSendTime) ? curTime : reqSendTime;
            notice.setSendTime(sendTime);
        } else {
            notice.setSendTime(new Date());
        }
        notice.setExpireTime(getExpireTime(notice.getSendTime()));
        Map<String, Object> map = new HashMap<>();
        map.put("template_id", WechatNoticeTemplateIds.templateId7);
        map.put("first", extensionInfo.get("first"));
        map.put("keyword1", extensionInfo.get("keyword1"));
        map.put("keyword2", extensionInfo.get("keyword2"));
        map.put("remark", extensionInfo.get("remark"));
        map.put("url", extensionInfo.get("url"));
        map.put("isWkt", extensionInfo.get("isWkt"));
        notice.setMessage(JsonUtils.toJson(map));
        wechatNoticePersistence.persist(notice);
    }

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.ParentOperationalNotice;
    }
}
