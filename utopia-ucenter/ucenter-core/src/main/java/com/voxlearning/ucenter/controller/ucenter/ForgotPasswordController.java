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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.service.user.AccountWebappService;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.footprint.client.ForgotPasswordDetailServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 15/12/2015
 */
@Controller
@RequestMapping(value = "/ucenter")
public class ForgotPasswordController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private ForgotPasswordDetailServiceClient forgotPasswordDetailServiceClient;
    @Inject private AccountWebappService accountWebappService;

    private final static String ResetPasswordEntryUrl = "/ucenter/resetnavigation.vpage";

    /**
     * 找回密码统一入口
     */
    @RequestMapping(value = "resetnavigation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String forgotPasswordAndAccountIndex(Model model) {
        return resetPwdStep1(model);
    }

    /**
     * 找回密码步骤(统一入口之后的step都由这里进入)
     */
    @RequestMapping(value = "resetpwdstep.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String resetpwdstep(Model model) {
        Pattern p = Pattern.compile("^\\w+$");
        String currentStep = getRequestParameter("step", "step1");
        if (!p.matcher(currentStep).find())
            currentStep = "step1";

        if (currentStep.startsWith("step3_")) {
            currentStep = "step3"; //第三步分很多种:step3_phone/step3_email/step3_sercurityquestion等等
        }

        switch (currentStep) {
            case "step1":
                return resetPwdStep1(model);
            case "step2":
                return resetPwdStep2(model);
            case "step3":
                return resetPwdStep3(model);
            case "step4":
                return resetPwdStep4(model);
            case "step5":
                return resetPwdStep5(model);
            default:
                return resetPwdStep1(model);
        }
    }

    @RequestMapping(value = "resetpwdstart.vpage")
    @ResponseBody
    public MapMessage resetpwdstart() {
        String account = getRequestParameter("account", "");
        String captchaToken = getRequestParameter("captchaToken", "");
        String captchaCode = getRequestParameter("captchaCode", "");
        int userType = getRequestInt("userType");

        if (StringUtils.isBlank(account)) {
            return MapMessage.errorMessage("请正确输入账号信息。");
        }
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码输入错误，请重新输入。");
        }
        if (userType != 0 && !Arrays.asList(1, 2, 3, 8).contains(userType)) {
            return MapMessage.errorMessage("请重新选择用户类型");
        }

        User user;
        if (userType == 0) {// 第一次进入，没有指定角色
            List<User> users = userLoaderClient.loadUserByToken(account);
            if (CollectionUtils.isEmpty(users)) {
                return MapMessage.errorMessage("您输入的账号不存在，请试试其他手机号吧 ");
            }
            if (users.size() > 1) {
                saveCaptchaCode(captchaToken, captchaCode);// 因为会选择角色进行第二次验证，所以在此添加同样的code重复调用
                return MapMessage.successMessage()
                        .add("needSelect", true)
                        .add("users", users.stream().map(User::getUserType).collect(Collectors.toList()));
            }
            user = users.get(0);
        } else {// 第二次进入，指定了角色
            List<User> users = userLoaderClient.loadUsers(account, UserType.of(userType));
            if (CollectionUtils.isEmpty(users)) {
                return MapMessage.errorMessage("您输入的账号不存在，请试试其他手机号吧 ");
            }
            user = users.get(0);
        }

        ResetPasswordUserInfo info = fillResetPasswordInfo(user);
        ResetPasswordContext context = ResetPasswordContext.newForUsers(info);
        saveResetPasswordContext(context);
        return context.getDataMapForDisplay();
    }

    /**
     * 用于从忘记账号直接进入忘记密码，不需要验证captcha
     */
    @RequestMapping(value = "resetpwdstartwoc.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map forgetpasswordWithoutCaptcha() {
        String account = getRequestParameter("account", "");
        int userType = getRequestInt("userType");

        if (StringUtils.isBlank(account)) {
            return MapMessage.errorMessage("请正确输入账号信息。");
        }
        if (!Arrays.asList(1, 2, 3).contains(userType)) {
            return MapMessage.errorMessage("请重新选择用户类型");
        }

        User user = userLoaderClient.loadUsers(account, UserType.of(userType)).stream().findFirst().orElse(null);
        if (user == null) {
            return MapMessage.errorMessage("找不到用户");
        }

        ResetPasswordUserInfo info = fillResetPasswordInfo(user);
        ResetPasswordContext context = ResetPasswordContext.newForUsers(info);    // 如果没有找到，context中的candidateUser是null
        saveResetPasswordContext(context);
        return context.getDataMapForDisplay();
    }

    /**
     * 找回密码--重置密码
     */
    @RequestMapping(value = "resetpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processResetpwForm() {
        ResetPasswordContext resetPasswordContext = getResetPasswordContext(getRequestParameter("token", ""));
        // 只有当前token对应的用户被验证过的时候，才能执行强制重置密码
        if (resetPasswordContext != null && resetPasswordContext.isCurrentUserVerified) {
            String newPassword = getRequestParameter("password", null);
            Long userId = resetPasswordContext.candidateUser.userId;
            User user = raikouSystem.loadUser(userId);

            if (!userServiceClient.setPassword(user, newPassword).isSuccess()) {
                return MapMessage.successMessage("重置密码失败");
            }

            // 记录重置密码日志
            saveForgotPwdDetail(resetPasswordContext, resetPasswordContext.method, "ForgotPasswordAccountController.processResetpwForm");
            // 销毁context
            destroyResetPasswordContext(resetPasswordContext);

            // 重置密码后处理
            accountWebappService.onPasswordReset(user, newPassword);

            return MapMessage.successMessage("重置密码成功");
        }
        return MapMessage.successMessage("重置密码失败");
    }

    /**
     * 当点击客服找回密码的时候会鸟儿悄儿的的往这里发个请求记录一下
     * FIXME 这块是哪调用的
     */
    @RequestMapping(value = "forgotpwdcallcenter.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage callCenter() {
        String token = getRequestParameter("token", "");
        ResetPasswordContext context = getResetPasswordContext(token);
        if (null == context) {
            return MapMessage.errorMessage();
        }
        context.method = FindPasswordMethod.CALL_CENTER;
        return MapMessage.successMessage();
    }

    private ResetPasswordUserInfo fillResetPasswordInfo(User user) {
        if (null == user) {
            return null;
        }

        ResetPasswordUserInfo info = new ResetPasswordUserInfo();
        info.setUserId(user.getId());
        info.setRealName(user.getProfile().getRealname());
        info.setUserType(user.getUserType());

        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.getId());
        String phone = sensitiveUserDataServiceClient.showUserMobile(user.getId(), "fillResetPasswordInfo", SafeConverter.toString(user.getId()));
        String ae = sensitiveUserDataServiceClient.loadUserEmail(user.getId(), "fillResetPasswordInfo");
        if (phone != null) {
            info.mobile = phone;
        }
        if (ae != null) {
            info.email = ae;
        }

        if (info.userType == UserType.STUDENT.getType() && (null == info.mobile || null == info.email)) {
            StudentParent sp = parentLoaderClient.loadStudentKeyParent(user.getId());
            User parent = sp == null ? null : sp.getParentUser();
            if (parent != null) {
                String am = sensitiveUserDataServiceClient.showUserMobile(parent.getId(), "fillResetPasswordInfo", SafeConverter.toString(parent.getId()));
                ae = sensitiveUserDataServiceClient.loadUserEmail(parent.getId(), "fillResetPasswordInfo");

                if (StringUtils.isBlank(info.mobile) && am != null) {
                    info.mobile = am;
                }
                if (StringUtils.isBlank(info.email) && ae != null) {
                    info.email = ae;
                }
            }
        }

        return info;
    }

    private void saveForgotPwdDetail(ResetPasswordContext context, FindPasswordMethod method, String refer) {
        if (context.candidateUser == null) {
            return;
        }

        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(context.candidateUser.getUserId());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改密码");
        userServiceRecord.setComments(method.getDescription());
        if (StringUtils.isNoneEmpty(refer)) {
            userServiceRecord.setAdditions("refer:" + refer);
        }

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }

    private void saveResetPasswordContext(ResetPasswordContext context) {
        ucenterWebCacheSystem.CBS.flushable.set("ResetPassword:" + context.token, 3600, context);
    }

    private void destroyResetPasswordContext(ResetPasswordContext context) {
        ucenterWebCacheSystem.CBS.flushable.delete("ResetPassword:" + context.token);
    }

    private ResetPasswordContext getResetPasswordContext(String token) {
        return ucenterWebCacheSystem.CBS.flushable.load("ResetPassword:" + token);
    }

    //////////////////////////////////
    ///////找回密码步骤
    /////////////////////////////////

    //找回密码-步骤1
    private String resetPwdStep1(Model model) {
        model.addAttribute("captchaToken", RandomUtils.randomString(24));

        return "ucenter/resetpassword/chip/step1";
    }

    //找回密码-步骤2
    private String resetPwdStep2(Model model) {
        String contextToken = getRequestParameter("token", "");
        ResetPasswordContext context = getResetPasswordContext(contextToken);

        if (context == null || context.candidateUser == null) {
            return "redirect:" + ResetPasswordEntryUrl;
        }

        ResetPasswordUserInfo userInfo = context.candidateUser;
        model.addAttribute("userInfo", userInfo.getDataMapForDisplay());
        model.addAttribute("context", context);
        return "ucenter/resetpassword/chip/step2";
    }

    //找回密码-步骤3
    //目前这一步分三种:step3_phone/step3_email/step3_securityquestion 2015-12-15
    private String resetPwdStep3(Model model) {
        String contextToken = getRequestParameter("token", "");
        ResetPasswordContext context = getResetPasswordContext(contextToken);

        if (context == null || context.candidateUser == null) {
            return "redirect:" + ResetPasswordEntryUrl;
        }

        String currentStep = getRequestParameter("step", "step1");
        if (!StringUtils.equals(currentStep, "step3_phone")
                && !StringUtils.equals(currentStep, "step3_email")
                && !StringUtils.equals(currentStep, "step3_securityquestion")) {
            currentStep = "step1";
        }

        ResetPasswordUserInfo userInfo = context.candidateUser;
        model.addAttribute("userInfo", userInfo.getDataMapForDisplay());
        model.addAttribute("context", context);

        if ("step3_securityquestion".equals(currentStep)) {
            //如果用户选择的是密码找回，则随机选择三个问题中的一个，返给前端，并且把问题和答案存到context中
            List<String> questions = new ArrayList<>(userInfo.questionAndAnswer.keySet());
            String question = questions.get(RandomUtils.nextInt(questions.size()));
            String answer = userInfo.questionAndAnswer.get(question);
            context.question = question;
            context.answer = answer;
            saveResetPasswordContext(context);
            model.addAttribute("question", question);
        }


        return "ucenter/resetpassword/chip/" + currentStep;
    }

    //找回密码-步骤4
    private String resetPwdStep4(Model model) {
        String contextToken = getRequestParameter("token", "");
        ResetPasswordContext context = getResetPasswordContext(contextToken);

        if (context == null || context.candidateUser == null) {
            return "redirect:" + ResetPasswordEntryUrl;
        }

        ResetPasswordUserInfo userInfo = context.candidateUser;
        model.addAttribute("userInfo", userInfo.getDataMapForDisplay());
        model.addAttribute("context", context);
        return "ucenter/resetpassword/chip/step4";
    }

    //找回密码-步骤5
    private String resetPwdStep5(Model model) {
        String contextToken = getRequestParameter("token", "");
        ResetPasswordContext context = getResetPasswordContext(contextToken);

        if (null != context) {
            ResetPasswordUserInfo userInfo = context.candidateUser;
            model.addAttribute("userInfo", userInfo.getDataMapForDisplay());
            model.addAttribute("context", context);
        }

        return "ucenter/resetpassword/chip/step5";
    }

    ///////////////////////////////////
    /////手机找回验证码相关代码
    //////////////////////////////////

    /**
     * 手机找回密码--发送验证码
     */
    @RequestMapping(value = "sendverificationcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendVerificationCode() {
        try {
            ResetPasswordContext context = getResetPasswordContext(getRequestParameter("token", ""));
            if (context == null) {
                return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");
            }

            String mobile = context.candidateUser.mobile;

            int expire = 120;
            if (context.mobileLastSendTime != 0) {
                int delta = (int) ((System.currentTimeMillis() - context.mobileLastSendTime) / 1000);
                if (expire - delta > 0) {
                    return MapMessage.errorMessage("验证码已发送，如未收到请2分钟后再试").add("timer", expire - delta);
                }
            }
            // 重置验证码输入次数
            context.verifyCodeTimes = 0;

            smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.FORGOT_PWD.name(), false);
            context.mobileLastSendTime = System.currentTimeMillis();
            saveResetPasswordContext(context);

            return MapMessage.successMessage("发送验证码成功").add("timer", expire);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("发送验证码失败");
    }

    /**
     * 手机找回密码--较验用户输入的验证码
     */
    @RequestMapping(value = "verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map verifyCode() {
        try {
            String token = getRequestParameter("token", "");
//
//            ResetPasswordContext context = getResetPasswordContext(token);
//            if (context == null) {
//                return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");
//            }
//
//            String code = getRequestParameter("code", "");
//            if (StringUtils.isEmpty(code)) {
//                return MapMessage.errorMessage("请输入验证码");
//            }
//
//            if (context.verifyCodeTimes > VERIFY_CODE_INPUT_TIMES_LIMIT) {// 需要限制验证码的输入次数，防止硬爆破
//                // 这里没有使用atomic integer，是因为实际需要做同步处理的是save context环节，考虑到
//                return MapMessage.errorMessage("达到该验证码提交次数上限，请重新获取短信验证码");
//            }
//
//            MapMessage message = smsServiceClient.verifyValidateCode(context.candidateUser.mobile, code, SmsType.FORGOT_PWD);
//            if (!message.isSuccess()) {
//                context.verifyCodeTimes++;
//                saveResetPasswordContext(context);
//                return message;
//            }

            MapMessage message = AtomicLockManager.instance().wrapAtomic(this)
                    .keyPrefix("ForgotPasswordController::verifyContextCode")
                    .keys(token)
                    .proxy()
                    .verifyContextCode(token);
            if (!message.isSuccess()) {
                return message;
            }
            ResetPasswordContext context = (ResetPasswordContext)message.get("context");

            context.isCurrentUserVerified = true;
            context.method = FindPasswordMethod.MOBILE;   // 记录是用手机找回密码

            saveResetPasswordContext(context);

            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("验证失败，请重新发送验证码。");
    }

    /**
     * 这里需要将获取context，并且增加context的验证次数放在一个原子操作中，防止验证码穷举爆破
     *
     * @param token
     * @return
     */
    public MapMessage verifyContextCode(String token) {
        ResetPasswordContext context = getResetPasswordContext(token);
        if (context == null) {
            return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");
        }

        String code = getRequestParameter("code", "");
        if (StringUtils.isEmpty(code)) {
            return MapMessage.errorMessage("请输入验证码");
        }

//        if (context.verifyCodeTimes >= VERIFY_CODE_INPUT_TIMES_LIMIT) {// 需要限制验证码的输入次数，防止硬爆破
//            // 这里没有使用atomic integer，是因为实际需要做同步处理的是save context环节，考虑到
//            return MapMessage.errorMessage("达到该验证码提交次数上限，请重新获取短信验证码");
//        }

        context.verifyCodeTimes++;
        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(context.candidateUser.mobile, code, SmsType.FORGOT_PWD.name());
        if (!message.isSuccess()) {
            saveResetPasswordContext(context);
            return message;
        }

        return MapMessage.successMessage().add("context", context);
    }

    ///////////////////////////////////
    /////Email找回相关
    //////////////////////////////////

    /**
     * Email找回密码--发送邮件
     */
    @RequestMapping(value = "sendverificationlink.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendVerificationLink() {
        try {
            ResetPasswordContext context = getResetPasswordContext(getRequestParameter("token", ""));

            Long userId = context.candidateUser.userId;
            int userType = context.candidateUser.userType;
            Long current = System.currentTimeMillis();
            Long expireTimestamp = current + 86400L * 1000L;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", context.candidateUser.userId);
            m.put("expire", expireTimestamp);
            m.put("email", context.candidateUser.email);
            m.put("userType", userType);
            String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
            if (defaultDesKey == null) {
                throw new ConfigurationException("No 'default_des_key' configured");
            }
            String encryptCode = DesUtils.encryptHexString(defaultDesKey, JsonUtils.toJson(m));

            String link = UrlUtils.buildUrlQuery(ProductConfig.getUcenterUrl()
                    + "/ucenter/resetpwdbyemail.vpage", MiscUtils.m("encryptCode", encryptCode));
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("userId", userId);
            content.put("name", context.candidateUser.realName);
            content.put("link", link);
            content.put("date", DateUtils.dateToString(new Date(current), DateUtils.FORMAT_SQL_DATETIME));
            content.put("expiredDate", DateUtils.dateToString(new Date(expireTimestamp), DateUtils.FORMAT_SQL_DATETIME));
            content.put("hotline", Constants.HOTLINE_SPACED);

            emailServiceClient.createTemplateEmail(EmailTemplate.password)
                    .to(context.candidateUser.email)
                    .subject("一起作业网(17zuoye.com)重新设置密码")
                    .content(content)
                    .send();


            return MapMessage.successMessage("发送邮件成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("发送邮件失败");
    }

    /**
     * Email找回密码--验证邮件
     */
    @RequestMapping(value = "resetpwdbyemail.vpage", method = RequestMethod.GET)
    public String emailResetPassword(Model model, HttpServletResponse response) throws Exception {
        String encryptCode = getRequestString("encryptCode");

        String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
        if (defaultDesKey == null) {
            throw new ConfigurationException("No 'default_des_key' configured");
        }
        try {
            String json = DesUtils.decryptHexString(defaultDesKey, encryptCode);
            Map<String, Object> m = JsonUtils.fromJson(json);

            Long userId = conversionService.convert(m.get("userId"), Long.class);
            Long expire = conversionService.convert(m.get("expire"), Long.class);
            String email = conversionService.convert(m.get("email"), String.class);
            Integer userType = conversionService.convert(m.get("userType"), Integer.class);

            if (userId == null || expire == null || email == null) {
                model.addAttribute("errmsg", "找回密码错误：用户或时间有错");
                return "/ucenter/resetpassword/resultbyemail";
            }
            if (expire < System.currentTimeMillis()) {
                model.addAttribute("errmsg", "找回密码错误：已过期");
                return "/ucenter/resetpassword/resultbyemail";
            }

            List<User> users = userLoaderClient.loadUserByToken(userId.toString());
            Optional<User> user = users.stream().filter(u -> u.getUserType().intValue() == userType.intValue()).findFirst();
            ResetPasswordUserInfo userInfo = null;
            if (user.isPresent()) {
                userInfo = fillResetPasswordInfo(user.get());
            }
            if (userInfo == null) {
                model.addAttribute("errmsg", "该用户不存在");
                return "/ucenter/resetpassword/resultbyemail";
            }

            ResetPasswordContext context = ResetPasswordContext.newForVerifiedUser(userInfo);
            context.method = FindPasswordMethod.EMAIL;           // 记录是用邮箱找回密码
            saveResetPasswordContext(context);

            model.addAttribute("step", "step4");
            model.addAttribute("token", context.token);

            String redirectUrl = "redirect:/ucenter/resetpwdstep.vpage?step=step4&token=" + context.token;
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            model.addAttribute("errmsg", "找回密码错误：用户或时间有错");
        }
        return "/ucenter/resetpassword/resultbyemail";
    }

    ///////////////////////////////////
    /////安全问题找回密码
    //////////////////////////////////

    /**
     * 找回密码--验证密保答案
     */
    @RequestMapping(value = "verifysecurityquestionanswer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map verifySecurityQuestion() {
        try {
            ResetPasswordContext context = getResetPasswordContext(getRequestParameter("token", ""));
            if (context == null) {
                return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");
            }

            String answer = getRequestParameter("answer", "");
            if (StringUtils.isEmpty(answer)) {
                return MapMessage.errorMessage("请输入密保问题答案");
            }

            if (!StringUtils.equals(context.answer, answer)) {
                return MapMessage.errorMessage("答案错误");
            }

            context.isCurrentUserVerified = true;
            context.method = FindPasswordMethod.SECURITY_QUESTION;   // 记录是用密保问题找回密码
            saveResetPasswordContext(context);

            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("验证失败，请重新回答");
    }

    ///////////////////////////////////
    /////帐号找回
    //////////////////////////////////

    /**
     * 账号找回入口
     */
    @RequestMapping(value = "forgotaccount.vpage", method = RequestMethod.GET)
    public String forgotAccount(Model model) {
        model.addAttribute("captchaToken", RandomUtils.randomString(24));

        if ("3，".equals(getRequestParameter("userType", ""))) {//FIXME:这是什么?
            logger.error("Invalid userType in forgotaccount");
        }

        return "ucenter/forgotaccount";
    }

    /**
     * 帐号找回--账号查询
     */
    @RequestMapping(value = "possibleaccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage retrivePossibleAccount(@RequestBody Map<String, Object> params) {
        if (MapUtils.isEmpty(params) || !params.containsKey("userType") || !params.containsKey("userName")
                || !params.containsKey("schoolId") || !params.containsKey("captchaToken") || !params.containsKey("captchaCode")) {
            return MapMessage.errorMessage("查询失败,参数错误");
        }

        try {
            int userTypeValue = ConversionUtils.toInt(params.get("userType"), 0);
            UserType userType = UserType.of(userTypeValue);
            if (userType != UserType.TEACHER && userType != UserType.STUDENT) {
                return MapMessage.errorMessage("该角色未开放找回账号功能");
            }

            String userName = params.get("userName").toString();
            Long schoolId = ConversionUtils.toLong(params.get("schoolId"), 0L);
            int clazzLevel = SafeConverter.toInt(params.get("clazzLevel"));
            String captchaToken = params.get("captchaToken").toString();
            String captchaCode = params.get("captchaCode").toString();

            if (StringUtils.isBlank(userName)) {
                return MapMessage.errorMessage("请输入名字");
            }
            if (!consumeCaptchaCode(captchaToken, captchaCode)) {
                return MapMessage.errorMessage("验证码输入错误，请重新输入。").set("value", "codeFalse");
            }

            switch (userType) {
                case TEACHER: {
                    return MapMessage.successMessage().add("accounts", fromalizeTeacherData(schoolId, userName));
                }
                case STUDENT: {
                    return MapMessage.successMessage().add("accounts", formalizeStudentData(schoolId, clazzLevel, userName));
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("查询失败");
    }

    private List<Map<String, Object>> fromalizeTeacherData(Long schoolId, String userName) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
        for (Teacher teacher : teachers) {
            if (StringUtils.equals(userName, teacher.fetchRealname())) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", teacher.getId());
                map.put("userName", teacher.fetchRealname());
                map.put("subject", teacher.getSubject().getValue());
                map.put("userType", UserType.TEACHER);
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
                if (ua != null) {
                    map.put("obscuredMobile", sensitiveUserDataServiceClient.loadUserMobileObscured(teacher.getId()));
                    map.put("obscuredEmail", sensitiveUserDataServiceClient.loadUserEmailObscured(teacher.getId()));
                }
                results.add(map);
            }
        }
        return results;
    }


    private List<Map<String, Object>> formalizeStudentData(Long schoolId, Integer classLevel, String userName) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> accounts = accountWebappService.findByStudentNameAndSchoolId(schoolId, classLevel, userName);
        for (Map<String, Object> each : accounts) {
            Long userId = conversionService.convert(each.get("userId"), Long.class);
            Integer clazzType = conversionService.convert(each.get("clazzType"), Integer.class);
            String jie = conversionService.convert(each.get("jie"), String.class);
            Integer clazzLevel = ClassJieHelper.toClazzLevel(SafeConverter.toInt(jie), (EduSystemType) each.get("eduSystem")).getLevel();
            String clazzName = conversionService.convert(each.get("clazzName"), String.class);
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("userName", userName);
            map.put("clazzName", formalizeClazzName(clazzLevel, clazzName, clazzType, jie));
            map.put("userType", UserType.STUDENT);
            results.add(map);
        }
        return results;
    }

    private String formalizeClazzName(Integer level, String clazzName, Integer clazzType, String jie) {
        if (ClazzType.of(clazzType) == ClazzType.PUBLIC) {
            ClazzLevel clazzLevel = ClazzLevel.of(level);
            if (clazzLevel == ClazzLevel.PRIMARY_GRADUATED || clazzLevel == ClazzLevel.MIDDLE_GRADUATED) {
                return jie + "级" + StringUtils.defaultString(clazzName);
            }
            if (clazzLevel == ClazzLevel.PRIVATE_GRADE) {
                return StringUtils.defaultString(clazzName);
            }
            return clazzLevel.getDescription() + StringUtils.defaultString(clazzName);
        } else {
            return StringUtils.defaultString(clazzName);
        }
    }

    ///////////////////////////////////
    /////辅助对象
    //////////////////////////////////

    //static private final int VERIFY_CODE_INPUT_TIMES_LIMIT = 5;

    static public class ResetPasswordContext implements Serializable {
        private static final long serialVersionUID = 7083730345247631573L;
        @Getter
        @Setter
        String token;
        ResetPasswordUserInfo candidateUser;
        boolean isCurrentUserVerified = false;
        long mobileLastSendTime;
        String mobileLastCode;
        FindPasswordMethod method;
        String question = "";
        String answer = "";
        String trackId = "";

        int verifyCodeTimes = 0;

        static ResetPasswordContext newForUsers(ResetPasswordUserInfo user) {
            ResetPasswordContext context = new ResetPasswordContext();
            context.token = RandomUtils.randomString(32);
            context.candidateUser = user;
            context.cleanCurrentUser();
            return context;
        }

        static ResetPasswordContext newForVerifiedUser(ResetPasswordUserInfo user) {
            ResetPasswordContext context = new ResetPasswordContext();
            context.token = RandomUtils.randomString(32);
            context.candidateUser = user;
            context.isCurrentUserVerified = true;
            return context;
        }

        void cleanCurrentUser() {
            isCurrentUserVerified = false;
        }

        MapMessage getDataMapForDisplay() {
            return MapMessage.successMessage()
                    .add("token", token)
                    .add("needSelect", false);
        }
    }

    static public class ResetPasswordUserInfo implements Serializable {
        private static final long serialVersionUID = -1240308716823261497L;
        @Getter
        @Setter
        Long userId;
        @Getter
        @Setter
        Integer userType;
        @Getter
        @Setter
        String realName = "";
        String email = "";
        String mobile = "";
        boolean securityQuestion = false;
        Map<String, String> questionAndAnswer = new HashMap<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResetPasswordUserInfo that = (ResetPasswordUserInfo) o;
            return userId.equals(that.userId);
        }

        @Override
        public int hashCode() {
            return userId.hashCode();
        }

        @JsonIgnore
        Map getDataMapForDisplay() {
            return MiscUtils.map()
                    .add("userId", userId)
                    .add("realName", realName)
                    .add("userType", userType)
                    .add("sqSetted", securityQuestion)
                    .add("obscuredMobile", StringUtils.mobileObscure(StringUtils.defaultString(mobile)))
                    .add("obscuredEmail", StringUtils.emailAddressObscure(StringUtils.defaultString(email)));
        }
    }
}
