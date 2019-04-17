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

package com.voxlearning.ucenter.controller.index;

import com.nature.commons.lang.util.StringUtil;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.AccountStatus;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.SpecialTeacherLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * login controller
 *
 * @author changyuan.liu
 * @since 2015.12.6
 */
@Controller
@RequestMapping("/")
public class PlazaController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private SpecialTeacherLoaderClient specialTeacherLoaderClient;

    @Override
    public boolean onBeforeControllerMethod() {
        String serverName = getRequest().getServerName();

        //如果不带域名直接访问，则跳转到域名上，防止cookie出问题
        if (RuntimeMode.ge(Mode.STAGING) && !TopLevelDomain.isDomainCookieAllowed(serverName)) {
            try {
                getResponse().sendRedirect(ProductConfig.getUcenterUrl());
                return false;
            } catch (IOException ignored) {
            }
        }

        return super.onBeforeControllerMethod();
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        String serverName = getRequest().getServerName();
        // 访问的是子域名
        // 这里有个坑
        // 非www域名在线上会直接弄一个空页面，这里可能是为了防止cdn打开首页用的
        if (serverName.contains(".17zuoye.com")) {
            String clientName = serverName.substring(0, serverName.indexOf("."));
            if (!clientName.contains("www") && !clientName.contains("staging") && !clientName.contains("ucenter") && !clientName.contains("cp")) {
                return "plaza/zhongxiaoxueIndex";
            }
        }

        if (currentUserId() != null) {
            User user = currentUser();

            // 判断用户是否需要修改真实名称
            boolean supplementName = false;
            if (user.getUserType() == UserType.TEACHER.getType()) {
                Teacher teacher = currentTeacher();
                if (teacher.isPrimarySchool()) {
                    supplementName = StringUtil.isEmpty(teacher.getProfile().getRealname());
                } else {
                    supplementName = StringRegexUtils.isNotRealName(teacher.getProfile().getRealname());
                }
                if (supplementName) {
                    return "redirect:/teacher/center/index.vpage";
                }
            } else if (user.getUserType() == UserType.STUDENT.getType()) {
                StudentDetail studentDetail = currentStudentDetail();
                if (studentDetail.isPrimaryStudent()) {
                    supplementName = StringUtil.isEmpty(studentDetail.getProfile().getRealname());
                } else {
                    supplementName = StringRegexUtils.isNotRealName(studentDetail.getProfile().getRealname());
                }
                if (supplementName) {
                    return "redirect:/student/center/index.vpage";
                }
            }

            School school = asyncUserServiceClient.getAsyncUserService()
                    .loadUserSchool(currentUser())
                    .getUninterruptibly();
            if (user.getUserType() == UserType.TEACHER.getType()) {
                // 判断老师是否需要去修改密码页面
                if (needForceModifyPassword(school != null ? school.getId() : null, user)) {
                    return "redirect:/teacher/center/index.vpage#/teacher/center/securitycenter.vpage?_tab=pandaria";
                }

                TeacherDetail teacher = currentTeacherDetail();
                List<Clazz> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(Collections.singleton(teacher.getId()))
                        .values().stream()
                        .flatMap(Collection::stream)
                        .filter(c -> !c.isTerminalClazz())
                        .filter(c -> !c.isDisabledTrue())
                        .collect(Collectors.toList());

                if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
                    if (teacherLoaderClient.isFakeTeacher(currentUserId())) {
                        return "redirect:/fakeappeal.vpage";
                    }

                    // FIXME 如果没有教务老师，那么老师可以自己选班，就去选班页面，否则直接跳业务页面
                    // Enhancement #64429 老师注册至有教务学校的学校后,除英语学科老师外,允许老师不带班
                    boolean ignored = CollectionUtils.isNotEmpty(specialTeacherLoaderClient.findSchoolAffairTeachers(teacher.getTeacherSchoolId()))
                            && Subject.ENGLISH != teacher.getSubject();
                    if (!ignored && CollectionUtils.isEmpty(teacherClazzs)) {
                        return "redirect:/teacher/systemclazz/clazzindex.vpage?step=showtip";
                    }

                    String url = ProductConfig.getKuailexueUrl();
                    return "redirect:" + url;
                } else {
                    // FIXME 初中英语老师，判断有没有班级，如果没有，去选班
                    if (CollectionUtils.isEmpty(teacherClazzs)) {
                        return "redirect:/teacher/systemclazz/clazzindex.vpage?step=showtip";
                    }
                }
            }

            String url = school != null && (school.getLevel() == 2 || school.getLevel() == 4) ?
                    ProductConfig.getJuniorSchoolUrl()
                    : (ProductConfig.getMainSiteBaseUrl() + "/ucenter/home.vpage");
            return "redirect:" + url;
        }

        // 生成一个 contextId 用于防止机器人刷接口
        String contextId = RandomUtils.randomString(10);
        ucenterWebCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
        model.addAttribute("contextId", contextId);
        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        model.addAttribute("currentTime", System.currentTimeMillis());

        return "plaza/index";
    }

    @RequestMapping(value = "kuailexue.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String goKuailexue(Model model) {
        String url = ProductConfig.getKuailexueUrl();
        return "redirect:" + url;
    }

    @RequestMapping(value = "sszindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String goShenSuanZi(Model model) {
        User user = currentUser();
        if (user != null) {
            return "redirect:" + ProductConfig.getMainSiteBaseUrl();
        }
        return "redirect:/login.vpage?webSource=" + UserWebSource.Shensz.getSource();
    }

    @RequestMapping(value = "fakeappeal.vpage", method = RequestMethod.GET)
    public String fakeAppeal(Model model) {
        String onlineServiceUrl = ProductConfig.getMainSiteBaseUrl() + "/redirector/onlinecs_new.vpage";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("type", "question_klx");
        model.addAttribute("onlinecs", UrlUtils.buildUrlQuery(onlineServiceUrl, paramMap));

        return "plaza/fakeAppeal";
    }

    @RequestMapping(value = "pauseAccount.vpage", method = RequestMethod.GET)
    public String pauseAccount(Model model) {
        String onlineServiceUrl = ProductConfig.getMainSiteBaseUrl() + "/redirector/onlinecs_new.vpage";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("type", "question_klx");
        model.addAttribute("onlinecs", UrlUtils.buildUrlQuery(onlineServiceUrl, paramMap));
        return "plaza/pauseAccount";
    }

    @RequestMapping(value = "pauseAccountAction.vpage")
    @ResponseBody
    public MapMessage pauseAccountAction(Model model) {
        Long userId = currentUserId();
        User user = raikouSystem.loadUser(userId);
        if (user != null && user.isTeacher()) {
            String code = getRequestString("code");
            MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(userId, code, SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name());
            if (mapMessage.isSuccess()) {
                TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(userId);
                if (teacherExtAttribute == null) {
                    teacherExtAttribute = new TeacherExtAttribute();
                }
                teacherExtAttribute.setAccountStatus(AccountStatus.NORMAL);
                teacherServiceClient.upsertTeacherExtAttribute(teacherExtAttribute);
                return MapMessage.successMessage();
            }
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "login.vpage", method = RequestMethod.GET)
    public String login(HttpServletRequest request, Model model) {
        //获取用户登录之前的页面
        String returnURL = request.getParameter("returnURL");
        if (returnURL != null) {
            model.addAttribute("returnURL", returnURL);
        } else {
            // 从Referer读取之前的页面地址
            String referer = request.getHeader("Referer");
            String host = request.getHeader("Host");
            if (StringUtils.startsWithIgnoreCase(referer, "http://" + host)
                    && !StringUtils.containsIgnoreCase(referer, "/login.vpage")) {
                model.addAttribute("returnURL", referer);
            }
        }

        String webSource = request.getParameter("webSource");
        model.addAttribute("isShensz", Objects.equals(UserWebSource.Shensz.getSource(), webSource));

        User user = currentUser();
        if (user != null) {
            StringBuilder userinfo = new StringBuilder();

            // 教研员和校长账号需要区分开
            UserType userType = UserType.of(user.getUserType());
            String description = userType.getDescription();
            if (userType == UserType.RESEARCH_STAFF) {
                ResearchStaff researchStaff = researchStaffLoaderClient.loadResearchStaff(user.getId());
                if (researchStaff.isPresident()) {
                    description = "校长";
                } else if (researchStaff.isAffairTeacher()) {
                    description = "老师";
                    if (user.fetchRealname() != null && user.fetchRealname().endsWith("老师")) {
                        description = ""; // 避免重复
                    }
                }
            }

            userinfo.append(user.getProfile().getRealname()).append(" ").append(description).append(" ")
                    .append("(<font color='green'>").append(user.getId()).append("</font>)");
            model.addAttribute("userinfo", userinfo);
        }

        // 生成一个 contextId 用于防止机器人刷接口
        String contextId = RandomUtils.randomString(10);
        ucenterWebCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
        model.addAttribute("contextId", contextId);
        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        model.addAttribute("lastAttemptUserName", getWebRequestContext().getLastUserNameCookieManager().getLastUserNameCookie());

        return "plaza/index";
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

}
