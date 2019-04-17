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

package com.voxlearning.washington.controller.ucenter;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.footprint.client.ForgotPasswordDetailServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.washington.support.AbstractController;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 忘记账号密码
 *
 * @author RuiBao
 * @author Xiaoguang Wang
 * @version 0.1
 * @since 13-12-25
 */
@Controller
@RequestMapping("/ucenter")
public class ForgotPasswordAccountController extends AbstractController {
    private final static String ResetPasswordEntryUrl = "/ucenter/resetnavigation.vpage";
    private final static Set<UserType> allowRetrieveAccountUserTypes;

    static {
        allowRetrieveAccountUserTypes = new HashSet<>();
        allowRetrieveAccountUserTypes.add(UserType.TEACHER);
        allowRetrieveAccountUserTypes.add(UserType.STUDENT);
    }

    @Inject private RaikouSystem raikouSystem;

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private ForgotPasswordDetailServiceClient forgotPasswordDetailServiceClient;
    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    /**
     * 找回密码账号的统一入口
     */
    @RequestMapping(value = "resetnavigation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String forgotPasswordAndAccountIndex(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/ucenter/resetnavigation.vpage";
//        return resetpwdstep(model);
    }

    /* --------------------------------------- 我是统一入口和找回账号的分界线 -------------------------------------------*/

    /**
     * 找回账号入口
     */
    @RequestMapping(value = "forgotaccount.vpage", method = RequestMethod.GET)
    public String forgotAccount(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/ucenter/forgotaccount.vpage";
    }

    /* --------------------------------------- 我是找回账号和找回密码的分界线 -------------------------------------------*/

    /**
     * 找回密码的入口
     */
    @RequestMapping(value = "forgotPassword.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String forgotPassword(Model model) {
        return resetpwdstep(model);
    }

    /**
     * 重置密码步骤入口
     */
    @RequestMapping(value = "resetpwdstep.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String resetpwdstep(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/ucenter/resetpwdstep.vpage";
    }

    /**
     * 根据输入信息查询用户，初始化context
     */
    @RequestMapping(value = "resetpwdstart.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @Deprecated
    public Map forgetpassword() {
        String account = getRequestParameter("account", "");
        String captchaToken = getRequestParameter("captchaToken", "");
        String captchaCode = getRequestParameter("captchaCode", "");
        int userType = getRequestInt("userType");

        if (StringUtils.isBlank(account)) {
            return MapMessage.errorMessage("请正确输入账号信息。");
        }
        if (userType != 0 && !Arrays.asList(1, 2, 3).contains(userType)) {
            return MapMessage.errorMessage("请重新选择用户类型");
        }
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码输入错误，请重新输入。");
        }

        ResetPasswordUserInfo user;
        if (userType == 0) {
            List<ResetPasswordUserInfo> users = findCandidateUsers(account);
            if (users.size() > 1) {// 账号对于多个角色
                saveCaptchaCode(captchaToken, captchaCode);// 因为会选择角色进行第二次验证，所以在此添加同样的code重复调用
                return MapMessage.successMessage()
                        .add("needSelect", true)
                        .add("users", users.stream().map(ResetPasswordUserInfo::getUserType).collect(Collectors.toSet()));
            }
            user = MiscUtils.firstElement(users);
        } else {
            user = findCandidateUser(account, userType);
        }
        if (user == null) {
            return MapMessage.errorMessage();
        }

        fillResetPasswordUsersRelatedData(user);

        ResetPasswordContext context = ResetPasswordContext.newForUsers(user);    // 如果没有找到，context中的candidateUser是null
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

        ResetPasswordUserInfo user = findCandidateUser(account, userType);
        fillResetPasswordUsersRelatedData(user);

        ResetPasswordContext context = ResetPasswordContext.newForUsers(user);    // 如果没有找到，context中的candidateUser是null

        saveResetPasswordContext(context);
        return context.getDataMapForDisplay();
    }

    /**
     * 找回密码--发送验证码
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
     * 找回密码--验证码是否匹配
     */
    @RequestMapping(value = "verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map verifyCode() {
        try {
            ResetPasswordContext context = getResetPasswordContext(getRequestParameter("token", ""));
            if (context == null) {
                return MapMessage.errorMessage("当前操作已经失效。请重新进行找回密码的操作。");
            }

            String code = getRequestParameter("code", "");
            if (StringUtils.isEmpty(code)) {
                return MapMessage.errorMessage("请输入验证码");
            }

            MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(context.candidateUser.mobile, code, SmsType.FORGOT_PWD.name());
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
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
     * 找回密码--发送邮件
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

            String link = ProductConfig.getMainSiteBaseUrl() + "/ucenter/resetpassword/" + encryptCode + ".vpage";
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
     * 找回密码--验证邮件
     */
    @RequestMapping(value = "resetpassword/{encryptCode}.vpage", method = RequestMethod.GET)
    public void emailResetPassword(@PathVariable("encryptCode") String encryptCode, Model model, HttpServletResponse response) throws Exception {
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
                raiseErrorPage(response, 403, "找回密码错误：用户或时间有错");
                return;
            }
            if (expire < System.currentTimeMillis()) {
                raiseErrorPage(response, 403, "找回密码错误：已过期");
                return;
            }
            ResetPasswordUserInfo userInfo = findCandidateUser(userId.toString(), userType);
            if (userInfo == null) {
                raiseErrorPage(response, 403, "该用户不存在");
                return;
            }

            ResetPasswordContext context = ResetPasswordContext.newForVerifiedUser(userInfo);
            context.method = FindPasswordMethod.EMAIL;           // 记录是用邮箱找回密码
            saveResetPasswordContext(context);

            model.addAttribute("step", "step4");
            model.addAttribute("token", context.token);

            String redirectUrl = "redirect:/ucenter/resetpwdstep.vpage?step=step4&token=" + context.token;
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            // 有时候解密会出错，直接403了
            raiseErrorPage(response, 403, "找回密码错误：用户或时间有错");
        }
        //return "redirect:/ucenter/resetpwdstep.vpage";
    }

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

            saveForgotPwdServiceRecord(resetPasswordContext.candidateUser.getUserId(), FindPasswordMethod.ACCOUNT_PASSWORD, "refer:ForgotPasswordAccountController.processResetpwForm");

            destroyResetPasswordContext(resetPasswordContext);

            return MapMessage.successMessage("重置密码成功");
        }
        return MapMessage.successMessage("重置密码失败");
    }

    /**
     * 当点击客服找回密码的时候会鸟儿悄儿的的往这里发个请求记录一下
     */
    @RequestMapping(value = "forgotpwdcallcenter.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage callCenter() {
        ResetPasswordContext context = getResetPasswordContext(getRequestParameter("token", ""));
        if (null == context) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage();
    }


    /**
     * ******************************************* private method ***************************************************
     */

//    private String processResetPasswordStep(Model model, String currentStep, String contextToken, int userType) {
//        ResetPasswordContext context = getResetPasswordContext(contextToken);
//
//        // 如果不是第1,5步的时候，需要检查是否有有效的 context 。如果没有context，则重新进入重置密码流程
//        if (!"step1".equals(currentStep) && !"step5".equals(currentStep)) {
//            if (context == null || context.candidateUser == null) {
//                return "redirect:" + ResetPasswordEntryUrl;
//            }
//        }
//
//        // 进入第1步的时候，需要初始化captcha环境
//        if ("step1".equals(currentStep)) {
//            model.addAttribute("captchaToken", RandomUtils.randomString(24));
//        }
//
//        // 如果有context， 则把currentUser也暴露给ftl
//        if (context != null) {
//            ResetPasswordUserInfo userInfo = context.candidateUser;
//            model.addAttribute("userInfo", userInfo.getDataMapForDisplay());
//            model.addAttribute("context", context);
//            // 进入第3步的时候，如果用户选择的是密码找回，则随机选择三个问题中的一个，返给前端，并且把问题和答案存到context中
//            if ("step3_securityquestion".equals(currentStep)) {
//                List<String> questions = new ArrayList<>(userInfo.questionAndAnswer.keySet());
//                String question = questions.get(RandomUtils.nextInt(questions.size()));
//                String answer = userInfo.questionAndAnswer.get(question);
//                context.question = question;
//                context.answer = answer;
//                saveResetPasswordContext(context);
//                model.addAttribute("question", question);
//            }
//        }
//
//        return "ucenter/resetpassword/chip/" + currentStep;
//    }
    private void saveResetPasswordContext(ResetPasswordContext context) {
        washingtonCacheSystem.CBS.flushable.set("ResetPassword:" + context.token, 3600, context);
    }

    private void destroyResetPasswordContext(ResetPasswordContext context) {
        washingtonCacheSystem.CBS.flushable.delete("ResetPassword:" + context.token);
    }

    private ResetPasswordContext getResetPasswordContext(String token) {
        return washingtonCacheSystem.CBS.flushable.load("ResetPassword:" + token);
    }

    private ResetPasswordUserInfo findCandidateUser(String account, int userType) {
        return findCandidateUsers(account)
                .stream()
                .filter(e -> Objects.equals(e.userType, userType))
                .findFirst()
                .orElse(null);
    }

    private List<ResetPasswordUserInfo> findCandidateUsers(String account) {
        List<ResetPasswordUserInfo> resetPasswordUserInfos = new LinkedList<>();
        try {
            List<User> users = userLoaderClient.loadUserByToken(account);
            for (User candidate : users) {
                ResetPasswordUserInfo userInfo = new ResetPasswordUserInfo();
                userInfo.userId = candidate.getId();
                userInfo.realName = candidate.getProfile().getRealname();
                userInfo.userType = candidate.getUserType();
                resetPasswordUserInfos.add(userInfo);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resetPasswordUserInfos;
    }

    private void fillResetPasswordUsersRelatedData(ResetPasswordUserInfo user) {
        if (user != null) {
            UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.userId);
            String am = sensitiveUserDataServiceClient.showUserMobile(user.userId, "forgotpassword",SafeConverter.toString(user.userId));
            String ae = sensitiveUserDataServiceClient.loadUserEmail(user.userId, "forgotpassword");
            if (am != null) {
                user.mobile = am;
            }
            if (ae != null) {
                user.email = ae;
            }

            if (user.userType == UserType.STUDENT.getType()) {
                StudentParent sp = parentLoaderClient.loadStudentKeyParent(user.userId);
                User parent = sp == null ? null : sp.getParentUser();
                if (parent != null) {
                    //UserAuthentication pa = userLoaderClient.loadUserAuthentication(parent.getId());
                    am = sensitiveUserDataServiceClient.showUserMobile(parent.getId(), "forgotpassword", SafeConverter.toString(parent.getId()));
                    ae = sensitiveUserDataServiceClient.loadUserEmail(parent.getId(), "forgotpassword");
                    if (am != null) {
                        user.mobile = am;
                    }
                    if (ae != null) {
                        user.email = ae;
                    }
                }
            }
        }
    }

    private void saveForgotPwdServiceRecord(Long userId, FindPasswordMethod method, String addition) {
        if (userId == null) {
            return;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(userId.toString());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改密码");
        userServiceRecord.setComments(method.getDescription());
        if (StringUtils.isNotEmpty(addition)) {
            userServiceRecord.setAdditions(addition);
        }

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }

    /**
     * ********************************************** entity ********************************************************
     */

    static public class ResetPasswordUserInfo implements Serializable {
        private static final long serialVersionUID = -1240308716823261497L;
        @Getter @Setter Long userId;
        @Getter @Setter Integer userType;
        @Getter @Setter String realName = "";
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
                    .add("obscuredMobile", StringHelper.mobileObscure(StringUtils.defaultString(mobile)))
                    .add("obscuredEmail", StringHelper.emailAddressObscure(StringUtils.defaultString(email)));
        }
    }

    static public class ResetPasswordContext implements Serializable {
        private static final long serialVersionUID = 7083730345247631573L;
        @Getter @Setter String token;
        ResetPasswordUserInfo candidateUser;
        boolean isCurrentUserVerified = false;
        long mobileLastSendTime;
        String mobileLastCode;
        FindPasswordMethod method;
        String question = "";
        String answer = "";
        String trackId = "";

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

        Map getDataMapForDisplay() {
            return MapMessage.successMessage()
                    .add("token", token)
                    .add("needSelect", false);
        }
    }
}