/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.support;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.webmvc.support.authentication.AuthenticationHandler;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.wechat.service.UserService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Xin Xin
 * @since 10/15/15
 */
@Slf4j
@NoArgsConstructor
public class WechatAuthenticationHandler extends AuthenticationHandler {
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private WechatLoaderClient wechatLoaderClient;
    @Inject
    private UserService userService;
    @Inject
    private UserServiceClient userServiceClient;

    @Override
    protected boolean isLoginRequest(String servletPath) {
        return false;
    }

    @Override
    protected boolean isLogoutRequest(String servletPath) {
        return false;
    }

    @Override
    protected boolean processLoginRequest(UtopiaHttpRequestContext context) throws ServletException, IOException {
        return false;
    }

    @Override
    protected boolean processLogoutRequest(UtopiaHttpRequestContext context) throws ServletException, IOException {
        return false;
    }

    @Override
    protected void recordUserLogin(UtopiaHttpRequestContext context) throws ServletException, IOException {

    }

    @Override
    protected void needLogin(UtopiaHttpRequestContext context) throws ServletException, IOException {
        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();

        log.error("chips english pay error, cookie is {}, url is {}", JsonUtils.toJson(request.getCookies()), request.getRequestURI());

        if (request.getRequestURI().contains("/parent/wxpay/pay-order")) {
            String oid = request.getParameter("oid");
            String returnUrl = "/parent/wxpay/pay-order.vpage?oid=" + oid;
            response.sendRedirect("/signup/chips/verifiedlogin.vpage?returnUrl=" + URLEncoder.encode(returnUrl, "UTF-8"));
        }

        request.setAttribute("errmsg", "登录状态已失效,请由微信菜单重新操作～");
        request.getRequestDispatcher("/error.vpage").forward(request, response);
    }
}
