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
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.impl.dao.UserWechatRefPersistence;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatNoticePersistence;
import com.voxlearning.utopia.service.wechat.impl.support.WechatNoticeTemplateIds;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生未支付的订单发通知
 * Created by Shuai Huan on 2014/11/10.
 */
@Named
@Lazy(false)
@NoArgsConstructor
public class UnpaidAfentiOrderRemindNoticeProcessor extends AbstractNoticeProcessor {

    @Inject private WechatNoticePersistence wechatNoticePersistence;
    @Inject private UserWechatRefPersistence userWechatRefPersistence;


    @Override
    public WechatNoticeProcessorType type() {
        return WechatNoticeProcessorType.UnpaidAfentiOrderRemindNotice;
    }

    @Override
    protected void processWechat(Long userId, String openId, Map extensionInfo) {

        UserWechatRef ref = userWechatRefPersistence.findByOpenId(openId);
        if (ref == null || DateUtils.dayDiff(new Date(), ref.getCreateDatetime()) < 10) {
            return;
        }
        if (MapUtils.isEmpty(extensionInfo)) {
            return;
        }
        WechatNotice notice = new WechatNotice();
        notice.setUserId(userId);
        notice.setOpenId(openId);
        notice.setMessageType(WechatNoticeType.TEMPLATE_UNPAID_ORDER_REMIND.getType());
        notice.setState(WechatNoticeState.WAITTING.getType());
        notice.setDisabled(false);
        notice.setExpireTime(getDefaultExpireTime());
        notice.setSendTime(new Date());
        Map<String, Object> map = new HashMap<>();
        map.put("template_id", WechatNoticeTemplateIds.templateId4);
        map.put("first", MessageFormat.format("您的孩子{0}想要开通【{1}】，点击查看>>", extensionInfo.get("studentName"), extensionInfo.get("productName")));

        map.put("ordertape", extensionInfo.get("orderCreateTime"));
        map.put("orderId", extensionInfo.get("orderId"));
        map.put("remark", "点击查看详情");
        notice.setMessage(JsonUtils.toJson(map));
        wechatNoticePersistence.persist(notice);
    }
}
