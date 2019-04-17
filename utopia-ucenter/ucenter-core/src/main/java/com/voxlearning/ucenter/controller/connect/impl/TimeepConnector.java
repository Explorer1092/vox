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

package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;

import javax.inject.Named;

/**
 * @author changyuan
 * @since 2016/3/15
 */
// TODO 未来考虑直接用soap的方式调用
@Named
public class TimeepConnector extends AbstractSsoConnector {
    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        return null;
    }

    @Override
    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validateResult, CookieManager cookieManager) {
        return "redirect:/";// 跳到主页
    }

//    public static void main(String[] args) throws MalformedURLException {
//
//        LoginWs loginWs = new LoginWs();
//
//        LoginWsPortType loginWsHttpPort = loginWs.getLoginWsHttpPort();
//
//        boolean result = loginWsHttpPort.checkLoginInfo("wpf_fly", "b228f9810daaea5887b9d202aae1a8f8");
//
//        System.out.println(result);
//    }
}
