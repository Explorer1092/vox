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

package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.ucenter.controller.connect.impl.timeepwsdl.LoginWs;
import com.voxlearning.ucenter.controller.connect.impl.timeepwsdl.LoginWsPortType;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * @author changyuan
 * @since 2016/3/16
 */
@Controller
@RequestMapping("/")
public class TimeepSsoController extends AbstractWebController {

    private final static String TIMEEP_SOURCE = "timeep";

    @Inject private SsoConnectorFactory ssoConnectorFactory;

    // "wpf_fly", "b228f9810daaea5887b9d202aae1a8f8"
    @RequestMapping(value = "ssologin/timeep.vpage", method = RequestMethod.GET)
    public String ssoLogin(Model model) {
        String username = getRequestString("username");
        String password = getRequestString("password");
        String timeepUid = getRequestString("userId");

        SsoConnections connectionInfo = SsoConnections.get(TIMEEP_SOURCE);
        AbstractSsoConnector connector = ssoConnectorFactory.getSsoConnector(connectionInfo);
        if (connector == null) {
            return "redirect:/";
        }

        // 验证token
        if (!validateToken(username, password)) {
            return "redirect:/";
        }

        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(connectionInfo.getSource(), timeepUid);
        if (landingSource != null) {// 因为是导入用户，所以必须有landing source数据
            // 用户存在，以一起作业网用户身份登录
            Long userId = landingSource.getUserId();
            UserAuthentication user = userLoaderClient.loadUserAuthentication(userId);
            RoleType roleType = RoleType.of(user.getUserType().getType());
            getWebRequestContext().saveAuthenticationStates(-1, user.getId(), user.getPassword(), roleType);
        }
        return "redirect:/";
    }

    private boolean validateToken(String username, String password) {
        // 重试机制
        int num = 0;
        Exception exception = null;
        while (num < 3) {// 最多重试3次
            try {
                LoginWs loginWs = new LoginWs();
                LoginWsPortType loginWsHttpPort = loginWs.getLoginWsHttpPort();
                return loginWsHttpPort.checkLoginInfo(username, password);
            } catch (Exception e) {
                exception = e;
            }
            num++;
            try {
                Thread.sleep(500);// 重试间隔500ms
            } catch (InterruptedException ignored) {

            }
        }
        logger.error("Retry 3 times failed for validating timeep user and pw. ", exception);
        return false;
    }
}
