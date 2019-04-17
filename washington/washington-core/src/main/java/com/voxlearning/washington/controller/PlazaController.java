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

package com.voxlearning.washington.controller;

import com.nature.commons.lang.util.StringUtil;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.service.SsoService;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.helpers.SsoHelper;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.*;


@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
public class PlazaController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = SsoService.class)
    private SsoService ssoService;

    @Inject private SsoHelper ssoHelper;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Override
    public boolean onBeforeControllerMethod() {
        String serverName = getRequest().getServerName();

        //如果不带域名直接访问，则跳转到域名上，防止cookie出问题
        if (RuntimeMode.ge(Mode.STAGING) && !TopLevelDomain.isDomainCookieAllowed(serverName)) {
            try {
                getResponse().sendRedirect(ProductConfig.getMainSiteBaseUrl());
                return false;
            } catch (IOException ignored) {
            }
        }

        return super.onBeforeControllerMethod();
    }

    /**
     * @param 'source' [String]   来自那? eg: JZT
     * @description 公用的App 下载接口
     * @para cid [String]      渠道号  eg: 123456
     */
    @RequestMapping(value = "/appDownload.vpage", method = RequestMethod.GET)
    public String appDownload(Model model) {

        model
                .addAttribute("cid", getRequestString("cid"))
                .addAttribute("source", getRequestString("source"));

        return "block/downloadApp";
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String serverName = getRequest().getServerName();
        // 访问的是子域名
        if (serverName.contains(".17zuoye.com")) {
            String clientName = serverName.substring(0, serverName.indexOf("."));
            if (!clientName.contains("www") && !clientName.contains("staging") && !clientName.contains("cp")) {
                return "plaza/zhongxiaoxueIndex";
            }
        }

        if (currentUser() != null) {
            User user = currentUser();
            // 判断用户是否需要修改真实名称
            boolean supplementName = false;
            // 来源
            String referer = getRequest().getHeader("Referer");
            if (user.getUserType() == UserType.TEACHER.getType()) {
                Teacher teacher = currentTeacher();
                if (teacher.isPrimarySchool()) {
                    supplementName = StringUtil.isEmpty(teacher.getProfile().getRealname());
                } else {
                    supplementName = StringRegexUtils.isNotRealName(teacher.getProfile().getRealname());
                }
                if (supplementName) {
                    return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage?supplementName=true";
                }
                School school = asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(currentUser())
                        .getUninterruptibly();
                // 判断老师是否需要去修改密码页面
                if (needForceModifyPassword(school != null ? school.getId() : null, user)) {
                    return "redirect:/teacher/center/index.vpage#/teacher/center/securitycenter.vpage?_tab=pandaria";
                }
                if (currentTeacher().isKLXTeacher() || currentTeacher().isJuniorMathTeacher()) {
                    if (teacherLoaderClient.isFakeTeacher(currentUserId())) {
                        String url = ProductConfig.getUcenterUrl() + "/fakeappeal.vpage";
                        return "redirect:" + url;
                    } else {
                        // PC 跳转 如果是初中数学老师记录
                        if (teacher.isJuniorMathTeacher()) {
                            if (referer != null && referer.contains("shensz")) {
                                saveHomePageUrl(teacher.getId(), "/teacher/index.vpage");
                            } else {
                                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
                                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                                        .loadSchoolExtInfo(teacherDetail.getTeacherSchoolId())
                                        .getUninterruptibly();
                                if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                                    return "redirect:" + "/redirector/apps/go.vpage?app_key=Shensz";
                                } else {
                                    String regionCode = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.name(), "ONLINE_BUSINESS_CODE");
                                    if (teacherDetail.getCityCode() != null && StringUtils.isNoneBlank(regionCode) && Arrays.asList(regionCode.split(",")).contains(String.valueOf(teacherDetail.getCityCode()))) {
                                        String homePageUrl = getHomePageUrl(teacher.getId());
                                        return "redirect:" + (homePageUrl == null ? "/teacher/index.vpage" : homePageUrl);
                                    }
                                }
                            }
                        }
                        return "redirect:" + ProductConfig.getKuailexueUrl();
                    }
                }

            } else if (user.getUserType() == UserType.STUDENT.getType()) {
                StudentDetail studentDetail = currentStudentDetail();
                if (studentDetail.isPrimaryStudent()) {
                    supplementName = StringUtil.isEmpty(studentDetail.getProfile().getRealname());
                } else {
                    supplementName = StringRegexUtils.isNotRealName(studentDetail.getProfile().getRealname());
                }
                if (supplementName) {
                    return "redirect:" + ProductConfig.getUcenterUrl() + "/student/center/index.vpage?supplementName=true";
                }
                // 如果是初中生
                if (studentDetail.isJuniorStudent()) {
                    if (referer != null && referer.contains("shensz")) {
                        saveHomePageUrl(studentDetail.getId(), "/student/index.vpage");
                    } else {
                        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                                .loadSchoolExtInfo(studentDetail.getClazz().getSchoolId())
                                .getUninterruptibly();
                        if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                            return "redirect:" + "/redirector/apps/go.vpage?app_key=Shensz";
                        } else {
                            String regionCode = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.name(), "ONLINE_BUSINESS_CODE");
                            if (studentDetail.getCityCode() != null && StringUtils.isNoneBlank(regionCode) && Arrays.asList(regionCode.split(",")).contains(String.valueOf(studentDetail.getCityCode()))) {
                                String homePageUrl = getHomePageUrl(studentDetail.getId());
                                return "redirect:" + (homePageUrl == null ? "/student/index.vpage" : homePageUrl);
                            }
                        }
                    }
                }
            }
        }

        if (currentUserId() != null) {
            return "redirect:" + "/ucenter/home.vpage";
        }

        try {
            return "redirect:" + ssoHelper.generateSsoReturnUrl(getWebRequestContext(), "", "");
        } catch (UnsupportedEncodingException e) {
            logger.error("generate sso return url failed: {}", e);
            return "redirect:" + "/ucenter/home.vpage";
        }
//        return "plaza/index";

//        // 生成一个 contextId 用于防止机器人刷接口
//        String contextId = RandomUtils.randomString(10);
//        washingtonCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
//        model.addAttribute("contextId", contextId);
//        model.addAttribute("captchaToken", RandomUtils.randomString(24));
//        model.addAttribute("currentTime", System.currentTimeMillis());
//
////        return useNewLoginPage() ? "plaza/index" : "plaza/index_old";
//        return "plaza/index";
    }


    @RequestMapping(value = "middleschool.vpage", method = RequestMethod.GET)
    public String school(@RequestParam(value = "type", required = false, defaultValue = "3") Integer userType,
                         @RequestParam(value = "t", required = false, defaultValue = "3") Integer t,
                         @RequestParam(value = "o", required = false, defaultValue = "1") Integer o,
                         @RequestParam(value = "d", required = false, defaultValue = "-1") Integer d,
                         @RequestParam(value = "mode", required = false, defaultValue = "0") Integer mode,
                         @RequestParam(value = "m", required = false, defaultValue = "0") Integer m, Model model) {
        model.addAttribute("t", t);
        model.addAttribute("o", o);
        model.addAttribute("d", d);
        model.addAttribute("m", m);
        model.addAttribute("mode", mode);
        return "plaza/middleschool";
    }

    @RequestMapping(value = "login.vpage", method = RequestMethod.GET)
    public String login(HttpServletRequest request, Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/login.vpage";
    }

    @RequestMapping(value = "tempLogin.vpage", method = RequestMethod.GET)
    public String tempLogin(HttpServletRequest request, Model model) {
        // 出于开发需要，本地启动washington会与首页保持www有冲突
        // 需要做个sso跳转
        try {
            return "redirect:" + ssoHelper.generateSsoReturnUrl(getWebRequestContext(), "", "") + "&ref=login";
        } catch (UnsupportedEncodingException e) {
            logger.error("generate sso return url failed: {}", e);
            return "redirect:" + "/ucenter/home.vpage";
        }
    }

    @RequestMapping(value = "loadpage.vpage", method = RequestMethod.GET)
    public String loadpage(HttpServletRequest request, Model model, @RequestParam(value = "url", required = false, defaultValue = "") String redirectUrl,
                           @RequestParam(value = "l", required = false, defaultValue = "0") Integer mustLogin,
                           @RequestParam(value = "r", required = false, defaultValue = "") String redirect,
                           @RequestParam(value = "nc", required = false, defaultValue = "1") Integer notLoading) {

        model.addAttribute("redirectUrl", redirectUrl);
        model.addAttribute("redirect", redirect);
        model.addAttribute("notLoading", notLoading);
        if (currentUserId() == null && mustLogin == 1) {
            model.addAttribute("redirect", "top");
            model.addAttribute("redirectUrl", "/login.vpage");
        }
        return "plaza/loadpage";
    }

    /**
     * SSO回调接口
     * 此接口不需要登录
     * QueryString: ?appKey=*****&ticket=*******&returnURL=**********&timestamp=******&sign=*********
     */
    @RequestMapping(value = "ssojump.vpage", method = RequestMethod.GET)
    public String ssoJump() {
        String dataJson = getRequestParameter("data", null);
        if (dataJson == null) {
            return "redirect:/";
        }
        Map<String, Object> data = JsonUtils.fromJson(dataJson);
        if (MapUtils.isEmpty(data)) {
            return "redirect:/";
        }
        String appKey = SafeConverter.toString(data.get("appKey"));
        String ticket = SafeConverter.toString(data.get("ticket"));
        String returnUrl = SafeConverter.toString(data.get("returnURL"));
        String timestamp = SafeConverter.toString(data.get("timestamp"));
        String sign = SafeConverter.toString(data.get("sign"));

        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(ticket) || StringUtils.isBlank(returnUrl) || StringUtils.isBlank(timestamp) || StringUtils.isBlank(sign)) {
            logger.warn("sso jump failed,appKey:{},ticket:{},returnUrl:{},timestamp:{},sign:{}", appKey, ticket, returnUrl, timestamp, sign);
            return "redirect:/";
        }

        try {
            //app数据量比较小,只有几十条,未来也不会爆增,全部放到缓存里了 2015-12-23
            Optional<VendorApps> app = vendorLoaderClient.loadVendorAppsIncludeDisabled().values()
                    .stream()
                    .filter(t -> t.isVisible(RuntimeMode.current().getLevel()) && t.getAppKey().equals(ProductConfig.get("sso.wsd.app_key", "17Platform")))
                    .findFirst();
            if (!app.isPresent()) {
                logger.warn("sso jump app not exist,appKey:{},ticket:{},returnUrl:{},timestamp:{},sign:{}", appKey, ticket, returnUrl, timestamp, sign);
                return "redirect:/";
            }

            if (!ssoJumpValidate(app.get())) {
                return "redirect:/";
            }

            Optional<Long> userId = getUserInfoFromSso(app.get(), ticket);
            if (!userId.isPresent()) {
                return "redirect:/";
            }

            User user = raikouSystem.loadUser(userId.get());
            if (null == user) {
                logger.warn("Sso jump user not exist,userId:{},appKey:{},ticke:{},returnUrl:{},timestamp:{},sign:{}", userId.get(), appKey, ticket, returnUrl, timestamp, sign);
                return "redirect:/";
            }

            Set<RoleType> set = userLoaderClient.loadUserRoles(user);
            RoleType[] roleTypes = set.toArray(new RoleType[set.size()]);
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getWebRequestContext().saveAuthenticationStates(-1, userId.get(), ua.getPassword(), roleTypes);

            returnUrl = URLDecoder.decode(returnUrl, "UTF-8");
            if (user.isTeacher()) {
                if (teacherLoaderClient.isWebGrayFunctionAvailable(teacherLoaderClient.loadTeacherDetail(user.getId()), "LoginToHttps", "Teacher", false)) {
                    returnUrl = returnUrl.replaceAll("^http://", "https://");
                }
            }
            return "redirect:" + returnUrl;
        } catch (Exception ex) {
            logger.error("Sso jump error,appKey:{},ticke:{},returnUrl:{},timestamp:{},sign:{}", appKey, ticket, returnUrl, timestamp, sign, ex);
            return "redirect:/";
        }
    }

    private boolean ssoJumpValidate(VendorApps app) throws UnsupportedEncodingException {
        String dataJson = getRequestParameter("data", null);
        if (StringUtils.isBlank(dataJson)) {
            logger.warn("sso jump params incorrect,data {}", dataJson);
            return false;
        }
        Map<String, Object> data = JsonUtils.fromJson(dataJson);

        String appKey = SafeConverter.toString(data.get("appKey"));
        String ticket = SafeConverter.toString(data.get("ticket"));
        String returnUrl = SafeConverter.toString(data.get("returnURL"));
        String timestamp = SafeConverter.toString(data.get("timestamp"));
        String sign = SafeConverter.toString(data.get("sign"));

        if (!appKey.equals(app.getAppKey())) {
            logger.warn("sso jump app incorrect,appKey:{},ticket:{},returnUrl:{},timestamp:{},sign:{}", appKey, ticket, returnUrl, timestamp, sign);
            return false;
        }

        //validate signature
        Map<String, String> params = new HashMap<>();
        params.put("appKey", app.getAppKey());
        params.put("ticket", ticket);
        params.put("returnURL", returnUrl);
        params.put("timestamp", timestamp);

        String signature = DigestSignUtils.signMd5(params, app.getSecretKey());
        if (!sign.equals(signature)) {
            logger.warn("sso jump signature incorrect,appKey:{},ticket:{},returnUrl:{},timestamp:{},sign:{}", appKey, ticket, returnUrl, timestamp, sign);
            return false;
        }

        //validate timestamp
        if (Instant.now().minusSeconds(60 * 60).isAfter(Instant.ofEpochMilli(Long.parseLong(timestamp)))) {
            logger.warn("sso jump request expired,appKey:{},ticket:{},returnUrl:{},timestamp:{},sign:{}", appKey, ticket, returnUrl, timestamp, sign);
            return false;
        }

        return true;
    }

    private Optional<Long> getUserInfoFromSso(VendorApps app, String ticket) {
        Map<String, String> params = new HashMap<>();
        params.put("appKey", app.getAppKey());
        params.put("ticket", ticket);
        params.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        params.put("sign", DigestSignUtils.signMd5(params, app.getSecretKey()));

        String dataJson = JsonUtils.toJson(params);
        MapMessage message;
        try {
            message = ssoService.validateTicket(dataJson);
        } catch (Exception ex) {
            logger.error("Get user info from ucenter sso error,appKey:{},msg:{}", app.getAppKey(), ex.getMessage(), ex);
            return Optional.empty();
        }
        if (message == null || !message.isSuccess()) {
            logger.error("Get user info from ucenter sso failed,appKey:{},msg:{}", app.getAppKey(), JsonUtils.toJson(message));
            return Optional.empty();
        }
        if (null == message.get("uid") || !NumberUtils.isNumber(message.get("uid").toString())) {
            logger.error("Get user info from ucenter sso incorrect,uid not exist,appKey:{}", app.getAppKey());
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(message.get("uid").toString()));
    }

    /**
     * 如果老师是通过批量注册的方式注册进来，而且没改过密码，直接去修改密码页面
     */
    private boolean needForceModifyPassword(Long schoolId, User user) {
        if (AppAuditAccounts.isNoneForcePasswdUpdateSchool(schoolId) || !user.isBatchUser()) {
            return false;
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        return ua != null && (ua.fetchPasswordState() == PasswordState.AUTO_GEN);
    }

}
