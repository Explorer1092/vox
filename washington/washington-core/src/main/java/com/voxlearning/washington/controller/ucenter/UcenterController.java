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

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.CommunityApplication;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.ArgMapper;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.ApplicationAuthorization;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.footprint.client.ForgotPasswordDetailServiceClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserEmailServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.mapper.UserSignSMSMapper;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.SessionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@Controller
@RequestMapping("/ucenter")
public class UcenterController extends AbstractController {
    private CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private ForgotPasswordDetailServiceClient forgotPasswordDetailServiceClient;
    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;
    @Inject private UserEmailServiceClient userEmailServiceClient;
    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    @RequestMapping(value = "home.vpage", method = RequestMethod.GET)
    public String home(@RequestParam(value = "redirectindex", required = false) String redirectindex, @RequestParam(value = "redirecturl", required = false) String redirecturl) {
        User user = currentUser();
        UserType userType = user == null ? UserType.ANONYMOUS : user.fetchUserType();
        String baseUrl = "/login.vpage";
        switch (userType) {
            case TEACHER: {
                baseUrl = "/teacher/index.vpage";
                break;
            }
            case PARENT: {
                baseUrl = "/parent/index.vpage";
                break;
            }
            case STUDENT: {
                baseUrl = "/student/index.vpage";
                break;
            }
            case EMPLOYEE: {
                Set<RoleType> roleTypes = userLoaderClient.loadUserRoles(user);
                if (roleTypes.contains(RoleType.ROLE_MARKETER)) {
                    baseUrl = "/ucenter/authorize/marketing.vpage";
                } else {
                    baseUrl = "/ucenter/authorize/admin.vpage";
                }
                break;
            }
            case RESEARCH_STAFF: {
                //baseUrl = "/ucenter/authorize/researchstaff.vpage";
                baseUrl = "/rstaff/index.vpage";
                break;
            }
            case TEMPORARY: {
                baseUrl = "/ucenter/authorize/admin.vpage";
                break;
            }
        }
        return "redirect:" + UrlUtils.buildUrlQuery(baseUrl, MiscUtils.map()
                .add("redirectindex", redirectindex)
                .add("redirecturl", redirecturl)
        );
    }

    public String authorizeLoginRedirect(String targetAuthorizeUrl, CommunityApplication communityApplication) {

        logger.debug("authorizeLoginRedirect : {} {}", targetAuthorizeUrl, communityApplication);

        String login = ProductConfig.getMainSiteUcenterLoginUrl();
        Long userId = currentUserId();

        if (userId != null && userId != 0) {

            // 顺序很重要，要与 AuthorizeMapper.isAuthorized 一致
            LinkedHashMap<String, Serializable> map = MiscUtils.<String, Serializable>map()
                    .add("uid", String.valueOf(userId))
                    .add("app", communityApplication.name())
                    .add("timestamp", String.valueOf(System.currentTimeMillis()))
                    .add("v", "1.0");

            String sign = ApplicationAuthorization.generateSignature(map, communityApplication.name());

            String params = mapJoin(map);
            params += "&sign=" + sign + "&login=" + login;
            return "redirect:" + targetAuthorizeUrl + "?" + params;
        } else {
            return "redirect:/login.vpage";
        }
    }

    // FIXME: 临时模拟一个，guava这玩意我们不再依赖了。那么大的footprint。坑货啊
    private static String mapJoin(Map map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sbuf = new StringBuilder();
        for (Object o : map.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            String k = e.getKey().toString();
            String v = e.getValue().toString();
            String p = k + "=" + v;
            sbuf.append(p);
            sbuf.append("&");
        }
        if (sbuf.length() == 0) {
            return "";
        }
        sbuf.setLength(sbuf.length() - 1);
        return sbuf.toString();
    }


    /**
     * 登录授权到“市场人员”应用，已经没用了，直接注销
     */
    @RequestMapping(value = "marketing.vpage", method = RequestMethod.GET)
    public String marketing() {
        return "redirect:/ucenter/logout.vpage";
    }

    /**
     * 小学英语组卷平台
     */
    @RequestMapping(value = "exam.vpage", method = RequestMethod.GET)
    public String math() {
        return authorizeLoginRedirect(commonConfiguration.getExamUrl() + "/authorize.vpage", CommunityApplication.exam);
    }

    /**
     * 小学数学平台
     */
    @RequestMapping(value = "math.vpage", method = RequestMethod.GET)
    public String exam() {
        return authorizeLoginRedirect(commonConfiguration.getExamUrl() + "/authorize.vpage", CommunityApplication.math);
    }

    @RequestMapping(value = "partner.vpage", method = RequestMethod.GET)
    public String partner(@RequestParam(value = "url", required = false) String targetUrl, Model model) {

        String loginUrl = "/login.vpage";
        String urlInput = getRequest().getParameter("urlInput");

        // 用户必须登录
        User user = currentUser();
        if (user == null) {
            return "redirect:" + loginUrl;
        }
        try {
            String sign = openApiAuth.generateSign(user.getId());
            targetUrl = StringUtils.defaultIfEmpty(targetUrl, ProductConfig.getMainSiteBaseUrl());

            model.addAttribute("targetUrl", targetUrl);
            model.addAttribute("token", sign);
            model.addAttribute("uid", user.getId());
            model.addAttribute("urlInput", urlInput);

            logger.debug("授权完成，重定向到：" + targetUrl + ",token:" + sign + " uid:" + user.getId());
            return "open/redirect";
        } catch (Exception e) {
            logger.error("用户授权错误", e);
            return "redirect:" + loginUrl;
        }
    }

    // 论坛授权跳转
    @RequestMapping(value = "bbspartner.vpage", method = RequestMethod.GET)
    public String bbsPartner(@RequestParam(value = "url", required = false) String targetUrl, Model model) {

        String loginUrl = "/login.vpage";
        String urlInput = getRequest().getParameter("urlInput");

        // 用户必须登录
        User user = currentUser();
        if (user == null) {
            return "redirect:" + loginUrl;
        }
        try {
            String sign = openApiAuth.generateSign(user.getId());
            targetUrl = StringUtils.defaultIfEmpty(targetUrl, ProductConfig.getMainSiteBaseUrl());

            model.addAttribute("targetUrl", targetUrl);
            model.addAttribute("token", sign);
            model.addAttribute("uid", user.getId());
            model.addAttribute("urlInput", urlInput);

            logger.debug("授权完成，重定向到：" + targetUrl + ",token:" + sign + " uid:" + user.getId());
            return "open/redirect";
        } catch (Exception e) {
            logger.error("用户授权错误", e);
            return "redirect:" + loginUrl;
        }
    }

    @RequestMapping(value = "checkuserauth.vpage", method = RequestMethod.GET)
    public String checkUserAuth(Model model) {
        String urlInput = getRequest().getParameter("url_input");
        if (currentUser() != null && UserType.TEACHER == currentUser().fetchUserType()) {
            model.addAttribute("urlInput", urlInput);
            return "ucenter/tobbs";
        } else {
            return "redirect:/index.vpage";
        }
    }

    /**
     * 用户修改自己的密码
     * 平台新手任务会有修改密码的需求，暂时无法去掉
     */
    @RequestMapping(value = "resetmypw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map resetmypw() {
        User user = currentUser();
        String currentPassword = getRequestParameter("current_password", "");
        String newPassword = getRequestParameter("new_password", "");
        try {
            MapMessage message = userServiceClient.changePassword(user, currentPassword, newPassword);
            if (!message.isSuccess()) {
                throw new RuntimeException(message.getInfo());
            }
            // 用户自己修改密码记录一下
            saveChangePwdUserServiceRecord(user.getId(), FindPasswordMethod.MODIFY_PASSWORD, "UcenterController.resetmypw");

            //由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            //由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            washingtonAuthenticationHandler.resetAuthCookie(getWebRequestContext(), -1);

            //若是教师，修改密码之后发送短信
            if (user.fetchUserType() == UserType.TEACHER) {
                sendPasswordChangeNoticeForTeacher(user, newPassword);
            }

            // 如果学生修改密码，更新学生端sessionkey
            if (user.fetchUserType() == UserType.STUDENT) {
                updateAppSessionKeyForStudent(user);
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .unflushable_clearUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, user.getId())
                        .awaitUninterruptibly();
            }
            return MapMessage.successMessage("修改密码成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage("修改密码失败。请输入正确的当前登录密码。");
        }
    }

    private void saveChangePwdUserServiceRecord(Long userId, FindPasswordMethod method, String refer) {
        if (userId == null) {
            return;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperatorId(userId.toString());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改密码");
        userServiceRecord.setComments(method.getDescription());
        userServiceRecord.setAdditions("refer:" + refer);

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }
    /**
     * 用户通过手机验证码的方式重置密码
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "resetpwbycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPasswordUsingVerifyCode() {
        User user = currentUser();
        String verifyCode = getRequestString("verify_code");
        String newPassword = getRequestString("new_password");
        try {
            MapMessage verifyResult = smsServiceClient.getSmsService().verifyValidateCode(user.getId(), verifyCode, SmsType.TEACHER_CHANGE_PASSWORD.name());
            if (!verifyResult.isSuccess()) {
                return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
            }

            // 修改密码
            userServiceClient.setPassword(user, newPassword);

            // 用户自己修改密码记录一下
            saveChangePwdUserServiceRecord(user.getId(), FindPasswordMethod.MODIFY_PASSWORD, "UcenterController.resetPasswordUsingVerifyCode");
            //由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            //由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            washingtonAuthenticationHandler.resetAuthCookie(getWebRequestContext(), -1);

            //若是教师，修改密码之后发送短信
            if (user.fetchUserType() == UserType.TEACHER) {
                sendPasswordChangeNoticeForTeacher(user, newPassword);
            }

            // 如果学生修改密码，更新学生端sessionkey
            if (user.fetchUserType() == UserType.STUDENT) {
                updateAppSessionKeyForStudent(user);
            }

            return MapMessage.successMessage("修改密码成功，请重新登录");
        } catch (Exception ex) {
            return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
        }
    }

    /**
     * 用户修改自己的密码--不用输入原密码
     */
    @RequestMapping(value = "setmypw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map setmypw() {
        User user = currentUser();
        String newPassword = getRequestParameter("new_password", "");
        try {
            MapMessage message = userServiceClient.setPassword(user, newPassword);
            if (!message.isSuccess()) {
                throw new RuntimeException(message.getInfo());
            }
            // 用户自己修改密码记录一下
            saveChangePwdUserServiceRecord(user.getId(), FindPasswordMethod.MODIFY_PASSWORD, "UcenterController.setmypw");

            //由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            //由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            washingtonAuthenticationHandler.resetAuthCookie(getWebRequestContext(), -1);

            //若是教师，修改密码之后发送短信
            if (user.fetchUserType() == UserType.TEACHER) {
                sendPasswordChangeNoticeForTeacher(user, newPassword);
            }

            // 如果学生修改密码，更新学生端sessionkey
            if (user.fetchUserType() == UserType.STUDENT) {
                updateAppSessionKeyForStudent(user);
            }

            return MapMessage.successMessage("修改密码成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage("修改密码失败。");
        }
    }

    @RequestMapping(value = "resetmyname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetmyname() {
        String name = getRequestParameter("name", "");
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            return ForbidModifyNameAndPortrait.errorMessage;
//        }
        MapMessage message = userServiceClient.changeName(currentUserId(), StringHelper.cleanXSS(RealnameRule.removeInvalidRealNameChars(name)));
        if (message.isSuccess()) {
            User user = currentUser();
            if (user != null) {
                com.voxlearning.alps.spi.bootstrap.LogCollector.info("backend-general", MiscUtils.map("usertoken", user.getId(),
                        "usertype", user.getUserType(),
                        "platform", "pc",
                        "version", "",
                        "op", "change user name",
                        "mod1", user.fetchRealname(),
                        "mod2", name,
                        "mod3", user.getAuthenticationState()));
            }
            return MapMessage.successMessage("重设用户姓名成功");
        } else {
            return MapMessage.errorMessage("重设用户姓名失败");
        }
    }

    @RequestMapping(value = "unreadmessage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage message() {
        User user = currentUser();
        if (user != null) {
            int totalUnreadMessageCount = messageServiceClient.getMessageService().getUnreadMessageCount(user.narrow());
            int totalUnreadLetterCount = conversationLoaderClient.getConversationLoader().getUnreadLetterCount(user.getId());
            return MapMessage.successMessage().add("total", totalUnreadLetterCount + totalUnreadMessageCount);
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "task.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage task() {
        MapMessage mesg = new MapMessage();
        mesg.add("rows", new ArrayList<ArgMapper>());
        mesg.add("total", 0);
        mesg.setSuccess(true);
        return mesg;
    }

    /**
     * 用户上传头像
     */
    @RequestMapping(value = "avatar.vpage", method = RequestMethod.GET)
    public String avatar(@RequestParam(value = "avatar_cancel", required = false, defaultValue = "") String avatar_cancel, @RequestParam(value = "avatar_callback", required = false, defaultValue = "") String avatar_callback, Model model) {
        User user = currentUser();
        if (null != user) {
            model.addAttribute("avatar_callback", avatar_callback);
            model.addAttribute("avatar_cancel", avatar_cancel);
            model.addAttribute("face", user.fetchImageUrl());
            model.addAttribute("userId", user.getId());
        }
        return "/ucenter/avatar";
    }

    /**
     * 注册成功后发送密码短信
     */
    @RequestMapping(value = "sendpwsms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage signupSendSMS(@RequestBody UserSignSMSMapper command) {
        try {
            // 发短信
            User user = currentUser();

            if (user == null || UserType.of(user.getUserType()) != UserType.TEACHER) {
                return MapMessage.errorMessage("您无权发送短信，可能是注册不成功或请重新登录！");
            }

            String mobile = command.getMobile();

            //检查手机号码是否正确
            if (StringUtils.isBlank(mobile)) {
                return MapMessage.errorMessage("手机号码不正确！");
            }

//            String context = "恭喜您加入一起作业！您的账号：" + user.getId() + "，密码：" + user.getRealCode() + "，请妥善保管账号信息。现在就登录www.17zuoye.com开始使用吧！";
            String context = "恭喜您加入一起作业！请用此手机号作为账号登录http://www.17zuoye.com 使用！";

            return atomicLockManager.wrapAtomic(userServiceClient)
                    .proxy()
                    .sendRegistrationSms(user.getId(), mobile, context);
        } catch (Exception e) {
            if (e instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("发送成功！");
            }
            return MapMessage.errorMessage("发送失败，请重试！");
        }
    }

    /**
     * ***************************************  新版老师邀请  *****************************************
     */

//    /**
//     * 老师邀请老师，通过点击链接注册
//     *
//     * @param url   加密信息，内包含邀请人信息和时间戳
//     * @param model 返回给页面的参数
//     * @return 返回地址
//     * @throws Exception
//     */
//    @RequestMapping(value = "titlink.vpage", method = RequestMethod.GET)
//    public String teacherInviteTeacherByLinkProcess(@RequestParam("url") String url, Model model) throws Exception {
//        // 获取邀请人信息
//        Map<String, Object> linkInfo = decryptCode(url);
//        model.addAttribute("subject", Subject.of(ConversionUtils.toString(linkInfo.get("subject"))));
//
//        User inviter = userLoaderClient.loadUser(ConversionUtils.toLong(linkInfo.get("userId")));
//        if (inviter != null) {
//            if (inviter.fetchUserType() == UserType.TEACHER) {
//                model.addAttribute("invitationType", InvitationType.TEACHER_INVITE_TEACHER_LINK);
//            } else {
//                model.addAttribute("invitationType", InvitationType.STUDENT_INVITE_TEACHER_LINK);
//            }
//        }
//
//        addInfo(inviter, model);
//        return "plaza/middleschool";
//    }

//    /**
//     * 教研员邀请老师，通过点击链接注册
//     *
//     * @param url   加密信息，内包含邀请人信息和时间戳
//     * @param model 返回给页面的参数
//     * @return 返回地址
//     * @throws Exception
//     */
//    @RequestMapping(value = "ritlink.vpage", method = RequestMethod.GET)
//    public String rstaffInviteTeacherByLinkProcess(@RequestParam("url") String url, Model model) throws Exception {
//        // 获取邀请人信息
//        Map<String, Object> linkInfo = decryptCode(url);
//        User inviter = userLoaderClient.loadUser(ConversionUtils.toLong(linkInfo.get("userId")));
//        if (inviter != null) {
//            model.addAttribute("invitationType", InvitationType.RSTAFF_INVITE_TEACHER_LINK);
//        }
//        addInfo(inviter, model);
//        model.addAttribute("staff", "staff");
//
//        // 生成一个 contextId 用于防止机器人刷接口
//        String contextId = RandomUtils.randomString(10);
//        washingtonCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
//        model.addAttribute("contextId", contextId);
//        model.addAttribute("captchaToken", RandomUtils.randomString(24));
//        model.addAttribute("currentTime", System.currentTimeMillis());
//
//        return "plaza/middleschool";
//    }

    @RequestMapping(value = "invite.vpage", method = RequestMethod.GET)
    public String processStudentInvitation(@RequestParam("invitation") String invitation, Model model) throws Exception {
        if (StringUtils.isBlank(invitation)) {
            return "redirect:/";
        }
        return "redirect:/signup/htmlchip/student.vpage?invitation=" + invitation;
    }

//    @RequestMapping(value = "titemail.vpage", method = RequestMethod.GET)
//    public String teacherInviteTeacherByEmailProcess(@RequestParam("url") String url, Model model) throws Exception {
//        // 获取邀请人信息
//        Map<String, Object> linkInfo = decryptCode(url);
//        model.addAttribute("inviteEmail", ConversionUtils.toString(linkInfo.get("email")));
//        model.addAttribute("subject", Subject.valueOf(ConversionUtils.toString(linkInfo.get("subject"))));
//
//        User inviter = userLoaderClient.loadUser(ConversionUtils.toLong(linkInfo.get("userId")));
//        if (null != inviter) {
//            if (inviter.fetchUserType() == UserType.TEACHER) {
//                model.addAttribute("invitationType", InvitationType.TEACHER_INVITE_TEACHER_EMAIL);
//            } else {
//                model.addAttribute("invitationType", InvitationType.STUDENT_INVITE_TEACHER_EMAIL);
//            }
//        }
//
//        addInfo(inviter, model);
//        return "plaza/middleschool";
//    }

    // private method for teacher invitation

    private Map<String, Object> decryptCode(String code) {
        String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
        if (defaultDesKey == null) {
            throw new ConfigurationException("No 'default_des_key' configured");
        }
        String decryptCode = DesUtils.decryptHexString(defaultDesKey, code);
        return JsonUtils.fromJson(decryptCode);
    }

//    private void addInfo(User inviter, Model model) throws Exception {
//        if (inviter != null) {
//            model.addAttribute("inviteUserId", inviter.getId());
//            model.addAttribute("teacherName", inviter.getProfile().getRealname());
//            School school = asyncUserServiceClient.getAsyncUserService()
//                    .loadUserSchool(inviter)
//                    .getUninterruptibly();
//            if (school != null) {
//                model.addAttribute("schoolInfo", school.getCname());
//            }
//        }
//        // 生成一个 contextId 用于防止机器人刷接口
//        String contextId = RandomUtils.randomString(10);
//        washingtonCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
//        model.addAttribute("contextId", contextId);
//    }

    /**
     * 修改密码后给老师发送邮件通知
     *
     * @param user
     * @param newPassword
     * @author changyuan.liu
     */
    private void sendPasswordChangeNoticeForTeacher(User user, String newPassword) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        if (null != ua && ua.getSensitiveEmail() != null) {
            Map<String, Object> content = new LinkedHashMap<String, Object>();
            content.put("name", user.getProfile().getRealname());
            content.put("userId", user.getId());
            content.put("password", newPassword);   // <- put new password here
            content.put("hotline", Constants.HOTLINE_SPACED);
            content.put("date", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
            content.put("time", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 HH点mm分"));

            // 短信内容不能含有 “操” --> "...如非本人操作请与我们联系..."
            userEmailServiceClient.buildEmail(EmailTemplate.teachermodifypassword)
                    .to(ua)
                    .subject("您已更改在一起作业的个人资料")
                    .content(content)
                    .send();
        }
    }

    /**
     * 修改密码后更新学生App的session key
     *
     * @param user
     * @author changyuan.liu
     */
    private void updateAppSessionKeyForStudent(User user) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", user.getId());
        if (vendorAppsUserRef != null) {
            vendorServiceClient.expireSessionKey(
                    "17Student",
                    user.getId(),
                    SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), user.getId()));
        }
    }
}
