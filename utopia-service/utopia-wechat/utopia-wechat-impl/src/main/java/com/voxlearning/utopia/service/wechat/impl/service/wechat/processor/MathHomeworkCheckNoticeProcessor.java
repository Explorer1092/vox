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

import com.voxlearning.alps.annotation.meta.Subject;
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
 * @author xin
 * @since 14-6-17 下午1:35
 */
@Named
@Lazy(false)
@NoArgsConstructor
public class MathHomeworkCheckNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.MathHomeworkCheckNotice;
    }

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEMPLATE_MATH_HOMEWORK_CHECK.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(new Date());
        notice.setExpireTime(getDefaultExpireTime());

        Map<String, Object> map = new HashMap<>();
        map.put("template_id", WechatNoticeTemplateIds.templateId1);
        map.put("teacherId", extensionInfo.get("teacherId"));
        map.put("teacherName", extensionInfo.get("teacherName"));
        map.put("subject", Subject.MATH);
        map.put("isQuiz", false);
        map.put("homeworkId", extensionInfo.get("homeworkId"));
        map.put("startDate", extensionInfo.get("startDate"));
        map.put("endDate", extensionInfo.get("endDate"));
        map.put("clazzId", extensionInfo.get("clazzId"));
        map.put("clazzName", extensionInfo.get("clazzName"));
        map.put("clazzLevel", extensionInfo.get("clazzLevel"));
        map.put("studentId", extensionInfo.get("studentId"));
        map.put("studentName", extensionInfo.get("studentName"));
        map.put("finished", extensionInfo.get("finished"));

        notice.setMessage(JsonUtils.toJson(map));

        wechatNoticePersistence.persist(notice);
    }
}
