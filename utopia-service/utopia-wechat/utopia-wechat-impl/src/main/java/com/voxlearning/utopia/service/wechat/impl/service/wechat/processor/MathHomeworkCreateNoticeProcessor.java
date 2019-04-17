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
import com.voxlearning.alps.core.util.StringUtils;
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
 * @since 14-5-22 下午6:10
 */

@Named
@Lazy(false)
@NoArgsConstructor
public class MathHomeworkCreateNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;

    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.MathHomeworkCreateNotice;
    }

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEMPLATE_MATH_HOMEWORK_CREATE.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setSendTime(new Date());
        notice.setExpireTime(getDefaultExpireTime());
        String dateInfo = extensionInfo.get("startDate").equals(extensionInfo.get("endDate"))
                ? "请于" + extensionInfo.get("startDate") + "内完成\n"
                : "请于" + extensionInfo.get("startDate") + "--" + extensionInfo.get("endDate") + "内完成\n";
        Map<String, Object> map = new HashMap<>();
        map.put("template_id", WechatNoticeTemplateIds.templateId2);
        map.put("first", "家长您好，" + extensionInfo.get("studentName") + "有新作业了！");
        map.put("keyword1", dateInfo);
        map.put("keyword2", Subject.MATH.getValue());
        map.put("keyword3", extensionInfo.get("units") + "，共" + extensionInfo.get("practiceCount") + "种类型");
        String keyword4 = StringUtils.isBlank(conversionService.convert(extensionInfo.get("note"), String.class)) ? "" : " " + extensionInfo.get("note");
        map.put("keyword4", extensionInfo.get("teacherName") + keyword4);
        map.put("remark", "点击查看");
        notice.setMessage(JsonUtils.toJson(map));

        wechatNoticePersistence.persist(notice);
    }
}
