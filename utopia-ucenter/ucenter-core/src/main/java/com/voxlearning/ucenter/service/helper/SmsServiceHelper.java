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

package com.voxlearning.ucenter.service.helper;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * SMS client helper implementation.
 * TODO 与washington代码重复，考虑抽出一个单独的webapp-core层，做为单独的一层封装
 * @author Xiaohai Zhang
 * @since Jan 24, 2014
 */
@Named
public class SmsServiceHelper {
    private static final Logger logger = LoggerFactory.getLogger(SmsServiceHelper.class);

    @Inject private RaikouSystem raikouSystem;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    public MapMessage sendUnbindMobileVerificationCode(Long userId, String mobile, SmsType smsType){
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

    public MapMessage sendUnbindMobileVerificationCode(String mobile, SmsType smsType, UserType userType, String ip, boolean voice) {
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
        try {
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, smsType.name(), voice);
        } catch (Exception ex) {
            logger.error("Failed to send verification code to unbind mobile (mobile={},smsType={},userType={},ip={})",
                    mobile, smsType, userType, ip, ex);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    public MapMessage sendUnbindMobileVerificationCode(String mobile, SmsType smsType, UserType userType, String ip) {
        return sendUnbindMobileVerificationCode(mobile, smsType, userType, ip, false);
    }

    public MapMessage sendMobileVerificationCodeToMobileForPaymentPassword(Long studentId) {
        Objects.requireNonNull(smsServiceClient);

        if (studentId == null) {
            return MapMessage.errorMessage();
        }
        String mobile = locateStudentSensitiveMobile(studentId);
        return smsServiceClient.getSmsService().sendMobileVerificationCodeToMobileForPaymentPassword(studentId, mobile);
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private String locateStudentSensitiveMobile(Long studentId) {

        String mobile = null;
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(studentId);
        if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
            mobile = sensitiveUserDataServiceClient.showUserMobile(studentId, "ucenter:locateStudentSensitiveMobile", SafeConverter.toString(studentId));
        } else {
            StudentParent sp = parentLoaderClient.loadStudentKeyParent(studentId);
            if (sp == null || sp.getParentUser() == null) {
                return null;
            }
            userAuthentication = userLoaderClient.loadUserAuthentication(sp.getParentUser().getId());
            if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                mobile = sensitiveUserDataServiceClient.showUserMobile(sp.getParentUser().getId(), "sms:message", SafeConverter.toString(sp.getParentUser().getId()));
            }
        }
        return mobile;
    }
}
