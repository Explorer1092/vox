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

package com.voxlearning.washington.controller.finance;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableBoolean;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.washington.support.AbstractController;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 无线冲值回调借口
 * Created by Alex on 14-11-12.
 */
@RequestMapping(value = "/finance/charging")
@Controller
public class WirelessChargingController extends AbstractController {

    private static final String SUCCESS_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<ctuport>\n   <result>0000</result>\n</ctuport>\n";
    private static final String VALIDATE_FAILED_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<ctuport>\n   <result>1007</result>\n</ctuport>\n";
    private static final String UNKNOWN_ORDER_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<ctuport>\n   <result>1004</result>\n</ctuport>\n";

    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;

    @RequestMapping(value = "callback.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    String callback(String CompanyID, String Mobile, String Amount, String OrderID, String Result, String Key) {
        //logger.info("Wireless charging callback invoked! {},{},{},{},{},{}", CompanyID, Mobile, Amount, OrderID, Result, Key);

        // 验证参数有效性
        String callbackKey = commonConfiguration.getWirelessChargingCallbackKey();

        StringBuffer buffer = new StringBuffer();
        buffer.append(CompanyID).append(Mobile).append(Amount).append(OrderID).append(Result).append(callbackKey);
        String validateKey = DigestUtils.md5Hex(buffer.toString());
        if (!validateKey.equals(Key)) {
            return VALIDATE_FAILED_RESULT;
        }

        // 验证订单数据是否一致
        WirelessCharging chargingItem = wirelessChargingServiceClient.getWirelessChargingService()
                .loadWirelessCharging(OrderID)
                .getUninterruptibly();
        if (chargingItem == null) {
            return UNKNOWN_ORDER_RESULT;
        }

        int chargingAmount = ConversionUtils.toInt(Amount);
        boolean mobileEquals = sensitiveUserDataServiceClient.mobileEquals(chargingItem.getTargetSensitiveMobile(), Mobile);
        if (!mobileEquals || chargingItem.getAmount() != chargingAmount) {
            return UNKNOWN_ORDER_RESULT;
        }

        Long userId = chargingItem.getUserId();

        // 更新订单状态
        if ("0".equals(Result)) {
            boolean ret = wirelessChargingServiceClient.getWirelessChargingService()
                    .updateChargingSuccess(OrderID)
                    .getUninterruptibly();
            // 充值成功发送提醒短信，服务周到哇，微信优先~~~
            if (ret && StringUtils.isNotBlank(chargingItem.getNotifySmsMessage())) {
                final MutableBoolean wechatNoticeSended = new MutableBoolean(false);
                Map<Long, List<UserWechatRef>> map = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(userId), WechatType.TEACHER);
                List<UserWechatRef> refs = map.get(userId);
                if (CollectionUtils.isNotEmpty(refs)) {
                    Map<String, Object> extensionInfo = MiscUtils.m("content", chargingItem.getNotifySmsMessage(), "mobile", StringHelper.mobileObscure(Mobile));
                    refs.stream()
                            .filter(source -> !source.isDisabledTrue())
                            .forEach(e -> {
                                wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.WirelessChargingNotice,
                                        userId, e.getOpenId(), extensionInfo);
                                wechatNoticeSended.setTrue();
                            });
                }
                if (wechatNoticeSended.isFalse()) {
                    userSmsServiceClient.buildSms().to(chargingItem)
                            .content(chargingItem.getNotifySmsMessage())
                            .type(SmsType.WIRELESS_CHARGING_SUCCESS_SMS)
                            .send();
                }
            }
        } else {
            wirelessChargingServiceClient.getWirelessChargingService()
                    .updateChargingFailed(OrderID, "", "")
                    .awaitUninterruptibly();
        }

        return SUCCESS_RESULT;
    }


//    @RequestMapping(value = "{chargeType}/summary.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    MapMessage summary(@PathVariable("chargeType") Integer chargetType) {
//        ChargeType type = ChargeType.get(chargetType);
//        if (type == null) {
//            return MapMessage.errorMessage("未知的充值类型");
//        }
//
//        int summary = businessFinanceServiceClient.loadChargeSummary(type);
//        int teacherSum = summary / 30;
//
//        return MapMessage.successMessage().add("summary", summary).add("persons", teacherSum);
//    }

}
