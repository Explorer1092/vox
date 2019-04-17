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

package com.voxlearning.ucenter.support.controller;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.cache.UcenterWebCacheSystem;
import com.voxlearning.ucenter.service.helper.SmsServiceHelper;
import com.voxlearning.ucenter.support.UcenterMessageSender;
import com.voxlearning.ucenter.support.context.UcenterRequestContext;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.*;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import lombok.Getter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * abstract controller for user center service
 *
 * @author changyuan.liu
 * @since 2015.12.6
 */
public class AbstractController extends SpringContainerSupport {

    @Inject protected UcenterWebCacheSystem ucenterWebCacheSystem;

    @Inject @Getter private GrayFunctionManagerClient grayFunctionManagerClient;

    @Getter @Inject private RaikouSystem raikouSystem;
    @Getter @Inject protected PageBlockContentServiceClient pageBlockContentServiceClient;
    @Getter @Inject protected UserLoaderClient userLoaderClient;
    @Getter @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Getter @Inject protected ParentLoaderClient parentLoaderClient;
    @Getter @Inject protected StudentLoaderClient studentLoaderClient;
    @Getter @Inject protected ResearchStaffLoaderClient researchStaffLoaderClient;

    @Inject protected TinyGroupLoaderClient tinyGroupLoaderClient;
    @Inject protected TinyGroupServiceClient tinyGroupServiceClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject protected GroupServiceClient groupServiceClient;
    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected ClazzServiceClient clazzServiceClient;
    @Inject protected SmsServiceClient smsServiceClient;
    @Inject protected SmsServiceHelper smsServiceHelper;
    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject protected UserServiceClient userServiceClient;
    @Inject protected UserAttributeLoaderClient userAttributeLoaderClient;
    @Inject protected UserAttributeServiceClient userAttributeServiceClient;
    @Inject protected ThirdPartyLoaderClient thirdPartyLoaderClient;
    @Inject protected TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject protected TeacherServiceClient teacherServiceClient;
    @Inject protected TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject protected StudentSystemClazzServiceClient studentSystemClazzServiceClient;
    @Inject protected ParentServiceClient parentServiceClient;
    @Inject protected CdnResourceUrlGenerator cdnResourceUrlGenerator;
    @Inject protected AppMessageServiceClient appMessageServiceClient;
    @Inject protected StudentServiceClient studentServiceClient;
    @Inject protected CommonConfigServiceClient commonConfigServiceClient;
    @Inject protected AsyncFootprintServiceClient asyncFootprintServiceClient;
    @Inject protected UserLoginServiceClient userLoginServiceClient;

    // 用于通知第三方清理session
    // 暂时先依赖进来，以后看看有没有什么好的办法decouple
    @Inject protected VendorLoaderClient vendorLoaderClient;
    @Inject protected VendorServiceClient vendorServiceClient;

    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    // 其他业务方发送消息
    @Inject protected UcenterMessageSender ucenterMessageSender;

    @Inject protected WechatServiceClient wechatServiceClient;
    @Inject protected BadWordCheckerClient badWordCheckerClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }


    public UcenterRequestContext getWebRequestContext() {
        return (UcenterRequestContext) DefaultContext.get();
    }

    public boolean onBeforeControllerMethod() {
        // 验证用户是否已经更改过密码，如已更改过需要重新登录
        // 如果是移动端过来的请求，不做这个验证
        if (isMobileRequest(getRequest())) {
            return true;
        }
        User user = currentUser();
        if (null != user) {
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            if (!ua.getPassword().equals(getWebRequestContext().getSaltedPassword())) {
                getWebRequestContext().cleanupAuthenticationStates();
                try {
                    String currentUrl = getRequest().getRequestURL() + (null == getRequest().getQueryString() ? "" : "?" + getRequest().getQueryString());
                    getResponse().sendRedirect(UrlUtils.buildUrlQuery(ProductConfig.getMainSiteBaseUrl(), MiscUtils.map("returnURL", currentUrl)));
                    return false;
                } catch (IOException ignored) {
                }
            }
        }

        return true;
    }

    /* ======================================================================================
       以下代码负责 Request & Response
       ====================================================================================== */
    protected HttpServletRequest getRequest() {
        return getWebRequestContext().getRequest();
    }

    protected String getRequestParameter(String key, String def) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? def : v;
    }

    protected String getRequestString(String key) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? "" : v;
    }

    protected String getRequestStringCleanXss(String key) {
        //then URLEncodedUtils can be used to parse query-string ?
        String v = getRequest().getParameter(key);
        return v == null ? "" : StringUtils.cleanXSS(v);
    }

    protected boolean getRequestBool(String name) {
        return ConversionUtils.toBool(getRequest().getParameter(name));
    }

    protected boolean getRequestBool(String name, boolean def) {
        return SafeConverter.toBoolean(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name, long def) {
        return ConversionUtils.toLong(getRequest().getParameter(name), def);
    }

    protected long getRequestLong(String name) {
        return ConversionUtils.toLong(getRequest().getParameter(name));
    }

    protected int getRequestInt(String name, int def) {
        return ConversionUtils.toInt(getRequest().getParameter(name), def);
    }

    protected int getRequestInt(String name) {
        return ConversionUtils.toInt(getRequest().getParameter(name));
    }

    protected double getRequestDouble(String name) {
        return ConversionUtils.toDouble(getRequest().getParameter(name), 0);
    }

    protected HttpServletResponse getResponse() {
        return getWebRequestContext().getResponse();
    }

    protected boolean isRequestPost() {
        return getRequest().getMethod().equals("POST");
    }

    protected boolean isRequestGet() {
        //then URLEncodedUtils can be used to parse query-string ?
        return getRequest().getMethod().equals("GET");
    }

    protected boolean isRequestAjax() {
        return HttpRequestContextUtils.isRequestAjax(getRequest());
    }

    protected CookieManager getCookieManager() {
        return getWebRequestContext().getCookieManager();
    }

    /* ======================================================================================
       以下代码负责常用的数据，比如当前用户、地理信息等
       ====================================================================================== */

    protected Long currentUserId() {
        return getWebRequestContext().getUserId();
    }

    protected User currentUser() {
        return getWebRequestContext().getCurrentUser();
    }

    protected List<RoleType> currentUserRoleTypes() {
        return getWebRequestContext().getRoleTypes();
    }

    protected Teacher currentTeacher() {
        return getWebRequestContext().getCurrentTeacher();
    }

    protected TeacherDetail currentTeacherDetail() {
        return getWebRequestContext().getCurrentTeacherDetail();
    }

    protected User currentParent() {
        return getWebRequestContext().getCurrentUser();
    }

    protected User currentStudent() {
        return getWebRequestContext().getCurrentStudent();
    }

    protected StudentDetail currentStudentDetail() {
        return getWebRequestContext().getCurrentStudentDetail();
    }

    protected ResearchStaff currentResearchStaff() {
        return getWebRequestContext().getCurrentResearchStaff();
    }

    protected ResearchStaffDetail currentResearchStaffDetail() {
        return getWebRequestContext().getCurrentResearchStaffDetail();
    }

    // mobile related

    protected boolean isMobileRequest(HttpServletRequest request) {
        return isIOSRequest(request) || isAndroidRequest(request);
    }

    protected boolean isIOSRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.containsIgnoreCase(userAgent, "iOS") ||
                StringUtils.containsIgnoreCase(userAgent, "iPhone") ||
                StringUtils.containsIgnoreCase(userAgent, "iPad") ||
                StringUtils.containsIgnoreCase(userAgent, "iPod");
    }

    protected boolean isAndroidRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.containsIgnoreCase(userAgent, "Android");
    }

}
