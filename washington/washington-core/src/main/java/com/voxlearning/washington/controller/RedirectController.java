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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentOrgLoader;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.vendor.client.ThirdPartLoginServiceClient;
import com.voxlearning.washington.controller.thirdparty.qiyukf.model.QiYuOnlineCSConfig;
import com.voxlearning.washington.mapper.InviteStudentInfoMapper;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.thirdparty.qiyukf.QiYuConstants.ENABLE_ROBOT;
import static com.voxlearning.washington.controller.thirdparty.qiyukf.QiYuConstants.QUESTION_TYPE_MAP;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-2-27
 */
@Controller
@RequestMapping("/redirector")
@Slf4j
public class RedirectController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AsyncVendorServiceClient asyncVendorServiceClient;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private ThirdPartLoginServiceClient thirdPartLoginServiceClient;
    @Inject
    protected UserLoaderClient userLoaderClient;
    @Inject
    protected AgentOrgLoader agentOrgLoader;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    private static final String FROM_TYPE_PARENT = "17Parent";
    private static final String FROM_TYPE_STUDENT_SUFFIX = "17Student";
    private static final String FROM_TYPE_XUEBA = "17Xueba";
    private static final String SSZ_REDIRECT_URL = "/redirector/apps/go.vpage?app_key=Shensz";

    @Inject
    private EmailServiceClient emailServiceClient;

    /**
     * 第三方合作，功能跳转
     *
     * @param source
     * @param token
     * @return
     */
    @RequestMapping(value = "primaryhwlist.vpage", method = RequestMethod.GET)
    public String primaryhwlist(String source, String token) {
        String returnUrl = "/teacher/new/homework/report/list.vpage";
        if (null == checkLoginAndSetSession(source, token)) {
            logger.error("source: {}, token: {}", source, token);
            return "redirect:/";
        }
        return "redirect:" + returnUrl;
    }

    /**
     * 第三方合作，初始化登录信息，种cookie信息
     *
     * @param source
     * @param token
     * @return
     */
    @RequestMapping(value = "intlogin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage intlogin(String source, String token) {
        if (null == checkLoginAndSetSession(source, token)) {
            logger.error("source: {}, token: {}", source, token);
            return MapMessage.errorMessage().setInfo("请重新登陆!").setErrorCode(ErrorCodeConstants.ERROR_CODE_AUTH);
        }
        return MapMessage.successMessage();
    }

    /**
     * 第三方合作，功能跳转
     *
     * @param source
     * @param token
     * @return
     */
    @RequestMapping(value = "primaryresource.vpage", method = RequestMethod.GET)
    public String primaryClazzResourcelist(String source, String token) {
        Long userId = checkLoginAndSetSession(source, token);
        if (null == userId) {
            logger.error("source: {}, token: {}", source, token);
            return "redirect:/";
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        return "redirect:/teacher/teachingresource/index.vpage?log=leftMenu&subject=" + teacher.getSubject().name();
    }

    /**
     * 第三方合作，功能跳转
     *
     * @param source
     * @param token
     * @return
     */
    @RequestMapping(value = "juniorhwlist.vpage", method = RequestMethod.GET)
    public String juniorhwlist(String source, String token) {
        Long userId = checkLoginAndSetSession(source, token);
        if (null == userId) {
            logger.error("source: {}, token: {}", source, token);
            return "redirect:/";
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (Subject.MATH == teacher.getSubject()) {//跳转计算
            return "redirect:/redirector/apps/go.vpage?app_key=Shensz";
        } else if (Subject.ENGLISH == teacher.getSubject()) {//跳转英语
            return "redirect:" + ProductConfig.getJuniorSchoolUrl() + "/teacher/homework/list";
        } else {//跳转首页
            return "redirect:/";
        }
    }

    /**
     * 第三方合作，功能跳转
     *
     * @param source
     * @param token
     * @return
     */
    @RequestMapping(value = "juniorhwreport.vpage", method = RequestMethod.GET)
    public String juniorhwreport(String source, String token) {
        Long userId = checkLoginAndSetSession(source, token);
        if (null == userId) {
            logger.error("source: {}, token: {}", source, token);
            return "redirect:/";
        }

        String subject = "math";
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher.getSubject() != null) {
            subject = teacher.getSubject().name().toLowerCase();
        }

        String returnUrl = ProductConfig.getKuailexueUrl() + "/" + subject + "/Vanalysis/exam_list";

        return "redirect:" + returnUrl;
    }

    @RequestMapping(value = "zika.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadZiKaContent(String source, String token) {
        Long userId = checkLoginAndSetSession(source, token);
        if (userId == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null) {
            return MapMessage.errorMessage("未查询到此教师");
        }
        Integer authenticationState = teacher.getAuthenticationState();
        if (authenticationState != 1) {
            return MapMessage.errorMessage("老师认证后才能使用此功能");
        }
        String ziKaWord = getRequestString("ziKaWord");
        if (StringUtils.isEmpty(ziKaWord)) {
            return MapMessage.errorMessage("确实查询字卡参数");
        }
        if (ziKaWord.length() > 1) {
            return MapMessage.errorMessage("字卡查询只支持一个汉字");
        }
        String type = "ZI_KA";
        Map params = new HashMap();
        params.put("ziKaWord", ziKaWord);
        return newHomeworkContentServiceClient.loadTeachingResourceContent(null, null, null, null, type, params);
    }

    /**
     * 校验用户是否通过在第三方登录，并设置session
     *
     * @param source
     * @param token
     * @return 用戶ID
     * @author zhouwei
     */
    private Long checkLoginAndSetSession(String source, String token) {
        Long userId = null;
        MapMessage message = thirdPartLoginServiceClient.getThirdPartLoginService().checkLogin(source, token);
        if (!message.isSuccess() || null == message.get("userId")) {//api调用失败
            return userId;
        }
        //用户登录，设置session
        userId = SafeConverter.toLong(message.get("userId"));
        User user = raikouSystem.loadUser(userId);
        RoleType roleType = RoleType.of(user.getUserType());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);
        return userId;
    }

    @RequestMapping(value = "createnewclass/{code}.vpage", method = RequestMethod.GET)
    public String createnewclass(@PathVariable("code") String code, Model model) {
        String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
        if (legacy_des_key == null) {
            throw new ConfigurationException("No 'legacy_des_key' configured");
        }

        String decryptCode = DesUtils.decryptHexString(legacy_des_key, code);
        Map<String, Object> validationInfo = JsonUtils.fromJson(decryptCode);
        final Long userId = conversionService.convert(validationInfo.get("userId"), Long.class);
        final String password = conversionService.convert(validationInfo.get("password"), String.class);

        model.addAttribute("key", userId);
        model.addAttribute("value", password);
        try {
            return "open/autologin";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "plaza/index";
        }
    }

    @RequestMapping(value = "assignhomework/{code}.vpage", method = RequestMethod.GET)
    public String assignhomework(@PathVariable("code") String code, Model model) {
        String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
        if (legacy_des_key == null) {
            throw new ConfigurationException("No 'legacy_des_key' configured");
        }

        String decryptCode = DesUtils.decryptHexString(legacy_des_key, code);
        Map<String, Object> validationInfo = JsonUtils.fromJson(decryptCode);
        final Long userId = conversionService.convert(validationInfo.get("userId"), Long.class);
        final String password = conversionService.convert(validationInfo.get("password"), String.class);
        model.addAttribute("key", userId);
        model.addAttribute("value", password);
        try {
            return "open/autologin";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "plaza/index";
        }
    }

    @RequestMapping(value = "checkhomework/{code}.vpage", method = RequestMethod.GET)
    public String checkhomework(@PathVariable("code") String code, Model model) {
        String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
        if (legacy_des_key == null) {
            throw new ConfigurationException("No 'legacy_des_key' configured");
        }

        String decryptCode = DesUtils.decryptHexString(legacy_des_key, code);
        Map<String, Object> validationInfo = JsonUtils.fromJson(decryptCode);
        final Long userId = conversionService.convert(validationInfo.get("userId"), Long.class);
        final String password = conversionService.convert(validationInfo.get("password"), String.class);
        model.addAttribute("key", userId);
        model.addAttribute("value", password);
        try {
            return "open/autologin";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "plaza/index";
        }
    }

    @RequestMapping(value = "login/{code}.vpage", method = RequestMethod.GET)
    public String login(@PathVariable("code") String code, Model model) {
        String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
        if (legacy_des_key == null) {
            throw new ConfigurationException("No 'legacy_des_key' configured");
        }

        String decryptCode = DesUtils.decryptHexString(legacy_des_key, code);
        Map<String, Object> validationInfo = JsonUtils.fromJson(decryptCode);
        final Long userId = conversionService.convert(validationInfo.get("userId"), Long.class);
        final String password = conversionService.convert(validationInfo.get("password"), String.class);

        model.addAttribute("key", userId);
        model.addAttribute("value", password);
        try {
            return "open/autologin";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "plaza/index";
        }
    }

    @SuppressWarnings("ConstantConditions")
    @RequestMapping(value = "emailTest/{email}_{type}.vpage", method = RequestMethod.GET)
    public String emailTemplateTest(@PathVariable("email") String email, @PathVariable("type") String type) {
        log.info("收件:" + email + " 类型:" + type);

        if (type.equals("all") || type.equals("bindmobile")) {
            Map<String, Object> bindmobile = new LinkedHashMap<String, Object>();
            bindmobile.put("name", "--姓名--");
            bindmobile.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.bindmobile)
                    .to(email)
                    .subject("邮件模板测试 - bindmobile")
                    .content(bindmobile)
                    .send();
        }
        if (type.equals("all") || type.equals("emailmodle")) {
            Map<String, Object> emailmodle = new LinkedHashMap<String, Object>();
            emailmodle.put("name", "--姓名--");
            emailmodle.put("url", "http://www.17zuoye.com");
            emailmodle.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.emailmodle)
                    .to(email)
                    .subject("邮件模板测试 - emailmodle")
                    .content(emailmodle)
                    .send();
        }

        if (type.equals("all") || type.equals("studentinviteteachers")) {
            Map<String, Object> studentinviteteacher = new LinkedHashMap<String, Object>();
            studentinviteteacher.put("name", "--姓名--");
            studentinviteteacher.put("url", "http://www.17zuoye.com");
            studentinviteteacher.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.studentinviteteacher)
                    .to(email)
                    .subject("邮件模板测试 - studentinviteteacher")
                    .content(studentinviteteacher)
                    .send();
        }

        if (type.equals("all") || type.equals("integral")) {
            Map<String, Object> integral = new LinkedHashMap<String, Object>();
            integral.put("name", "--姓名--");
            integral.put("url", "http://www.17zuoye.com");
            integral.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.integral)
                    .to(email)
                    .subject("邮件模板测试 - integral")
                    .content(integral)
                    .send();
        }
        if (type.equals("all") || type.equals("password")) {
            Map<String, Object> password = new LinkedHashMap<String, Object>();
            password.put("name", "--姓名--");
            password.put("id", "--ID--");
            password.put("url", "http://www.17zuoye.com");
            password.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            password.put("time", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日 hh点 mm分"));
            password.put("hotline", Constants.HOTLINE_SPACED);
            emailServiceClient.createTemplateEmail(EmailTemplate.password)
                    .to(email)
                    .subject("邮件模板测试 - password")
                    .content(password)
                    .send();
        }
        if (type.equals("all") || type.equals("signup")) {
            Map<String, Object> signup = new LinkedHashMap<String, Object>();
            signup.put("name", "--测试姓名--");
            signup.put("id", "--ID--");
            signup.put("password", "--password--");
            signup.put("userType", "--userType--");
            signup.put("email", "--email--");
            signup.put("link", "http://www.17zuoye.com");
            signup.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            signup.put("hotline", Constants.HOTLINE_SPACED);
            emailServiceClient.createTemplateEmail(EmailTemplate.signup)
                    .to(email)
                    .subject("邮件模板测试 - signup")
                    .content(signup)
                    .send();
        }
        if (type.equals("all") || type.equals("teacheractivatedbystudent")) {
            List<InviteStudentInfoMapper> studentInfoMappers = null;

            Map<String, Object> teacheractivatedbystudent = new LinkedHashMap<String, Object>();
            teacheractivatedbystudent.put("name", "--测试姓名--");
            teacheractivatedbystudent.put("id", "--ID--");
            teacheractivatedbystudent.put("inviteeList", studentInfoMappers);
            teacheractivatedbystudent.put("link", "http://www.17zuoye.com");
            teacheractivatedbystudent.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teacheractivatedbystudent)
                    .to(email)
                    .subject("邮件模板测试 - teacheractivatedbystudent")
                    .content(teacheractivatedbystudent)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherAuthenticationsuccess")) {
            Map<String, Object> teacherAuthenticationsuccess = new LinkedHashMap<String, Object>();
            teacherAuthenticationsuccess.put("name", "--测试姓名--");
            teacherAuthenticationsuccess.put("link", "http://www.17zuoye.com");
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherAuthenticationsuccess)
                    .to(email)
                    .subject("邮件模板测试 - teacherAuthenticationsuccess")
                    .content(teacherAuthenticationsuccess)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherbind")) {
            Map<String, Object> bindemail = new LinkedHashMap<String, Object>();
            bindemail.put("name", "--测试姓名--");
            bindemail.put("link", "http://www.17zuoye.com");
            emailServiceClient.createTemplateEmail(EmailTemplate.bindemail)
                    .to(email)
                    .subject("邮件模板测试 - bindemail")
                    .content(bindemail)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherbindsuccess")) {
            Map<String, Object> teacherbindsuccess = new LinkedHashMap<String, Object>();
            teacherbindsuccess.put("name", "--测试姓名--");
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherbindsuccess)
                    .to(email)
                    .subject("邮件模板测试 - teacherbindsuccess")
                    .content(teacherbindsuccess)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherfindpassword")) {
            //待开发
        }
        if (type.equals("all") || type.equals("teacherfirstgetcheckhomeworkgold")) {
            Map<String, Object> teacherfirstgetcheckhomeworkgold = new LinkedHashMap<String, Object>();
            teacherfirstgetcheckhomeworkgold.put("name", "--测试姓名--");
            teacherfirstgetcheckhomeworkgold.put("count", "--园丁豆个数--");
            teacherfirstgetcheckhomeworkgold.put("link", "http://www.17zuoye.com");
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherfirstgetcheckhomeworkgold)
                    .to(email)
                    .subject("邮件模板测试 - teacherfirstgetcheckhomeworkgold")
                    .content(teacherfirstgetcheckhomeworkgold)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherfirsthomeworkdonotcheck")) {
            Map<String, Object> teacherfirsthomeworkdonotcheck = new LinkedHashMap<String, Object>();
            teacherfirsthomeworkdonotcheck.put("name", "--测试姓名--");
            teacherfirsthomeworkdonotcheck.put("link", "http://www.17zuoye.com");
            teacherfirsthomeworkdonotcheck.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherfirsthomeworkdonotcheck)
                    .to(email)
                    .subject("邮件模板测试 - teacherfirsthomeworkdonotcheck")
                    .content(teacherfirsthomeworkdonotcheck)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherhaveclassnohomework")) {
            Map<String, Object> teacherhaveclassnohomework = new LinkedHashMap<String, Object>();
            teacherhaveclassnohomework.put("name", "--测试姓名--");
            teacherhaveclassnohomework.put("userId", "--userId--");
            teacherhaveclassnohomework.put("password", "--password--");
            teacherhaveclassnohomework.put("link", "http://www.17zuoye.com");
            teacherhaveclassnohomework.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherhaveclassnohomework)
                    .to(email)
                    .subject("邮件模板测试 - teacherhaveclassnohomework")
                    .content(teacherhaveclassnohomework)
                    .send();
        }
        if (type.equals("all") || type.equals("teachermodifypassword")) {
            Map<String, Object> teachermodifypassword = new LinkedHashMap<String, Object>();
            teachermodifypassword.put("name", "--测试姓名--");
            teachermodifypassword.put("time", DateUtils.dateToString(new Date(), "yyyy.MM.dd hh.mm.ss"));
            teachermodifypassword.put("password", "--password--");
            teachermodifypassword.put("hotline", Constants.HOTLINE_SPACED);
            teachermodifypassword.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teachermodifypassword)
                    .to(email)
                    .subject("邮件模板测试 - teachermodifypassword")
                    .content(teachermodifypassword)
                    .send();
        }
        if (type.equals("all") || type.equals("teachermodifypersonalinformation")) {
            Map<String, Object> teachermodifypersonalinformation = new LinkedHashMap<String, Object>();
            teachermodifypersonalinformation.put("name", "--测试姓名--");
            teachermodifypersonalinformation.put("time", DateUtils.dateToString(new Date(), "yyyy.MM.dd hh.mm.ss"));
            teachermodifypersonalinformation.put("area", "--area--");
            teachermodifypersonalinformation.put("school", "--school--");
            teachermodifypersonalinformation.put("address", "--address--");
            teachermodifypersonalinformation.put("zipcode", "--zipcode--");
            teachermodifypersonalinformation.put("hotline", Constants.HOTLINE_SPACED);
            teachermodifypersonalinformation.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teachermodifypersonalinformation)
                    .to(email)
                    .subject("邮件模板测试 - teachermodifypersonalinformation")
                    .content(teachermodifypersonalinformation)
                    .send();
        }
        if (type.equals("all") || type.equals("teachernewregisternoclass")) {
            Map<String, Object> teachernewregisternoclass = new LinkedHashMap<String, Object>();
            teachernewregisternoclass.put("name", "--测试姓名--");
            teachernewregisternoclass.put("userId", "--userId--");
            teachernewregisternoclass.put("password", "--password--");
            teachernewregisternoclass.put("link", "http://www.17zuoye.com");
            teachernewregisternoclass.put("hotline", Constants.HOTLINE_SPACED);
            teachernewregisternoclass.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teachernewregisternoclass)
                    .to(email)
                    .subject("邮件模板测试 - teachernewregisternoclass")
                    .content(teachernewregisternoclass)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherweekreportassignhomework")) {
            Map<String, Object> teacherweekreportassignhomework = new LinkedHashMap<String, Object>();
            teacherweekreportassignhomework.put("name", "--测试姓名--");
            teacherweekreportassignhomework.put("classNum", "--classNum--");
            teacherweekreportassignhomework.put("homeworkNum", "--homeworkNum--");
            teacherweekreportassignhomework.put("studentNum", "--studentNum--");
            teacherweekreportassignhomework.put("finishNum", "--finishNum--");
            teacherweekreportassignhomework.put("notFinishNum", "--notFinishNum--");
            teacherweekreportassignhomework.put("avgScore", "--avgScore--");
            teacherweekreportassignhomework.put("commentsNum", "--commentsNum--");
            teacherweekreportassignhomework.put("integralNum", "--integralNum--");
            teacherweekreportassignhomework.put("link", "http://www.17zuoye.com");
            teacherweekreportassignhomework.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherweekreportassignhomework)
                    .to(email)
                    .subject("邮件模板测试 - teacherweekreportassignhomework")
                    .content(teacherweekreportassignhomework)
                    .send();
        }
        if (type.equals("all") || type.equals("teacherweekreportassignnohomework")) {
            Map<String, Object> teacherweekreportassignnohomework = new LinkedHashMap<String, Object>();
            teacherweekreportassignnohomework.put("name", "--测试姓名--");
            teacherweekreportassignnohomework.put("link", "http://www.17zuoye.com");
            teacherweekreportassignnohomework.put("hotline", Constants.HOTLINE_SPACED);
            teacherweekreportassignnohomework.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.teacherweekreportassignnohomework)
                    .to(email)
                    .subject("邮件模板测试 - teacherweekreportassignnohomework")
                    .content(teacherweekreportassignnohomework)
                    .send();
        }
        if (type.equals("all") || type.equals("verifymobile")) {
            Map<String, Object> verifymobile = new LinkedHashMap<String, Object>();
            verifymobile.put("name", "--测试姓名--");
            verifymobile.put("date", DateUtils.dateToString(new Date(), "yyyy年 MM月 dd日"));
            emailServiceClient.createTemplateEmail(EmailTemplate.verifymobile)
                    .to(email)
                    .subject("邮件模板测试 - verifymobile")
                    .content(verifymobile)
                    .send();
        }
        return "plaza/index";
    }

    /**
     * 获取七鱼客服的配置信息。为壳使用七鱼的SDK准备的
     *
     * @return
     */
    @RequestMapping(value = "loadqiyukfconfig.vpage")
    @ResponseBody
    public MapMessage loadQiyuKfConfig() {
        Long userId = getRequestLong("userId");
        String questionType = getRequestString("questionType");
        String fromType = getRequestString("fromType");
        String type = "";
        if (questionType.toUpperCase().equals("TIANJI_ALL")) {
            type = "marketer";
            questionType = "marketer";
        }

        //构建信息
        Map<String, Object> infos = buildInfo(type, questionType, userId);

        //获取七鱼客服配置信息
        QiYuOnlineCSConfig qiYuOnlineCSConfig = this.qiYuOnlineCSConfig((String) infos.get("questionType"), (User) infos.get("user"));

        //17学生、家长启用机器人
        int robotId = 0;// 机器人id
        if (ENABLE_ROBOT.contains(type)) {
            robotId = qiYuOnlineCSConfig.getRobotId();
        }

        MapMessage resultMsg = new MapMessage();
        resultMsg.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMsg.add("destId", qiYuOnlineCSConfig.getCsGroupId());
        resultMsg.add("robustOption", robotId > 0 ? 1 : 0);
        resultMsg.add("qType", qiYuOnlineCSConfig.getQtype());
        resultMsg.add("robotId", robotId);

        // 代上自定义crm参数“用户类型”
        List<Map<String, Object>> crmParams = new ArrayList<>();

        // 七鱼在用调用用户信息接口的时候，无法保存name。需要显示的在js传入
        Map<String, Object> nameParam = new HashMap<>();
        nameParam.put("key", "real_name");
        nameParam.put("value", infos.get("name"));
        crmParams.add(nameParam);

        Map<String, Object> crmParam = new HashMap<>();
        crmParam.put("type", "crm_param");
        crmParam.put("key", "usertype");
        crmParam.put("value", infos.get("userType"));
        crmParams.add(crmParam);

        resultMsg.add("data", JsonUtils.toJson(crmParams));

        LoggerUtils.info("loadQiyuKfConfig", userId, questionType, fromType, resultMsg);
        return resultMsg;
    }

    /**
     * 跳转到七鱼客服系统
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "onlinecs_new.vpage", method = RequestMethod.GET)
    public String qiyukf(Model model) {

        String type = getRequestString("type");
        Long userId = getRequestLong("userId");
        String questionTypeo = getRequestString("question_type").trim();
        String questionType = questionTypeo;
        String origin = getRequestStringCleanXss("origin");
        if (questionType.toUpperCase().equals("TIANJI_ALL")) {
            type = "marketer";
            questionType = "marketer";
        }

        Map<String, Object> infos = buildInfo(type, questionType, userId);

        //获取七鱼客服配置
        QiYuOnlineCSConfig qiYuOnlineCSConfig = this.qiYuOnlineCSConfig((String) infos.get("questionType"), (User) infos.get("user"));

        // 如果有传入的话，优先用传入的，否则找匹配的
        if (StringUtils.isEmpty(origin)) {
            origin = "未知";
        }

        int robotId = 0;// 机器人id
        type = (String) infos.get("type");
        //非"反馈建议"问题小学、老师和家长启用机器人
        if (ENABLE_ROBOT.contains(type)) {
            robotId = qiYuOnlineCSConfig.getRobotId();
        }
        // 代上自定义crm参数“用户类型”
        List<Map<String, Object>> crmParams = new ArrayList<>();

        // 七鱼在用调用用户信息接口的时候，无法保存name。需要显示的在js传入
        Map<String, Object> nameParam = new HashMap<>();
        nameParam.put("key", "real_name");
        nameParam.put("value", infos.get("name"));
        crmParams.add(nameParam);

        // 头像信息
        Map<String, Object> avatarParam = new HashMap<>();
        avatarParam.put("key", "avatar");
        avatarParam.put("index", 0);
        avatarParam.put("value", infos.get("avatarUrl"));
        avatarParam.put("href", infos.get("avatarUrl"));
        crmParams.add(avatarParam);

        Map<String, Object> crmParam = new HashMap<>();
        crmParam.put("type", "crm_param");
        crmParam.put("key", "usertype");
        crmParam.put("value", infos.get("userType"));
        crmParams.add(crmParam);

        model.addAttribute("destId", qiYuOnlineCSConfig.getCsGroupId());
        model.addAttribute("uid", infos.get("customerId"));
        model.addAttribute("data", JsonUtils.toJson(crmParams));
        model.addAttribute("pageOrigin", origin);
        model.addAttribute("robustOption", robotId > 0 ? 1 : 0);
        model.addAttribute("qtype", qiYuOnlineCSConfig.getQtype());
        model.addAttribute("robotId", robotId);

        Map<String, String> msg = new HashMap<>();
        msg.put("destId", qiYuOnlineCSConfig.getCsGroupId() + "");
        msg.put("questionType", questionType);
        msg.put("uid", (String) infos.get("customerId"));
        msg.put("data", JsonUtils.toJson(crmParams));
        msg.put("pageOrigin", origin);
        msg.put("robotId", robotId + "");
        msg.put("qtype", qiYuOnlineCSConfig.getQtype() + "");
        msg.put("reqType", type);
        msg.put("reqQuestionType", questionTypeo);
        msg.put("reqUserId", SafeConverter.toString(userId));
        LoggerUtils.info("onlinecs_new", msg);
        return "other/qiyukf_jump";

    }

    /**
     * 构建
     *
     * @param type
     * @param questionType
     * @param userId
     * @return
     */
    private Map<String, Object> buildInfo(String type, String questionType, Long userId) {
        //check
        if (StringUtils.isBlank(questionType)) {// FIXME 临时解决 questionType为空的问题,需要查各个入口端
            questionType = "question_other";
        }

        //问题类型格式化
        questionType = questionType.trim().toLowerCase();
        if (QUESTION_TYPE_MAP.containsKey(questionType)) {
            questionType = QUESTION_TYPE_MAP.get(questionType);
        }

        String customerId = "";
        String name = "";
        String avatarUrl = "";
        // 市场来源的，用户信息不从上下文拿，通过userId查。
        User user = null;
        if (!"marketer".equals(type)) {
            user = currentUser();
        }
        if (user != null) {
            customerId = String.valueOf(user.getId());
            name = user.fetchRealname();
            avatarUrl = getUserAvatarImgUrl(user);
            // 如果是姓名为空的家长，则把名字置成孩子姓名 + 称呼
            if (StringUtils.isEmpty(name) && user.getUserType() == UserType.PARENT.getType()) {
                StudentParentRef spRef = parentLoaderClient.loadParentStudentRefs(user.getId())
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (spRef != null) {
                    User stu = raikouSystem.loadUser(spRef.getStudentId());
                    name = stu.fetchRealname() + spRef.getCallName();
                }
            }
        } else if ("marketer".equals(type)) {
            AgentUser agentUser = agentUserLoaderClient.load(userId);
            if (agentUser != null) {
                customerId = String.valueOf(userId);
                name = agentUser.getRealName();
            }
            List<SchoolLevel> schoolLevels = agentOrgLoader.fetchUserServeSchoolLevels(userId);
            if (schoolLevels != null && !schoolLevels.isEmpty()) {
                SchoolLevel schoolLevel = schoolLevels.get(0);
                questionType = type + (schoolLevel == SchoolLevel.HIGH || schoolLevel == SchoolLevel.MIDDLE ? "_m" : "_p");
            }
            if (QiYuOnlineCSConfig.nameOf(questionType) == null) {
                questionType = QiYuOnlineCSConfig.marketer_p.name();
            }
        } else {
            // 还有种情况是从App端跳转过来的，从Web的上下文拿不到用户信息
            user = raikouSystem.loadUser(userId);
            if (user != null) {
                name = user.fetchRealname();
                customerId = String.valueOf(userId);
            }
        }

        // 如果能查到用户信息，并且在没有传入身份的前提下，设置用户的身份字段
        if (user != null && StringUtils.isBlank(type)) {
            type = UserType.of(user.getUserType()).name().toLowerCase();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("customerId", customerId);
        result.put("name", name);
        result.put("avatarUrl", avatarUrl);
        result.put("user", user);
        result.put("questionType", questionType);
        result.put("type", type);
        result.put("userType", StringUtils.equals(type, "marketer") ? 1 : 0);
        return result;
    }


    /**
     * 获取七鱼在线客服配置信息
     *
     * @param questionType
     * @param user
     * @return
     */
    private QiYuOnlineCSConfig qiYuOnlineCSConfig(String questionType, User user) {
        QiYuOnlineCSConfig result = QiYuOnlineCSConfig.nameOf(questionType);
        if (result != null) {
            return result;
        }

        if (user != null && user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            questionType = questionType + (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher() ? "_mt" : "_pt");
            result = QiYuOnlineCSConfig.nameOf(questionType);
            if (result != null) {
                return result;
            }
            return QiYuOnlineCSConfig.question_other_pt;
        } else if (user != null && user.fetchUserType() == UserType.STUDENT) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            questionType = questionType + (studentDetail.isSeniorStudent() || studentDetail.isJuniorStudent() ? "_ms" : "_ps");
            result = QiYuOnlineCSConfig.nameOf(questionType);
            if (result != null) {
                return result;
            }
            return QiYuOnlineCSConfig.question_other_ps;
        } else if (user != null && user.fetchUserType() == UserType.PARENT) {
            questionType = questionType + "_parent";
            result = QiYuOnlineCSConfig.nameOf(questionType);
            if (result != null) {
                return result;
            }
            return QiYuOnlineCSConfig.question_bangzhu_parent;
        } else {
            return QiYuOnlineCSConfig.question_account_ps;
        }

    }

    @RequestMapping(value = "onlinecsm.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String gotoOnlinecsSystem4Mobile() {
        String userId = getRequestString("userId");
        String fromType = getRequestString("fromType");
        String sys = getRequestString("sys");
        String model = getRequestString("model");
        String appVersion = getRequestString("app_version");
        String systemVersion = getRequestString("system_version");
        String origin = getRequestString("origin");

        String questionType = getRequestString("question_type");
        if (StringUtils.isBlank(questionType)) {
            questionType = getRequestString("questionType");
        }

        // 转换一下
        String type = "";
        // 家长和学吧都映射到parent上面
        List<String> mapToParentType = Arrays.asList(FROM_TYPE_PARENT, FROM_TYPE_XUEBA);
        if (mapToParentType.contains(fromType)) {
            type = "parent";
        } else if (fromType.startsWith(FROM_TYPE_STUDENT_SUFFIX)) {
            type = "student";
        }
        return "redirect:" + UrlUtils.buildUrlQuery("/redirector/onlinecs_new.vpage",
                MapUtils.m(
                        "userId", userId
                        , "question_type", questionType
                        , "type", type
                        , "origin", origin
                ));
    }

    @RequestMapping(value = "onlinecsm/evaluate.vpage", method = RequestMethod.GET)
    public String gotoEvaluate4Mobile() {
        String soid = getRequestString("soid");
        String employeeId = getRequestString("employeeId");
        String customerId = getRequestString("customerId");
        String satisfactionId = getRequestString("satisfactionId");
        String ifClose = getRequestString("ifClose");
        Integer solveProblem = getRequestInt("solveProblem");
        Integer reason0 = getRequestInt("reason0");
        Integer reason1 = getRequestInt("reason1");
        Integer reason2 = getRequestInt("reason2");
        Integer reason3 = getRequestInt("reason3");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("evaluate.soid", soid);
        paramMap.put("evaluate.employeeId", employeeId);
        paramMap.put("evaluate.customerId", customerId);
        paramMap.put("evaluate.satisfactionId", satisfactionId);
        paramMap.put("evaluate.advice", "");
        paramMap.put("msgParam.scType", "SR");
        paramMap.put("ifClose", ifClose);
        paramMap.put("evaluate.solveProblem", solveProblem);
        paramMap.put("evaluate.Reason0", reason0);
        paramMap.put("evaluate.Reason1", reason1);
        paramMap.put("evaluate.Reason2", reason2);
        paramMap.put("evaluate.Reason3", reason3);


        String serviceUrl = "onlinecsm.17zuoye.com:81";
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
            serviceUrl = "124.202.195.250:8140";
        }

        String param = UrlUtils.buildQueryString(paramMap);

        return "redirect:http://" + serviceUrl + "/WebCall4/WCALL_sendEvaluate.action?" + param;
    }

    @RequestMapping(value = "apps/go.vpage", method = RequestMethod.GET)
    public String goApps(Model model) {

        String appKey = getRequestString(REQ_APP_KEY);
        User user = currentUser();
        if (StringUtils.isBlank(appKey)) {
            return "redirect:/";
        }

        if (user == null) {
            return "redirect:/login.vpage?returnURL=/redirector/apps/go.vpage?app_key=" + appKey;
        }

        VendorApps app = vendorLoaderClient.getExtension().loadVendorApp(appKey);
        if (app == null) {
            return "redirect:/";
        }

        // 如果跳极算，则记录一下
        if (Objects.equals(appKey, "Shensz")) {
            saveHomePageUrl(user.getId(), SSZ_REDIRECT_URL);
        }

        //索尼数学大赛
        if (appKey.equals(OrderProductServiceType.GlobalMath.name())) {
            long userId;
            if (user.isParent()) {
                userId = getRequestLong("sid");
                Set<Long> students = studentLoaderClient.loadParentStudents(user.getId())
                        .stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());
                if (!students.contains(userId)) return "redirect:/";
            } else {
                userId = user.getId();
            }
            MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                    .registerVendorAppUserRef(appKey, userId)
                    .getUninterruptibly();
            if (!message.isSuccess() || null == message.get("ref")) return "redirect:/";
            VendorAppsUserRef vendorAppsUserRef = (VendorAppsUserRef) message.get("ref");

            String sessionKey = vendorAppsUserRef.getSessionKey();
            model.addAttribute("session_key", sessionKey);
            String basic = getRequestString("basic");

            if (!StringUtils.isBlank(basic)) {
                model.addAttribute("basic", basic);
            }
            return "redirect:" + app.getAppUrl();

        }

        Long userId = currentUserId();

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, userId)
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) return "redirect:/";
        VendorAppsUserRef vendorAppsUserRef = (VendorAppsUserRef) message.get("ref");

        String sessionKey = vendorAppsUserRef.getSessionKey();
        // model.addAttribute("session_key", sessionKey);

        // source
        String source = "pc";
        String ua = getRequest().getHeader("User-Agent");
        if (StringUtils.isNoneBlank(ua) && (ua.contains("17Teacher") || ua.contains("17Student") || ua.contains("17Parent"))) {
            source = "app";
        }

        String returnUrl = getRequestString("return_url");

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("source", source);
        paramMap.put("session_key", sessionKey);
        if (StringUtils.isNoneBlank(returnUrl)) {
            paramMap.put("return_url", returnUrl);
        }

        return "redirect:" + UrlUtils.buildUrlQuery(app.getAppUrl(), paramMap);
    }


    @RequestMapping(value = "log/go.vpage", method = RequestMethod.GET)
    public String logGo(Model model) {
        String goUrl = getRequestString("url");
        if (StringUtils.isBlank(goUrl)) {
            return "redirect:/";
        }

        String userId = "";
        if (currentUserId() != null) {
            userId = SafeConverter.toString(currentUserId());
        }

        String params = getRequestString("logparams");

        LogCollector.instance().info("a17zy_log_go",
                MiscUtils.map(
                        "go_url", goUrl,
                        "user_id", userId,
                        "params", params,
                        "env", RuntimeMode.getCurrentStage(),
                        "client_ip", getWebRequestContext().getRealRemoteAddr()
                ));

        return "redirect:" + goUrl;
    }

    @RequestMapping(value = "temp_unusable.vpage", method = RequestMethod.GET)
    public String tempUnusable(Model model) {
        try {
            getWebRequestContext().getResponse().sendError(501, "当前网络拥挤，请稍候登录。");
        } catch (Exception e) {
            logger.error("error happened when do redirect", e);
        }
        return "";
    }

    // 带上必要的参数跳转一起学奥数
    @RequestMapping(value = "goaoshu.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String goAoShu(Model pageModel) {
        String returnURL = getRequestString("returnURL");
        if (StringUtils.isBlank(returnURL)) { // 未知的跳转URL
            String requestSchema = "https://";
            if (!getWebRequestContext().isHttpsRequest()) {
                requestSchema = "http://";
            }

            // FIXME 先替换test和staging， production等楼上通知
            String path = "17xue-student.test.17zuoye.net/m/auth/jzt/entry.vpage";
            if (RuntimeMode.isStaging()) {
                path = "17xue-student.staging.17zuoye.net/m/auth/jzt/entry.vpage";
            } else if (RuntimeMode.isProduction()) {
                path = "micro.17zuoye.com/micro";
                Date changeTime = DateUtils.stringToDate("2017-08-24 20:00:00", DateUtils.FORMAT_SQL_DATETIME);
                Date curTime = new Date();
                if (curTime.after(changeTime)) {
                    path = "xue.17xueba.com/m/auth/jzt/entry.vpage";
                }
            }

            returnURL = requestSchema + path;
        }

        Map<String, String> paramMap = new HashMap<>();

        Long realStudentId = 0L;

        User user = currentUser();
        if (user != null) {
            paramMap.put("uid", SafeConverter.toString(user.getId()));
            realStudentId = user.getId();
        } else {
            paramMap.put("uid", "0");
        }

        Long sid = getRequestLong("sid");
        if (sid > 0L) {
            paramMap.put("sid", SafeConverter.toString(sid));
            realStudentId = sid;
        } else if (user != null && user.fetchUserType() == UserType.PARENT) {
            // find the first child as sid
            List<User> children = studentLoaderClient.loadParentStudents(user.getId());
            if (CollectionUtils.isNotEmpty(children)) {
                paramMap.put("sid", SafeConverter.toString(children.get(0).getId()));
                realStudentId = children.get(0).getId();
            } else {
                paramMap.put("sid", "0");
            }
        }

        String app_version = getRequestString("app_version");
        paramMap.put("app_version", app_version);
        String model = getRequestString("model");
        paramMap.put("model", model);
        String imei = getRequestString("imei");
        paramMap.put("imei", imei);
        String channel = getRequestString("channel");
        paramMap.put("channel", channel);
        String system_version = getRequestString("system_version");
        paramMap.put("system_version", system_version);
        String env = getRequestString("env");
        paramMap.put("env", env);

        // 加上数字签名
        VendorApps apps = vendorLoaderClient.loadVendor("YiQiXue");
        if (apps != null) {
            String sig = DigestSignUtils.signMd5(paramMap, apps.getSecretKey());
            paramMap.put("sig", sig);
        }

        // user_type, school_level不放入签名
        paramMap.put("user_type", user == null ? UserType.ANONYMOUS.name() : user.fetchUserType().name());
        appendSchoolLevel(paramMap, realStudentId);

        //一起学家长任务参数
        String orderReferer = getRequestString("orderreferer");
        if (StringUtils.isNotBlank(orderReferer)) {
            paramMap.put("orderreferer", orderReferer);
        }
        Long msInviter = getRequestLong("msinviter");
        String msidy = getRequestString("msidy");
        if (0 != msInviter && StringUtils.isNotBlank(msidy)) {
            paramMap.put("msinviter", msInviter.toString());
            paramMap.put("msidy", msidy);
        }

        return "redirect:" + UrlUtils.buildUrlQuery(returnURL, paramMap);
    }

    private void appendSchoolLevel(Map<String, String> paramMap, Long userId) {
        try {
            if (userId == null || userId <= 0L) {
                return;
            }

            User user = raikouSystem.loadUser(userId);
            if (!user.isStudent()) {
                return;
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (studentDetail == null || studentDetail.getClazz() == null) {
                return;
            }

            paramMap.put("school_level", studentDetail.getClazz().getEduSystem().getKtwelve().name());

        } catch (Exception e) {
            logger.error("appendSchoolLevel error.", e);
        }
    }

    // 带上必要的参数跳转一起学
    @Deprecated
    @RequestMapping(value = "go17Xue.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String go17Xue(Model pageModel) {
        String returnURL = getRequestString("returnURL");
        if (StringUtils.isBlank(returnURL)) { // 未知的跳转URL
            String requestSchema = "https://";
            if (!getWebRequestContext().isHttpsRequest()) {
                requestSchema = "http://";
            }

            String path = "17xue-student.test.17zuoye.net/m/auth/jzt/entry.vpage";
            if (RuntimeMode.isStaging()) {
                path = "17xue-student.staging.17zuoye.net/m/auth/jzt/entry.vpage";
            } else if (RuntimeMode.isProduction()) {
                path = "www.17xueba.com/m/auth/jzt/entry.vpage";
            }

            returnURL = requestSchema + path;
        }

        Map<String, String> paramMap = new HashMap<>();

        User user = currentUser();
        if (user != null) {
            paramMap.put("uid", SafeConverter.toString(user.getId()));
        } else {
            paramMap.put("uid", "0");
        }

        Long sid = getRequestLong("sid");
        if (sid > 0L) {
            paramMap.put("sid", SafeConverter.toString(sid));
        } else if (user != null && user.fetchUserType() == UserType.PARENT) {
            // find the first child as sid
            List<User> children = studentLoaderClient.loadParentStudents(user.getId());
            if (CollectionUtils.isNotEmpty(children)) {
                paramMap.put("sid", SafeConverter.toString(children.get(0).getId()));
            } else {
                paramMap.put("sid", "0");
            }
        }

        String app_version = getRequestString("app_version");
        paramMap.put("app_version", app_version);
        String model = getRequestString("model");
        paramMap.put("model", model);
        String imei = getRequestString("imei");
        paramMap.put("imei", imei);
        String channel = getRequestString("channel");
        paramMap.put("channel", channel);
        String system_version = getRequestString("system_version");
        paramMap.put("system_version", system_version);
        String env = getRequestString("env");
        paramMap.put("env", env);

        // 加上数字签名
        VendorApps apps = vendorLoaderClient.loadVendor("YiQiXue");
        if (apps != null) {
            String sig = DigestSignUtils.signMd5(paramMap, apps.getSecretKey());
            paramMap.put("sig", sig);
        }

        return "redirect:" + UrlUtils.buildUrlQuery(returnURL, paramMap);
    }

    //老师直播
    @RequestMapping(value = "teacherlive.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String teacherLive() {
        User user = currentUser();
        if (user == null || !user.isTeacher()) {
            return "redirect:/";
        }

        String returnURL = getRequestString("returnURL");
        if (StringUtils.isBlank(returnURL)) {
            return "redirect:/";
        }

        String logoUrl = getUserAvatarImgUrl(user);
        String p = "||" + user.getId() + "|" + user.fetchRealname() + "|1";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("logoUrl", logoUrl);
        paramMap.put("p", p);

        return "redirect:" + UrlUtils.buildUrlQuery(returnURL, paramMap);
    }

    @RequestMapping(value = "go3p.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String gothird(Model pageModel) {
        String returnURL = getRequestString("returnURL");
        if (StringUtils.isBlank(returnURL)) { // 未知的跳转URL
            return "redirect:/";
        }

        Map<String, String> paramMap = new HashMap<>();

        User user = currentUser();
        if (user != null) {
            paramMap.put("uid", SafeConverter.toString(user.getId()));
        } else {
            paramMap.put("uid", "0");
        }

        return "redirect:" + UrlUtils.buildUrlQuery(returnURL, paramMap);
    }

    @RequestMapping(value = "get_live_redirect_addr.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getLiveRedirectAddr() {
        try {
            Map<String, String> urlParams = new HashMap<>();
            urlParams.put("app_id", getRequestString("app_id"));
            urlParams.put("avatar_url", getRequestString("avatar_url"));
            urlParams.put("live_id", getRequestString("live_id"));
            urlParams.put("nickname", getRequestString("nickname"));
            urlParams.put("room_index", getRequestString("room_index"));
            urlParams.put("timestamp", getRequestString("timestamp"));
            urlParams.put("user_id", getRequestString("user_id"));
            urlParams.put("user_type", getRequestString("user_type"));
            List<String> keys = new ArrayList<>(urlParams.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                sb.append(key).append('=').append(urlParams.get(key)).append('&');
            }
            sb.setLength(sb.length() - 1);

            String secretKey = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "live_secret_key");
            Validate.notBlank(secretKey, "secretKey配置丢失!");

            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            String sign = Hex.encodeHexString(mac.doFinal(sb.toString().getBytes("UTF-8")));
            urlParams.put("sign", sign);

            urlParams.put("recommend_text", getRequestString("recommend_text"));
            urlParams.put("recommend_url", getRequestString("recommend_url"));

            String urlTpl;
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest())
                urlTpl = "https://activity.test.17zuoye.net/index.html";
            else
                urlTpl = "https://livecdn.17zuoye.com/zylive/index.html";

            String url = UrlUtils.buildUrlQuery(urlTpl, urlParams);
            return MapMessage.successMessage().add("url", url);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "now.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage now() {
        return MapMessage.successMessage().add("timestamp", System.currentTimeMillis());
    }

    @RequestMapping(value = "appreport.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String appReport() {
        String appKey = getRequestString("app_key");
        String appId = getEduyunAppId(appKey, getRequest());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appId", appId);

        User user = currentUser();
        if (user != null) {
            paramMap.put("userId", SafeConverter.toString(user.getId()));
        } else {
            paramMap.put("userId", "0");
        }

        return "redirect:" + UrlUtils.buildUrlQuery("http://system.eduyun.cn/bmp-web/sysAppReport/appReport", paramMap);
    }

    @RequestMapping(value = "appeval.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String appEval() {
        String appKey = getRequestString("app_key");
        String appId = getEduyunAppId(appKey, getRequest());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appId", appId);

        User user = currentUser();
        if (user != null) {
            paramMap.put("userId", SafeConverter.toString(user.getId()));
        } else {
            paramMap.put("userId", "0");
        }

        return "redirect:" + UrlUtils.buildUrlQuery("http://system.eduyun.cn/bmp-web/getSpAppDetail_index", paramMap);
    }

    private String getEduyunAppId(String appKey, HttpServletRequest request) {
        String appId = SsoConnections.Cnedu.getClientId();

        if ("17Student".equals(appKey)) {
            if (isIOSRequest(getRequest())) {
                appId = SsoConnections.CneduStudentIOS.getClientId();
            } else {
                appId = SsoConnections.CneduStudent.getClientId();
            }
        } else if ("17Teacher".equals(appKey)) {
            if (isIOSRequest(getRequest())) {
                appId = SsoConnections.CneduTeacherIOS.getClientId();
            } else {
                appId = SsoConnections.CneduTeacher.getClientId();
            }
        } else if ("17JuniorStu".equals(appKey)) {
            if (isIOSRequest(getRequest())) {
                appId = SsoConnections.CneduJuniorStuIOS.getClientId();
            } else {
                appId = SsoConnections.CneduJuniorStu.getClientId();
            }
        } else if ("17JuniorTea".equals(appKey)) {
            if (isIOSRequest(getRequest())) {
                appId = SsoConnections.CneduJuniorTeaIOS.getClientId();
            } else {
                appId = SsoConnections.CneduJuniorTea.getClientId();
            }
        }

        return appId;
    }
}
