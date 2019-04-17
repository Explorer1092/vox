/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.MizarOrderType;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.business.impl.utils.SeattleWechatMsgUtils;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.BusinessActivity;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2016/12/12.
 */
@Named
@Slf4j
public class SeattleOrderRemindFilter extends UserOrderFilter {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private BusinessActivityManagerClient businessActivityManagerClient;
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder orderRecord = context.getOrder();
        // 判断是否是通用导流订单
        if (orderRecord.getOrderType() == OrderType.seattle && StringUtils.isNotBlank(orderRecord.getExtAttributes())) {
            // 记录一条课时与用户的关联, 统一存在trustee_order方便查询
            TrusteeOrderRecord orderRef = TrusteeOrderRecord.newOrder();
            orderRef.setParentId(orderRecord.getUserId());
            orderRef.setStudentId(SafeConverter.toLong(JsonUtils.fromJson(orderRecord.getExtAttributes()).get("studentId")));
            orderRef.setPrice(orderRecord.getOrderPrice());
            orderRef.setActivityId(SafeConverter.toLong(orderRecord.getProductId()));
            orderRef.setStatus(TrusteeOrderRecord.Status.Paid);
            orderRef.setRemark(SafeConverter.toString(JsonUtils.fromJson(orderRecord.getExtAttributes()).get("remark")));
            orderRef.setTrack(SafeConverter.toString(JsonUtils.fromJson(orderRecord.getExtAttributes()).get("track")));
            orderRef.setOrderType(MizarOrderType.COMMON_PAY.getCode());
            // 在此处同步一下外部支付流水号等信息, 方法同 UserPaymentHistoryFilter
            PaymentVerifiedData paymentVerifiedData = context.getCallbackContext().getVerifiedPaymentData();
            orderRef.setOutTradeNo(paymentVerifiedData.getExternalTradeNumber()); // 外部支付流水号
            orderRef.setPayAmount(paymentVerifiedData.getPayAmount()); // 支付金额
            orderRef.setPayMethod(context.getCallbackContext().getPayMethodGateway()); // 支付渠道
            AtomicLockManager.instance().wrapAtomic(trusteeOrderServiceClient)
                    .keyPrefix("ParentService:saveTrusteeOrder")
                    .keys(orderRecord.getId())
                    .proxy().saveTrusteeOrder(orderRef);
            // 做提醒相关
            long businessActivityId = SafeConverter.toLong(orderRecord.getProductId());
            if (businessActivityId != 0) {
                BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                        .load(businessActivityId);
                if (activity != null && Boolean.TRUE.equals(activity.getSendMsg())
                        && StringUtils.isNoneBlank(activity.getMsgTitle(), activity.getMsgContent())) {
                    // 发支付成功通知消息
                    AppMessage message = new AppMessage();
                    message.setUserId(orderRecord.getUserId());
                    message.setMessageType(ParentMessageType.REMINDER.getType());
                    message.setTitle(activity.getMsgTitle());
                    message.setContent(activity.getMsgContent());
                    message.setLinkUrl(activity.getReturnUrl());
                    Map<String, Object> extInfo = new HashMap<>();
                    extInfo.put("tag", ParentMessageTag.通知.name());
                    message.setExtInfo(extInfo);
                    message.setLinkType(0);
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                }

                // 短信消息提醒
                if (activity != null && StringUtils.isNotEmpty(activity.getSmsContent())) {
                    // 先查询手机号
                    String phone = sensitiveUserDataServiceClient.showUserMobile(orderRecord.getUserId(), "be:SeattleOrderRemindFilter", "9999");
                    if (StringUtils.isNoneBlank(phone)) {
                        // 发短信
                        smsServiceClient.createSmsMessage(phone).content(activity.getSmsContent()).type(SmsType.PAY_SUCCESS_SMS_NOTIFY.name()).send();
                    }
                }

                // 微信模板消息提醒
                if (activity != null && StringUtils.isNotBlank(activity.getWechatContent())) {
                    // 订单是否来源于微信渠道
                    if (StringUtils.equals(context.getCallbackContext().getPayMethodGateway(), "wechatpay")) {
                        wechatServiceClient.getWechatService().processWechatNoticeNoWait(
                                WechatNoticeProcessorType.ParentOperationalNotice, orderRecord.getUserId(), SeattleWechatMsgUtils.extensionInfo(activity, RuntimeMode.current()), WechatType.PARENT
                        );
                    }
                }
            }
        } // if seattle type
        chain.doFilter(context); // go on
    }
}
