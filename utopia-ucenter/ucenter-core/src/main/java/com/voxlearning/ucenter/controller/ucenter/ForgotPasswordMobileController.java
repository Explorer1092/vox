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

package com.voxlearning.ucenter.controller.ucenter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.service.user.AccountWebappService;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

import static com.voxlearning.alps.annotation.meta.UserType.STUDENT;
import static com.voxlearning.alps.annotation.meta.UserType.TEACHER;

/**
 * @author RuiBao
 * @version 0.1
 * @since 8/20/2015
 */
@Controller
@RequestMapping("/ucenter")
public class ForgotPasswordMobileController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AccountWebappService accountWebappService;

    // 根据输入的手机号码查询用户，初始化context
    @RequestMapping(value = "mrpwds1.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileResetPasswordStep1() {
        String mobile = getRequestParameter("mobile", "");
        String captchaToken = getRequestParameter("captchaToken", "");
        String captchaCode = getRequestParameter("captchaCode", "");

        if (StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码输入错误，请重新输入。");
        }

        List<CandidateUserInfo> users = findCandidateUsers(mobile);
        if (CollectionUtils.isEmpty(users)) return MapMessage.errorMessage("手机号不正确");

        MobileResetPasswordContext context = MobileResetPasswordContext.init(users, mobile, "", "");
        smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.FORGOT_PWD.name(), false);
        saveContext(context);
        return MapMessage.successMessage()
                .add("mobile", mobile)
                .add("token", context.token)
                .add("needUserType", users.size() > 1)
                .add("timer", 60);
    }

    // 如果手机号码绑定用户超过1个，选择用户类型
    @RequestMapping(value = "mrpwds2.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileResetPasswordStep2() {
        MobileResetPasswordContext context = getContext(getRequestString("token"));
        if (context == null) return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");

        int userType = getRequestInt("userType");
        if (userType != 1 && userType != 3) return MapMessage.errorMessage("请重新选择");

        context.userType = userType;
        saveContext(context);
        return MapMessage.successMessage().add("token", context.token).add("mobile", context.mobile);
    }

    // 输入验证码
    @RequestMapping(value = "mrpwds3.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileResetPasswordStep3() {
        MobileResetPasswordContext context = getContext(getRequestString("token"));
        if (context == null) return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");

        String code = getRequestParameter("code", "");
        if (StringUtils.isEmpty(code)) return MapMessage.errorMessage("请输入验证码");

        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(context.mobile, code, SmsType.FORGOT_PWD.name());
        if(!message.isSuccess()){
            return message;
        }
        context.verified = true;
        saveContext(context);
        return MapMessage.successMessage().add("token", context.token).add("mobile", context.mobile);
    }

    // 重置密码
    @RequestMapping(value = "mrpwds4.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileResetPasswordStep4() {
        MobileResetPasswordContext context = getContext(getRequestString("token"));
        if (context == null) return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");
        if (!context.verified) return MapMessage.errorMessage("重置密码失败");

        String password = getRequestString("password");
        if (StringUtils.isBlank(password)) return MapMessage.errorMessage("请输入密码");

        CandidateUserInfo candidate = context.candidateUsers
                .stream()
                .filter(source -> Objects.equals(source.userType, context.userType))
                .findFirst()
                .orElse(null);
        if (candidate == null) return MapMessage.errorMessage("重置密码失败");

        User user = raikouSystem.loadUser(candidate.userId);
        if (user == null) return MapMessage.errorMessage("重置密码失败");

        if (!userServiceClient.setPassword(user, password).isSuccess()) {
            return MapMessage.successMessage("重置密码失败");
        }

        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(user.getId());
        userServiceRecord.setOperatorId(user.getId().toString());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改密码");
        userServiceRecord.setComments(FindPasswordMethod.MOBILE.getDescription());
        userServiceRecord.setAdditions("refer:ForgotPasswordMobileController.mobileResetPasswordStep4");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        // 销毁context
        destroyContext(context);

        // 重置密码后处理
        accountWebappService.onPasswordReset(user, password);

        return MapMessage.successMessage("重置密码成功");
    }

    // 发送验证码
    @RequestMapping(value = "svc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendVerificationCode() {
        try {
            MobileResetPasswordContext context = getContext(getRequestString("token"));
            if (context == null) return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");

            String mobile = context.mobile;

            int expire = 60;
            if (context.mobileLastSendTime != 0) {
                int delta = (int) ((System.currentTimeMillis() - context.mobileLastSendTime) / 1000);
                if (expire - delta > 0) {
                    return MapMessage.errorMessage("验证码已发送，如未收到请1分钟后再试").add("timer", expire - delta);
                }
            }

            smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.FORGOT_PWD.name(), false);
            context.mobileLastSendTime = System.currentTimeMillis();
            saveContext(context);
            return MapMessage.successMessage("发送验证码成功").add("timer", expire);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("发送验证码失败");
    }

    // private method

    private List<CandidateUserInfo> findCandidateUsers(String mobile) {
        try {
            List<UserAuthentication> uas = userLoaderClient.loadMobileAuthentications(mobile);
            if (CollectionUtils.isEmpty(uas)) return Collections.emptyList();
            List<CandidateUserInfo> candidates = new ArrayList<>();
            for (UserAuthentication ua : uas) {
                User user = raikouSystem.loadUser(ua.getId());
                if (user == null || !Arrays.asList(STUDENT, TEACHER).contains(user.fetchUserType())) continue;
                CandidateUserInfo candidate = new CandidateUserInfo();
                candidate.userId = user.getId();
                candidate.userType = user.getUserType();
                candidates.add(candidate);
            }
            return candidates;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void saveContext(MobileResetPasswordContext context) {
        ucenterWebCacheSystem.CBS.flushable.set("MobileResetPassword:" + context.token, 3600, context);
    }

    private MobileResetPasswordContext getContext(String token) {
        return ucenterWebCacheSystem.CBS.flushable.load("MobileResetPassword:" + token);
    }

    private void destroyContext(MobileResetPasswordContext context) {
        ucenterWebCacheSystem.CBS.flushable.delete("MobileResetPassword:" + context.token);
    }

    // inner class

    static public class MobileResetPasswordContext implements Serializable {
        private static final long serialVersionUID = 7083730345247631573L;

        @Getter @Setter String token;
        List<CandidateUserInfo> candidateUsers;
        Integer userType;
        String mobile;
        boolean verified = false;
        long mobileLastSendTime;
        String mobileLastCode;
        String trackId;

        static MobileResetPasswordContext init(List<CandidateUserInfo> users, String mobile, String code, String trackId) {
            MobileResetPasswordContext context = new MobileResetPasswordContext();
            context.token = RandomUtils.randomString(32);
            context.candidateUsers = new ArrayList<>(users);
            context.mobile = mobile;
            context.mobileLastCode = code;
            context.trackId = trackId;
            context.mobileLastSendTime = System.currentTimeMillis();
            if (users.size() == 1) context.userType = users.get(0).userType;
            context.cleanCurrentUser();
            return context;
        }

        void cleanCurrentUser() {
            verified = false;
        }
    }

    @EqualsAndHashCode(of = {"userId"})
    static public class CandidateUserInfo implements Serializable {
        private static final long serialVersionUID = -1240308716823261497L;

        Long userId;
        Integer userType;
    }
}
