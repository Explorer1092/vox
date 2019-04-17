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

package com.voxlearning.luffy.support;

import com.voxlearning.alps.webmvc.support.authentication.AuthenticationHandler;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Xin Xin
 * @since 10/15/15
 */
@Slf4j
@NoArgsConstructor
public class LuffyAuthenticationHandler extends AuthenticationHandler {

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

    }
}
