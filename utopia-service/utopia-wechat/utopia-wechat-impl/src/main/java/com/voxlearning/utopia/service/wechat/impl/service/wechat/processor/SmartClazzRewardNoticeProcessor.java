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
import com.voxlearning.utopia.service.wechat.impl.support.WechatNoticeTemplateIds;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 智慧教室课堂奖励
 * Created by Shuai Huan on 2014/10/20.
 */
@Named
@Lazy(false)
@NoArgsConstructor
public class SmartClazzRewardNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.SmartClazzRewardNotice;
    }

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEMPLATE_SMARTCLAZZ_STUDENT_REWARD.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(new Date());
        notice.setExpireTime(getDefaultExpireTime());
        Map<String, Object> map = new HashMap<>();
        map.put("template_id", WechatNoticeTemplateIds.templateId1);
        map.put("first", extensionInfo.get("studentName") + "家长您好：\n"
                + extensionInfo.get("teacherName") + "老师刚刚在课堂上奖励您的孩子\n");
        map.put("keyword1", "智慧课堂奖励");
        map.put("keyword2", new Date());
        map.put("remark", "↓请点击“课堂表现”查看");
        notice.setMessage(JsonUtils.toJson(map));

        wechatNoticePersistence.persist(notice);
    }
}
