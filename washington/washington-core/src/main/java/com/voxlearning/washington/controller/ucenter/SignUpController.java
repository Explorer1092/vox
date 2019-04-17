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

package com.voxlearning.washington.controller.ucenter;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.random.RandomGenerator;
import com.voxlearning.alps.lang.util.*;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.Cache;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.washington.mapper.UserMapper;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.WashingtonRequestContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

@Controller
@RequestMapping("/signup")
public class SignUpController extends AbstractController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private static Cache cache = CacheSystem.CBS.getCache("storage");

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    @RequestMapping(value = "sendpwmessage.vpage", method = {RequestMethod.GET})
    public String sendPwMessage(@RequestParam(value = "mobile", defaultValue = "", required = false) String mobile, Model model) {
        User user = currentUser();
        if (user == null) {
            return "redirect:/login.vpage";
        }
        model.addAttribute("mobile", mobile);
//        model.addAttribute("message", "恭喜您加入一起作业！您的账号：" + user.getId() + "，密码：" + user.getRealCode() + "，请妥善保管账号信息。现在就登录www.17zuoye.com开始使用吧！");
        String context = "恭喜您加入一起作业！请用此手机号作为账号登录http://www.17zuoye.com 使用！";
        model.addAttribute("message", context);
        return "ucenter/signup/sendpwmessage";
    }


    /**
     * ------------------------------------------ 新版注册流程  2014年4月10日 --------------------------------------------
     */

    /**
     * step1 - 选择角色
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String signupIndex(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/signupchip/index.vpage";
    }

    /**
     * step2 - 根据角色不同进入注册表单页
     */
    @RequestMapping(value = "htmlchip/{page}.vpage", method = RequestMethod.GET)
    public String steps(@PathVariable("page") String page, Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/signupchip/htmlchip/" + page + ".vpage";
    }


    /**
     * --------------------------------------------- 手机注册发送验证码 --------------------------------------------------
     */

    private boolean verifyContext() {
        String contextId = getRequest().getParameter("cid");
        String ctxIp = washingtonCacheSystem.CBS.unflushable.load("VrfCtxIp_" + contextId);
        return !StringUtils.isEmpty(ctxIp);
    }

    /**
     * 教师手机注册，发送验证码
     */
    @RequestMapping(value = "tmsignsvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherMobileSignupSendVerificationCode() {

        String mobile = getRequest().getParameter("mobile");

        // 验证码部分begin
        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");

        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码输入错误，请重新输入!");
        }
        // 验证码部分end

        boolean voice = getRequestBool("voice");
        if (!verifyContext()) {
            return MapMessage.successMessage("  发验证码成功  ");  //返回一个假消息
        }
        int count = getRequestInt("count");

        String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
        if (StringUtils.isNoneBlank(pickUpLog) && "2".equals(pickUpLog)) {
            logger.info("sendRegisterVerifyCode invoked from {}, {}, {}", "WSD SignUpController", mobile, getWebRequestContext().getRealRemoteAddress());
        }
        MapMessage message = getSmsServiceHelper().sendUnbindMobileVerificationCode(mobile,
                SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE, UserType.TEACHER, voice);
        if (message.isSuccess() && count == 2) {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
            }
//            if (!feedbackServiceClient.getFeedbackService().feedback(mobile, RegisterFeedbackCategory.SUBMIT_TWICE).isSuccess()) {
//                return MapMessage.errorMessage();
//            }
        }
        return message;
    }

    /**
     * 学生手机注册，发送验证码
     */
    @RequestMapping(value = "smsignsvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentMobileSignupSendVerificationCode() {
        String mobile = getRequest().getParameter("mobile");
        if (!verifyContext()) {
            return MapMessage.successMessage("  发验证码成功  ");     //返回一个假消息
        }

        return getSmsServiceHelper().sendUnbindMobileVerificationCode(
                mobile,
                SmsType.STUDENT_VERIFY_MOBILE_REGISTER_MOBILE,
                UserType.STUDENT,
                false
        );
    }

    /**
     * 家长手机注册，发送验证码
     */
    @RequestMapping(value = "pmsignsvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentMobileSignupSendVerificationCode() {
        String mobile = getRequest().getParameter("mobile");
        if (!verifyContext()) {
            return MapMessage.successMessage("  发验证码成功  ");  //返回一个假消息
        }
        return getSmsServiceHelper().sendUnbindMobileVerificationCode(
                mobile,
                SmsType.PARENT_VERIFY_MOBILE_REGISTER_MOBILE,
                UserType.PARENT,
                false
        );
    }

    /**
     * ---------------------------------------------- 教师家长手机注册 --------------------------------------------------
     */

    /**
     * 教师家长手机注册
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    @RequestMapping(value = "msignup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processMobileSignupForm(@RequestBody UserMapper command) {
        String code = command.getCode();
        String mobile = command.getMobile();
        String realname = command.getRealname();
        String password = command.getPassword();
        RoleType roleType = RoleType.valueOf(command.getRole());
        UserType userType = UserType.of(command.getUserType());

        if (StringUtils.isBlank(code) || StringUtils.isBlank(mobile) || StringUtils.isBlank(realname) || StringUtils.isBlank(password)) {
            return MapMessage.errorMessage("信息不全");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        if (roleType != RoleType.ROLE_PARENT && roleType != RoleType.ROLE_TEACHER) {
            return MapMessage.errorMessage("用户角色错误");
        }
        if (userType != UserType.PARENT && userType != UserType.TEACHER) {
            return MapMessage.errorMessage("用户类型错误");
        }

        boolean whosyourdaddy = (StringUtils.equals("演示", realname) || StringUtils.equals("测试", realname)) && StringUtils.equals("123456", code);
        SmsType smsType;
        if (userType == UserType.TEACHER) {
            smsType = SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE;
        } else {
            smsType = SmsType.PARENT_VERIFY_MOBILE_REGISTER_MOBILE;
        }

        if (!whosyourdaddy) {
            MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
            if (!validateResult.isSuccess()) {
                return validateResult;
            }
        } else {
            if (userLoaderClient.loadMobileAuthentication(mobile, userType) != null) {
                return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
            }
        }

        // 创建用户
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(roleType);
        neonatalUser.setUserType(userType);
        neonatalUser.setMobile(command.getMobile());
        neonatalUser.setPassword(command.getPassword());
        neonatalUser.setRealname(command.getRealname());
        neonatalUser.setInviter(command.getInviteInfo());
        neonatalUser.setInvitationType(command.getInvitationType());
        if (!whosyourdaddy) {
            neonatalUser.setWebSource(UserWebSource.web_self_reg.getSource());
        }

        try {
            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                MapMessage result = MapMessage.errorMessage();
                result.getAttributes().putAll(message.getAttributes());
                return result;
            }
            User user = (User) message.get("user");
            Long userId = user.getId();
            if (!whosyourdaddy) {
                //注册的时候同步shippingaddress 电话号码信息
                MapMessage msg = userServiceClient.activateUserMobile(userId, mobile, true);
                if (!msg.isSuccess()) {
                    MapMessage result = MapMessage.errorMessage();
                    result.getAttributes().put("none", "绑定用户手机失败");
                    return result;
                }
            }

            //刚注册的登录，登录状态关闭浏览器就消除了，强制用户下次登录的时候再输入一次密码，强化记忆
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(userId);
            getWebRequestContext().saveAuthenticationStates(-1, userId, ua.getPassword(), roleType);

            try {
                // 注册成功后，立刻记录用户的登录记录
//                userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddr(), OperationSourceType.pc);
//                userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.pc);
            } catch (Exception e) {
                logger.error("after register, record user login failed: " + e.getMessage(), e);
            }

            // 判断是否要做第三方帐号绑定
            checkAccountBind(command, userId);

            //如果是短信邀请进来的，默认绑定手机号
            miscServiceClient.bindInvitedTeacherMobile(userId);

            // #11782 老师注册成功发下一步指导短信
            if (userType == UserType.TEACHER && !whosyourdaddy) {
                smsServiceClient.createSmsMessage(command.getMobile())
                        .content("注册成功！用手机号和密码即可登录一起作业网辅助教学。学生注册时填写你的手机号即可加入班级，完成作业。")
                        .type(SmsType.TEACHER_GUIDE_AFTER_REG.name())
                        .send();

                smsServiceClient.createSmsMessage(command.getMobile())
                        .content("[转发家长]本班拟用教 育 部课题平台，请下载手机一起作业www.17zyw.cn/YRFfA3 输入" + command.getMobile() + "注册学生号")
                        .type(SmsType.TEACHER_GUIDE_AFTER_REG.name())
                        .send();
            }

            return MapMessage.successMessage("创建用户成功").add("row", userId);
        } catch (Exception ex) {
            MapMessage message = MapMessage.errorMessage();
            message.getAttributes().put("none", "创建用户失败");
            return message;
        }
    }

    /**
     * --------------------------------------------- 邮件注册发送验邮件 --------------------------------------------------
     */

    /**
     * 教师邮箱注册，发送激活链接
     */
    @RequestMapping(value = "esignsvl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherEmailSinupSendVerificationLink(@RequestBody UserMapper command) {
        try {
            String email = command.getEmail();
            String realname = command.getRealname();
            String password = command.getPassword();
            RoleType roleType = RoleType.valueOf(command.getRole());
            UserType userType = UserType.of(command.getUserType());

            if (StringUtils.isBlank(email) || StringUtils.isBlank(realname) || StringUtils.isBlank(password)) {
                return MapMessage.errorMessage("信息不全");
            }
            if (!EmailRule.isEmail(email)) {
                return MapMessage.errorMessage("请填写正确的邮箱");
            }
            if (userLoaderClient.loadEmailAuthentication(email) != null) {
                return MapMessage.errorMessage("该邮箱已经注册，请直接登录");
            }
            if (roleType != RoleType.ROLE_PARENT && roleType != RoleType.ROLE_TEACHER) {
                return MapMessage.errorMessage("用户角色错误");
            }
            if (userType != UserType.PARENT && userType != UserType.TEACHER) {
                return MapMessage.errorMessage("用户类型错误");
            }

            // 生成邀请地址
            Map<String, Object> registerInfo = new LinkedHashMap<>();
            registerInfo.put("email", email);
            registerInfo.put("realname", realname);
            registerInfo.put("password", password);
            registerInfo.put("roleType", roleType);
            registerInfo.put("userType", userType);
            registerInfo.put("expiredTimestamp", System.currentTimeMillis() + 86400000L);
            if (StringUtils.isNotBlank(command.getInviteInfo())) {
                registerInfo.put("inviteInfo", command.getInviteInfo());
            }
            if (null != command.getInvitationType()) {
                registerInfo.put("invitationType", command.getInvitationType());
            }
            String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
            if (defaultDesKey == null) {
                throw new ConfigurationException("No 'default_des_key' configured");
            }
            String link = ProductConfig.getMainSiteBaseUrl() + "/signup/tesignactivation/"
                    + DesUtils.encryptHexString(defaultDesKey, JsonUtils.toJson(registerInfo)) + ".vpage";


            // 发送邮件
            String subject = "请激活你的一起作业账号，完成注册";
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("url", link);
            content.put("name", realname);
            content.put("date", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 hh:mm"));

            emailServiceClient.createTemplateEmail(EmailTemplate.emailregister)
                    .to(command.getEmail())
                    .subject(subject)
                    .content(content)
                    .send();

            return MapMessage.successMessage().add("email", email);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * --------------------------------------------- 邮件注册发送验邮件 --------------------------------------------------
     */

    @RequestMapping(value = "sendemailsuccess.vpage", method = RequestMethod.GET)
    public String emailSendSuccess(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/signup/sendemailsuccess.vpage";
    }

    /**
     * 教师家长邮箱注册，点击激活链接
     */
    @RequestMapping(value = "tesignactivation/{url}.vpage", method = RequestMethod.GET)
    public String processMobileSignupLink(@PathVariable("url") String url) {
        String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
        if (defaultDesKey == null) {
            throw new ConfigurationException("No 'default_des_key' configured");
        }
        try {
            String decryptCode = DesUtils.decryptHexString(defaultDesKey, url);
            Map<String, Object> registerInfo = JsonUtils.fromJson(decryptCode);
            String email = ConversionUtils.toString(registerInfo.get("email"));
            String realname = ConversionUtils.toString(registerInfo.get("realname"));
            String password = ConversionUtils.toString(registerInfo.get("password"));
            long expiredTimestamp = ConversionUtils.toLong(registerInfo.get("expiredTimestamp"));
            String inviteInfo = ConversionUtils.toString(registerInfo.get("inviteInfo"));
            InvitationType invitationType = conversionService.convert(registerInfo.get("invitationType"), InvitationType.class);
            RoleType roleType = conversionService.convert(registerInfo.get("roleType"), RoleType.class);
            UserType userType = conversionService.convert(registerInfo.get("userType"), UserType.class);

            if (StringUtils.isBlank(email) || StringUtils.isBlank(realname) || StringUtils.isBlank(password)) {
                return "redirect:/login.vpage";
            }
            if (new Date().after(new Date(expiredTimestamp))) {
                return "redirect:/login.vpage";
            }
            if (null != userLoaderClient.loadEmailAuthentication(email)) {
                return "redirect:/login.vpage";
            }
            if (roleType != RoleType.ROLE_PARENT && roleType != RoleType.ROLE_TEACHER) {
                return "redirect:/login.vpage";
            }
            if (userType != UserType.PARENT && userType != UserType.TEACHER) {
                return "redirect:/login.vpage";
            }

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(roleType);
            neonatalUser.setUserType(userType);
            neonatalUser.setPassword(password);
            neonatalUser.setRealname(realname);
            neonatalUser.setEmail(email);
            neonatalUser.setInviter(inviteInfo);
            neonatalUser.setInvitationType(invitationType);
            neonatalUser.setWebSource(UserWebSource.email.getSource());

            MapMessage message;
            try {
                message = atomicLockManager.wrapAtomic(userServiceClient)
                        .keys(email)
                        .proxy()
                        .registerUser(neonatalUser);
            } catch (DuplicatedOperationException e) {
                return "redirect:/login.vpage";
            }
            if (!message.isSuccess()) {
                logger.error("链接注册失败");
                return "redirect:/login.vpage";
            } else {
                if (message.containsKey("sendMessage")) {
                    List sendMessage = (List) message.get("sendMessage");
                    for (Object obj : sendMessage) {
                        Map each = (Map) obj;
                        Long receiver = (Long) each.get("receiver");
                        String content = (String) each.get("content");
                        User receiverUser = new User();
                        receiverUser.setId(receiver);
                        teacherLoaderClient.sendTeacherMessage(receiverUser.getId(), content);
                    }
                }
            }
            User user = (User) message.get("user");
            Long userId = user.getId();
            if (!userServiceClient.activateUserEmail(userId, email).isSuccess()) {
                logger.error("链接注册：绑定用户手机失败 [userId:{},email:{}]", userId, email);
                return "redirect:/login.vpage";
            }

            //刚注册的登录，登录状态关闭浏览器就消除了，强制用户下次登录的时候再输入一次密码，强化记忆
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(userId);
            getWebRequestContext().saveAuthenticationStates(-1, userId, ua.getPassword(), roleType);

            try {
                // 注册成功后，立刻记录用户的登录记录
//                userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.pc);
//                userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.pc);
            } catch (Exception e) {
                logger.error("after register, record user login failed: " + e.getMessage(), e);
            }

            //如果是短信邀请进来的，默认绑定手机号
            miscServiceClient.bindInvitedTeacherMobile(userId);

            if (userType == UserType.TEACHER) {
                return "redirect:/teacher/index.vpage";
            } else {
                return "redirect:/parent/regsucc.vpage";
            }
        } catch (Exception ex) {
            logger.error("链接注册失败" + ex.getMessage(), ex);
            return "redirect:/login.vpage";
        }
    }

    @Deprecated //已拆到用户中心2015-12-11
    @RequestMapping(value = "checkclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findClazzInfo() {
        Long id = getRequestLong("id");
        if (0 == id) {
            return MapMessage.errorMessage("请输入老师给你的号。");
        }
        MapMessage mapMessage = businessStudentServiceClient
                .joinClazz_findClazzInfo(id, Collections.singleton(Ktwelve.PRIMARY_SCHOOL));
        if (mapMessage.isSuccess() && mapMessage.get("teacher") == null) {
            return MapMessage.errorMessage("老师号错误。");
        }
        return mapMessage;
    }

    /**
     * Sign up user from specified user mapper information.
     *
     * @param command User mapper information of neotatal user.
     * @return Sign up result in Message format.
     */
    @Deprecated //已拆到用户中心2015-12-11
    @SuppressWarnings({"unchecked", "deprecation"})
    @RequestMapping(value = "signup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map processSignupForm(@RequestBody UserMapper command) {
        logger.debug("Signing up user...");

        RoleType roleType = RoleType.valueOf(command.getRole());
        UserType userType = UserType.of(command.getUserType());

        if (roleType != RoleType.ROLE_PARENT && roleType != RoleType.ROLE_TEACHER && roleType != RoleType.ROLE_STUDENT) {
            throw new RuntimeException("bad role type");
        }

        if (userType != UserType.PARENT && userType != UserType.TEACHER && userType != UserType.STUDENT) {
            throw new RuntimeException("bad user type");
        }

        // 判断班级是否允许学生自由加入
        Long clazzId = StringUtils.isBlank(command.getClazzId()) ? null : Long.parseLong(command.getClazzId().trim());
        long teacherId = SafeConverter.toLong(command.getTeacherId());
        GroupMapper group = null;
        if (clazzId != null) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            boolean flag;// 标志是否允许学生加入
            if (clazz != null) {
                if (!clazz.isSystemClazz()) {
                    flag = clazz.getFreeJoin();
                } else {
                    if (command.getTeacherId().length() == 11) {// 老师有可能是手机号注册
                        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(String.valueOf(teacherId), UserType.TEACHER);
                        if (ua == null) {
                            logger.error("teacher cannot be found by mobile {}", teacherId);
                            return MapMessage.errorMessage("该手机号找不到老师，请核实手机号是否正确");
                        }
                        teacherId = ua.getId();
                    }
                    group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
                    if (group != null) {
                        flag = group.getFreeJoin();
                    } else {
                        flag = false;
                    }
                }
            } else {
                flag = false;
            }
            if (!flag) {
                MapMessage retMessage = MapMessage.errorMessage();
                retMessage.getAttributes().put("clazzId", "老师不允许新学生加入此班级，有问题请与老师联系。");
                return retMessage;
            }
            //新加入逻辑  判断班级内重名的学生
            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazzId);
            if (CollectionUtils.isNotEmpty(studentIds)) {
                List<User> sameNameList = userLoaderClient.loadUsers(studentIds)
                        .values()
                        .stream()
                        .filter(s -> StringUtils.equals(s.fetchRealname(), command.getRealname().trim()))
                        .sorted((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sameNameList)) {
                    // redmine 13449
                    // TODO 暂时把逻辑放这里把，必须找时间重构了，太乱了。。。
                    // 找到第一个未登录的重名学生
                    User user = userLoginServiceClient.getUserLoginService().findSameNameNeverLoginUser(sameNameList).getUninterruptibly();
                    if (user != null) {
                        Long userId = user.getId();
                        // 为该学生使用这一重名账号
                        userServiceClient.setPassword(user, command.getPassword());
                        if (StringUtils.isNoneBlank(command.getMobile())) {
                            userServiceClient.activateUserMobile(userId, command.getMobile());
                        }
                        user = raikouSystem.loadUser(userId);
                        // 导入学生
                        clazzServiceClient.importStudent(teacherId, clazzId, userId);
                        if (!StringUtils.equals(command.getWebSource(), UserConstants.WEB_SOURCE_P_S_QQ_WECHAT)) {
                            //刚注册的登录，登录状态关闭浏览器就消除了，强制用户下次登录的时候再输入一次密码，强化记忆
                            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                            getWebRequestContext().saveAuthenticationStates(-1, userId, ua.getPassword(), roleType);

                            try {
                                // 注册成功后，立刻记录用户的登录记录
//                                userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddr(), OperationSourceType.pc);
//                                userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.pc);
                            } catch (Exception e) {
                                logger.error("after register, record user login failed: " + e.getMessage(), e);
                            }

                            checkAccountBind(command, userId);

                            //如果是短信邀请进来的，默认绑定手机号
                            miscServiceClient.bindInvitedTeacherMobile(userId);
                        }
                        return MapMessage.successMessage("创建用户成功").add("row", userId);
                    }
                    MapMessage retMessage = MapMessage.errorMessage();
                    retMessage.getAttributes().put("userId", MiscUtils.firstElement(sameNameList).getId());
                    return retMessage;
                }
            }
        }

        try {
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(roleType);
            neonatalUser.setUserType(userType);
            neonatalUser.setEmail(command.getEmail());
            neonatalUser.setMobile(command.getMobile());
            neonatalUser.setPassword(command.getPassword());
            neonatalUser.setRealname(command.getRealname());
            neonatalUser.setInviter(command.getInviteInfo());
            neonatalUser.setInvitationType(command.getInvitationType());
            neonatalUser.setCode(command.getCode());
            if (teacherId != 0) {
                neonatalUser.setTeacherId(teacherId);
            }
            neonatalUser.setClazzId(StringUtils.isBlank(command.getClazzId()) ? null : Long.parseLong(command.getClazzId().trim()));
            neonatalUser.setWebSource(StringUtils.isBlank(command.getWebSource()) ? UserWebSource.web_self_reg.getSource() : command.getWebSource().trim());

            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);

            if (!message.isSuccess()) {
                MapMessage result = MapMessage.errorMessage();
                result.getAttributes().putAll(message.getAttributes());
                return result;
            }
            User user = (User) message.get("user");
            Long userId = user.getId();

            if (!StringUtils.equals(command.getWebSource(), UserConstants.WEB_SOURCE_P_S_QQ_WECHAT)) {
                //刚注册的登录，登录状态关闭浏览器就消除了，强制用户下次登录的时候再输入一次密码，强化记忆
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(userId);
                getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);

                try {
                    // 注册成功后，立刻记录用户的登录记录
                    asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.pc);
                } catch (Exception e) {
                    logger.error("after register, record user login failed: " + e.getMessage(), e);
                }

                checkAccountBind(command, userId);

                //如果是短信邀请进来的，默认绑定手机号
                miscServiceClient.bindInvitedTeacherMobile(userId);
            }
            return MapMessage.successMessage("创建用户成功").add("row", userId);
        } catch (Exception ex) {
            MapMessage message = MapMessage.errorMessage();
            message.getAttributes().put("none", "创建用户失败。注册信息（如邮箱等）填写错了？");
            return message;
        }
    }

    /**
     * 家长帮学生注册，判断班级中是否存在名字和密码都相同的人
     */
    @RequestMapping(value = "validatenp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkSameName() {
        Long clazzId = getRequestLong("clazzId");
        String name = getRequestString("name");
        String pwd = getRequestString("pwd");

        List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzId(clazzId);
        for (User student : userLoaderClient.loadUsers(studentIds).values()) {
            if (StringUtils.equals(student.fetchRealname(), name)) {
                return MapMessage.successMessage().add("id", student.getId());
            }
        }
        return MapMessage.errorMessage(); // 没有找到，可以执行注册
    }

    @RequestMapping(value = "feedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage feedback(@RequestParam String mobile) {
        if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
            return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
        }
        // FIXME 这个地方已经没人看了。。。
        return MapMessage.successMessage("操作成功");
//        MapMessage message = feedbackServiceClient.getFeedbackService().feedback(mobile, RegisterFeedbackCategory.CALL_ME);
//        if (message.isSuccess()) {
//            return MapMessage.successMessage("操作成功");
//        } else {
//            logger.error("feedback failed,[mobile:{}],msg:{}", mobile, message.getInfo());
//            return MapMessage.errorMessage("操作失败，请重新获取短信验证码");
//        }
    }

    /**
     * 手机端支持老师注册 *
     */
    @RequestMapping(value = "mobile/register.vpage", method = RequestMethod.GET)
    public String mobileRegister(Model model) {
        // 生成一个 contextId 用于防止机器人刷接口
        String contextId = RandomUtils.randomString(10);
        washingtonCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
        model.addAttribute("contextId", contextId);

        return "mobile/pc/register";
    }

    /*移动端公共学生注册页面*/
    @RequestMapping(value = "mobile/publicstudentreg.vpage", method = RequestMethod.GET)
    public String mobilePublicStudentRegister(Model model) {
        // 生成一个 contextId 用于防止机器人刷接口
        String contextId = RandomUtils.randomString(10);
        washingtonCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
        model.addAttribute("contextId", contextId);

        return "mobile/pc/public_student_register";
    }

    /*移动端公共入口页*/
    @RequestMapping(value = "mobile/index.vpage", method = RequestMethod.GET)
    public String mobileIndex() {
        return "project/mobilegoin/index";
    }

    /**
     * 教师手机端注册，发送验证码
     */
    @RequestMapping(value = "tmsignsvc-m.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherMobileSignupSendVerificationCode_Mobile() {
        String mobile = getRequest().getParameter("mobile");
        if (!verifyContext()) {
            return MapMessage.successMessage("  发验证码成功  ");     //返回一个假消息
        }
        return getSmsServiceHelper().sendUnbindMobileVerificationCode(mobile, SmsType.TEACHER_REG_BY_MOBILE_SEND_CODE, UserType.TEACHER, false);
    }

    @RequestMapping(value = "mobile/teacherregister.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherMobileSignup() {
        String userName = StringHelper.filterEmojiForMysql(getRequestParameter("userName", ""));
        String mobile = getRequestParameter("mobile", "");
        String province = getRequestParameter("province", "");
        String code = getRequestParameter("code", "");

        try {
            if (StringUtils.isBlank(province)) {
                return MapMessage.errorMessage("省份不能为空");
            }
            if (StringUtils.isBlank(userName)) {
                return MapMessage.errorMessage("名称不能为空");
            }
            if (StringUtils.isBlank(mobile)) {
                return MapMessage.errorMessage("手机不能为空");
            }
            if (StringUtils.isBlank(code)) {
                return MapMessage.errorMessage("短信验证码不能为空");
            }

            boolean whosyourdaddy = (StringUtils.equals("演示", userName) || StringUtils.equals("测试", userName)) && StringUtils.equals("123456", code);
            if (!whosyourdaddy) {
                MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.TEACHER_REG_BY_MOBILE_SEND_CODE.name());
                if (!validateResult.isSuccess()) {
                    return validateResult;
                }
            } else {
                if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                    return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
                }
            }

            return businessTeacherServiceClient.teacherRegisterByMobile(mobile, userName, province);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(StringUtils.join("注册失败", e));
        }
    }

    private void checkAccountBind(UserMapper command, Long userId) {
        // 判断是否要做第三方帐号绑定
        String dataKey = command.getDataKey();
        if (StringUtils.isBlank(dataKey)) {
            return;
        }

        CacheObject<Map> cacheObject = washingtonCacheSystem.CBS.unflushable.get(dataKey);
        if (cacheObject == null) {
            // failed to access couchbase server, ignore
            return;
        }

        Map map = cacheObject.getValue();
        if (map != null) {
            String sourceName = String.valueOf(map.get("source"));
            String sourceUid = String.valueOf(map.get("sourceUid"));
            String sourceUserName = String.valueOf(map.get("userName"));
            try {
                thirdPartyService.persistLandingSource(sourceName, sourceUid, sourceUserName, userId);
            } catch (Exception ignored) {
            }
            washingtonCacheSystem.CBS.unflushable.delete(dataKey);
        }
    }


    /*********************************************薯条英语分享注册***************************/

    /**
     * 判断当前用户是否存在 是否是家长号
     *
     * @return
     */
    @RequestMapping(value = "chips/validatelogin.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage chipsValidateLogin() {
        User user = currentUser();
        if (null == user || !user.isParent()) {
            // 返回错误+CID
            // 生成一个 contextId 用于防止机器人刷接口
            return MapMessage.errorMessage().add("cid", generateContextIdForChips(getWebRequestContext()));
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "chips/sendsmscode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chipsSendSmsCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");

        try {
            if (!com.voxlearning.alps.lang.util.MobileRule.isMobile(mobile) || StringUtils.isBlank(contextId)) {
                return MapMessage.errorMessage("参数错误");
            }

            if (!verifyContextForChips()) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }

            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_CHIPS_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            logger.error("Send sms code for chips login failed,mobile:{},contextId:{}", mobile, contextId, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }

    @RequestMapping(value = "chips/verifiedlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chipsVerifiedLoginPost() {
        String mobileNumber = getRequestString("mobile");
        String verifyCode = getRequestString("code");

        if (!com.voxlearning.alps.core.util.MobileRule.isMobile(mobileNumber) || StringUtils.isBlank(verifyCode)) {
            return MapMessage.errorMessage("请输入正确的手机号与验证码");
        }

        try {
            // 验证短信验证码
            MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(mobileNumber, verifyCode, SmsType.PARENT_VERIFY_MOBILE_CHIPS_REGISTER.name());
            if (!message.isSuccess()) {
                return message;
            }

            // 检查手机号是否已经认证
            UserAuthentication userAus = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
            Long userId;
            if (null == userAus) {
                // 没有手机号 检查手机号是否已经注册
                List<UserAuthentication> aus = userLoaderClient.loadMobileAuthentications(mobileNumber);
                if (CollectionUtils.isNotEmpty(aus)) {
                    return MapMessage.errorMessage("手机号已注册其他角色账号");
                }
                // 注册一个
                NeonatalUser neonatalUser = new NeonatalUser();
                neonatalUser.setRoleType(RoleType.ROLE_PARENT);
                neonatalUser.setUserType(UserType.PARENT);
                neonatalUser.setMobile(mobileNumber);
                neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
                message = userServiceClient.registerUserAndSendMessage(neonatalUser);
                if (!message.isSuccess()) {
                    return message;
                }

                User parent = (User) message.get("user");
                message = userServiceClient.activateUserMobile(parent.getId(), mobileNumber);
                if (!message.isSuccess()) {
                    return message; //绑手机失败
                }
                userId = parent.getId();
            } else {
                userId = userAus.getId();
            }

            User parent = raikouSystem.loadUser(userId);
            if (null != parent) {
                //设置登录cookie
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
                getWebRequestContext().saveAuthenticationStates(-1, parent.getId(), ua.getPassword(), RoleType.ROLE_PARENT);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("没有家长帐号");
            }
        } catch (Exception ex) {
            logger.error("Verified Login Failed, mobile:{},code:{}", mobileNumber, verifyCode, ex);
            return MapMessage.errorMessage("登录失败");
        }
    }

    private String generateContextIdForChips(WashingtonRequestContext context) {
        String contextId = RandomUtils.randomString(10);
        Boolean ret = washingtonCacheSystem.CBS.unflushable.set("VrfCtxWx_" + contextId, 10 * 60, context.getRealRemoteAddress());
        if (!ret) {
            throw new IllegalStateException("create contextId error.");
        }
        return contextId;
    }

    private boolean verifyContextForChips() {
        String contextId = getRequest().getParameter("cid");
        String ctxIp = washingtonCacheSystem.CBS.unflushable.load("VrfCtxWx_" + contextId);
        return !StringUtils.isEmpty(ctxIp);
    }

}
