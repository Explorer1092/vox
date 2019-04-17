/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.util.Configurable;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.random.RandomUtils;
import com.voxlearning.alps.lang.util.MapMessage;

import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.persist.entity.loginlog.AgentUserLoginLog;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.log.AgentUserLoginLogService;
import com.voxlearning.utopia.agent.service.user.UserConfigService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.support.AgentUserSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * AgentAuthController
 * Created by Shuai.Huan on 2014/7/3.
 */
@Controller
@RequestMapping("/")
public class AgentAuthController extends AbstractAgentController {

    private List<String> noCheckCaptchaUsersForTestEvn = Arrays.asList("jinlingzy", "jinlingsjl", "jinlingdq", "testchenghong");

    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserConfigService userConfigService;
    @Inject private AgentUserServiceClient agentUserServiceClient;
    @Inject private AgentUserLoginLogService agentUserLoginLogService;
    @Inject private AgentUserSupport agentUserSupport;
    @Inject private AgentRequestSupport agentRequestSupport;

    @RequestMapping(value = "auth/login.vpage", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("client", "pc");
        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        return "auth/login";
    }

    @Deprecated
    @RequestMapping(value = "mobile/login.vpage", method = RequestMethod.GET)
    public String mobileLogin(Model model) {
        String userName = getRequestString("username");
        model.addAttribute("client", "h5");
        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        model.addAttribute("userName", userName);
        return "mobile/work_record/login";
    }

    @RequestMapping(value = "mobile/login.vpage", method = RequestMethod.POST)
    public String mobileLoginPost(Model model) {
        return loginPost(model);
    }

    @RequestMapping(value = "auth/login.vpage", method = RequestMethod.POST)
    public String loginPost(Model model) {
        String client = getRequestString("client");
        if (StringUtils.isBlank(client)) {
            client = "pc";
        }
        String userName = getRequestString("username");
        String password = getRequestString("password");

        String encodeUserName = "";
        try {
            encodeUserName = URLEncoder.encode(userName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        // FIXME 应对扫描处理
        if (userName.length() >= 20 || password.length() >= 20) {
            getAlertMessageManager().addMessageInfo("用户名或密码长度不能超过20");
            model.addAttribute("client", client);
            if (client.equals("h5")) {
                return redirect("/mobile/login.vpage?username=" + encodeUserName);
            } else {
                return "auth/login";
            }
        }

        if(!(RuntimeMode.le(Mode.TEST) && noCheckCaptchaUsersForTestEvn.contains(userName))){ // 测试环境对指定账户不进行验证码校验， 主要是QA 那边的工具使用, 有问题 @金玲
            String captchaToken = getRequestString("captchaToken");
            String captchaCode = getRequestString("captchaCode");
            if (!consumeCaptchaCode(captchaToken, captchaCode)) {
                getAlertMessageManager().addMessageInfo("验证码失效或错误，请重新输入");
                model.addAttribute("code", 1);
                if (client.equals("h5")) {
                    return redirect("/mobile/login.vpage?username=" + encodeUserName);
                } else {
                    return "auth/login";
                }
            }
        }

        AgentUser agentUser = baseUserService.getByAccountName(userName);

        // staging环境超级密码
        boolean superUser = false;
        if (RuntimeMode.isStaging() && ((!Objects.equals(userName, "admin") && password.equals("_17zy2017")) || (Objects.equals(userName, "admin") && password.equals("_17zuoyeAdmin")))) {
            superUser = true;
        }
        if (RuntimeMode.isTest() && password.equals("test")) {
            superUser = true;
        }
        if (RuntimeMode.isDevelopment()) {
            superUser = true;
        }

        int code = 0;
        if (agentUser == null || agentUser.getStatus() == 9) {
            code = 2;
            getAlertMessageManager().addMessageInfo("用户名错误，请重新输入");
        } else if (!Password.obscurePassword(password, agentUser.getPasswdSalt()).equals(agentUser.getPasswd()) && !superUser){
            code = 4;
            getAlertMessageManager().addMessageInfo("密码错误，请重新输入");
        } else {

            // 设置设备ID
            String deviceId = agentRequestSupport.getDeviceId(getRequest());
            if (StringUtils.isBlank(agentUser.getDeviceId()) && StringUtils.isNotBlank(deviceId)) {
                agentUser.setDeviceId(deviceId);
                baseUserService.updateAgentUser(agentUser);
            }

            AuthCurrentUser currentUser = new AuthCurrentUser();
            currentUser.setUserId(agentUser.getId());
            currentUser.setUserName(agentUser.getAccountName());
            currentUser.setRealName(agentUser.getRealName());
            currentUser.setUserPhone(agentUser.getTel());
            currentUser.setGroupName(baseOrgService.getUserGroupNames(agentUser.getId()));
            currentUser.setRoleList(internalAuthDataLoader.loadUserRoleList(agentUser.getId()));
            currentUser.setDeviceId(agentUser.getDeviceId());
            if(currentUser.isProductOperator()){ // 如果是产品运营人员的话，设置影子账号ID
                List<AgentGroup> agentGroupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Country); // 获取市场部
                if(CollectionUtils.isNotEmpty(agentGroupList)){ // 获取全国总监的用户ID, 作为产品运营人员的影子账号
                    Long shadowUserId = baseOrgService.getGroupManager(agentGroupList.get(0).getId());
                    currentUser.setShadowId(shadowUserId);
                }
            }
            // 只允许全国总监的协作账号登录使用 == 省培训师
            if (currentUser.isDataViewer() && client.equals("h5")) {
                code = 3;
                getAlertMessageManager().addMessageInfo("暂不支持协作账号使用本应用，请申请专员账号");
            } else {
                currentUser.setAuthPathList(internalAuthDataLoader.loadUserAuthPathList(agentUser.getId()));
                currentUser.setPageElementCodes(internalAuthDataLoader.loadUserPageElementCodes(currentUser.getUserId()));
                currentUser.setOperationCodes(internalAuthDataLoader.loadUserOperationCodes(currentUser.getUserId()));
                agentCacheSystem.setAuthCurrentUser(currentUser.getUserId(), currentUser);
                agentCacheSystem.updateUserAuthRefreshTime(currentUser.getUserId());
                setUserAndSignToCookie(SafeConverter.toString(currentUser.getUserId()), agentApiAuth.getUserSign(currentUser.getUserId()));
                getAlertMessageManager().clearMessages();
                asyncLogService.logLogin(currentUser, super.getRequest().getRequestURI(), "Login Success", client);
            }
        }

        if (code != 0) {
            model.addAttribute("client", client);
            model.addAttribute("code", code);
            if (client.equals("h5")) {
                return redirect("/mobile/login.vpage?username=" + encodeUserName);
            } else {
                return "auth/login";
            }
        }

        if (client.equals("h5")) {
            return redirect("/mobile/index.vpage");
        } else {
            return redirect("/");
        }
    }



    @RequestMapping(value = "auth/logout.vpage", method = RequestMethod.GET)
    String logout() {
        AuthCurrentUser user = getCurrentUser();
        asyncLogService.logLogout(user, super.getRequest().getRequestURI(), "Logout Success", "");
        //清空cookie
        removeUserAndSignFromCookie();

        // 删除cache数据
        try {
            agentApiAuth.clearTicket(user.getUserId());
            agentCacheSystem.removeUserSession(user.getUserId());
            agentCacheSystem.getAlertMessageCache().evict(user.getUserId());
        } catch (Exception e) {
        }

        if(agentRequestSupport.isMobileRequest(getRequest())){
            return redirect("/view/mobile/crm/login/redirect_login.vpage");
        }else {
            String client = getRequestString("client");
            if (StringUtils.isBlank(client)) {
                client = "pc";
            }
            if (client.equals("h5")) {
                return redirect("/mobile/login.vpage");
            } else {
                return redirect("/");
            }
        }
    }


    @RequestMapping(value = "resetPassword.vpage", method = RequestMethod.GET)
    String index(Model model) {
        String client = getRequestString("client");
        int status = getRequestInt("status", -1);
        if (StringUtils.isBlank(client)) {
            client = "pc";
        }
        if (client.equals("h5")) {
            model.addAttribute("status", status);
            return "mobile/workbench/password";
        } else {
            return "auth/password";
        }
    }

    @RequestMapping(value = "resetPassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage reset(String password, String newPassword) {
        asyncLogService.logResetPassword(getCurrentUser(), getRequest().getRequestURI(), "reset my password,userId:" + getCurrentUserId(), "");
        return userConfigService.resetPassword(getCurrentUserId(), password, newPassword, true);
    }

    @RequestMapping(value = "captcha")
    public void captchaApi(HttpServletResponse resp) throws IOException{
        captcha(resp);
    }

    @RequestMapping(value = "captcha.vpage", method = RequestMethod.GET)
    public void captcha(HttpServletResponse resp) throws IOException {
        String token = getRequestParameter("token", "");
        Properties properties = new Properties();
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest() || RuntimeMode.isStaging()) {
            properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0");
        } else {
            properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0123456789");
        }
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "80");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "30");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "25");
        properties.setProperty(Constants.KAPTCHA_BORDER, "no");
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.FishEyeGimpy");

        Producer kaptchaProducer = new DefaultKaptcha();
        ((Configurable) kaptchaProducer).setConfig(new Config(properties));

        // Set to expire far in the past.
        resp.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");

        // return a jpeg
        resp.setContentType("image/jpeg");

        // create the text for the image
        String capText = kaptchaProducer.createText();

        // create the image with the text
        BufferedImage bi = kaptchaProducer.createImage(capText);

        // write the data out
        ImageIO.write(bi, "jpg", resp.getOutputStream());

        //set the attributes after we write the image in case the image writing fails.
        saveCaptchaCode(token, capText);
    }

    @RequestMapping(value = "auth/login_by_mobile.vpage", method = RequestMethod.GET)
    public String loginByPhone(Model model) {
        String mobile = getRequestString("mobile");
        model.addAttribute("client", "pc");
        model.addAttribute("mobile", mobile);
        return "auth/login_by_mobile";
    }

    @RequestMapping(value = "mobile/login_by_mobile.vpage", method = RequestMethod.GET)
    public String mobileLoginByPhone(Model model) {
        String mobile = getRequestString("mobile");
        model.addAttribute("client", "h5");
        model.addAttribute("mobile", mobile);
        return "mobile/work_record/login_by_mobile";
    }
    @RequestMapping(value = "auth/login_by_mobile.vpage", method = RequestMethod.POST)
    public String loginByPhonePost(Model model, HttpServletRequest request) {
        return loginPostByMobile(model,request);
    }

    @RequestMapping(value = "mobile/login_by_mobile.vpage", method = RequestMethod.POST)
    public String loginPostByMobile(Model model, HttpServletRequest request) {
        String client = getRequestString("client");
        if (StringUtils.isBlank(client)) {
            client = "pc";
        }
        String mobile = getRequestString("mobile");
        String virificationCode = getRequestString("captchaCode");

        String encodeMobile = "";
        try {
            encodeMobile = URLEncoder.encode(mobile, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        if (!MobileRule.isMobile(mobile)) {
            model.addAttribute("client", client);
            model.addAttribute("mobile",encodeMobile);
            if (client.equals("h5")) {
                return redirect("/mobile/login_by_mobile.vpage?mobile=" + encodeMobile);
            } else {
                return "auth/login_by_mobile";
            }
        }

        AgentUser agentUser = baseUserService.getByMobile(mobile);

        int code = 0;
        if (agentUser == null || agentUser.getStatus() == 9) {
            getAlertMessageManager().addMessageInfo("该手机号码未绑定账号，请重新输入或联系管理员换绑手机号码");
            code = 2;
        } else if (!verifySmsCode(mobile, virificationCode, SmsType.APP_CRM_MOBILE_LOGIN).isSuccess()) {
            getAlertMessageManager().addMessageInfo("验证码失效或错误，请重新输入");
            code = 1;
        } else {
            // 设置设备ID
            String deviceId = agentRequestSupport.getDeviceId(getRequest());
            //设备ID更新
            if (StringUtils.isNotBlank(deviceId) && !deviceId.equals(agentUser.getDeviceId())) {
                agentUser.setDeviceId(deviceId);
                baseUserService.updateAgentUser(agentUser);
                AgentUserLoginLog log = new AgentUserLoginLog();
                log.setOptTime(new Date());
                log.setNewDeviceString(deviceId);
                log.setOldDeviceString(agentUser.getDeviceId());
                log.setOptUserId(agentUser.getId());
                log.setOptUserName(agentUser.getRealName());
                agentUserLoginLogService.insertLoginLog(log);
            }

            AuthCurrentUser currentUser = new AuthCurrentUser();
            currentUser.setUserId(agentUser.getId());
            currentUser.setUserName(agentUser.getAccountName());
            currentUser.setRealName(agentUser.getRealName());
            currentUser.setUserPhone(agentUser.getTel());
            currentUser.setGroupName(baseOrgService.getUserGroupNames(agentUser.getId()));
            currentUser.setRoleList(internalAuthDataLoader.loadUserRoleList(agentUser.getId()));
            currentUser.setDeviceId(deviceId);
            if(currentUser.isProductOperator()){ // 如果是产品运营人员的话，设置影子账号ID
                List<AgentGroup> agentGroupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Country); // 获取市场部
                if(CollectionUtils.isNotEmpty(agentGroupList)){ // 获取全国总监的用户ID, 作为产品运营人员的影子账号
                    Long shadowUserId = baseOrgService.getGroupManager(agentGroupList.get(0).getId());
                    currentUser.setShadowId(shadowUserId);
                }
            }
            // 只允许全国总监的协作账号登录使用 == 省培训师
            if (currentUser.isDataViewer() && client.equals("h5")) {
                code = 3;
                getAlertMessageManager().addMessageInfo("暂不支持协作账号使用本应用，请申请专员账号");
            } else {
                currentUser.setAuthPathList(internalAuthDataLoader.loadUserAuthPathList(agentUser.getId()));
                currentUser.setPageElementCodes(internalAuthDataLoader.loadUserPageElementCodes(currentUser.getUserId()));
                currentUser.setOperationCodes(internalAuthDataLoader.loadUserOperationCodes(currentUser.getUserId()));
                agentCacheSystem.setAuthCurrentUser(currentUser.getUserId(), currentUser);
                agentCacheSystem.updateUserAuthRefreshTime(currentUser.getUserId());
                setUserAndSignToCookie(SafeConverter.toString(currentUser.getUserId()), agentApiAuth.getUserSign(currentUser.getUserId()));
                getAlertMessageManager().clearMessages();
                asyncLogService.logLogin(currentUser, super.getRequest().getRequestURI(), "Login Success", client);
            }
        }
        if (code != 0) {
            model.addAttribute("client", client);
            model.addAttribute("code", code);
            model.addAttribute("mobile",encodeMobile);
            if (client.equals("h5")) {
                return redirect("/mobile/login_by_mobile.vpage?mobile=" + encodeMobile);
            } else {
                return  "auth/login_by_mobile";
            }
        }
        if (client.equals("h5")) {
            return redirect("/mobile/index.vpage");
        } else {
            return redirect("/");
        }
    }


    @RequestMapping(value = "mobile/getSMSCode.vpage")
    @ResponseBody
    public MapMessage getSMSCode() {
        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请填写正确的手机号码").add("result", 400).add("message", "请填写正确的手机号码");
        }
        AgentUser agentUser = baseUserService.getByMobile(mobile);
        if (null == agentUser){
            return MapMessage.errorMessage("该手机号码未绑定账号，请重新输入或联系管理员换绑手机号码").add("result", 400).add("message", "该手机号码未绑定账号，请重新输入或联系管理员换绑手机号码");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.APP_CRM_MOBILE_LOGIN.name(), false).add("result", "success");
    }


}
