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

package com.voxlearning.washington.helpers;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Setter;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * SMS client helper implementation.
 *
 * @author Xiaohai Zhang
 * @since Jan 24, 2014
 */
public class SmsServiceHelper {
    private static final Logger logger = LoggerFactory.getLogger(SmsServiceHelper.class);

    @Setter private RaikouSystem raikouSystem;
    @Setter private SmsServiceClient smsServiceClient;
    @Setter private UserLoaderClient userLoaderClient;

    public MapMessage sendUnbindMobileVerificationCode(Long userId, String mobile, SmsType smsType) {
        Objects.requireNonNull(smsServiceClient);
        Objects.requireNonNull(userLoaderClient);

        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请填写正确的手机号码");
        }
        if (smsType == null) {
            return MapMessage.errorMessage();
        }
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return MapMessage.errorMessage();
        }
        if (userLoaderClient.loadMobileAuthentication(mobile, user.fetchUserType()) != null) {
            return MapMessage.errorMessage("手机号码被占用，请填写其他号码");
        }

        try {
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(userId, mobile, smsType.name());
        } catch (Exception ex) {
            logger.error("Failed to send verification code to unbind mobile (userId={},mobile={},smsType={})",
                    userId, mobile, smsType, ex);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    public MapMessage sendUnbindMobileVerificationCode(String mobile, SmsType smsType, UserType userType, boolean voice) {
        Objects.requireNonNull(smsServiceClient);
        Objects.requireNonNull(userLoaderClient);

        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请填写正确的手机号码");
        }

        if (smsType == null) {
            return MapMessage.errorMessage();
        }

        if (userLoaderClient.loadMobileAuthentication(mobile, userType) != null) {
            return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
        }

        if (SmsType.STUDENT_VERIFY_MOBILE_REGISTER_MOBILE == smsType
                || SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE == smsType) {
          MapMessage verifyResponse = smsServiceClient.getSmsService().verifyMobileRisk(mobile);
          if (!verifyResponse.isSuccess()) {
            return verifyResponse;
          }
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, smsType.name(), voice);
    }

}
