/*
 *
 *  * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *  *
 *  * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *  *
 *  * NOTICE: All information contained herein is, and remains the property of
 *  * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 *  * and technical concepts contained herein are proprietary to Shanghai Sunny
 *  * Education, Inc. and its suppliers and may be covered by patents, patents
 *  * in process, and are protected by trade secret or copyright law. Dissemination
 *  * of this information or reproduction of this material is strictly forbidden
 *  * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 *
 */

package com.voxlearning.ucenter.controller.connect.controller;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.ucenter.controller.connect.SsoConnectorFactory;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author changyuan
 * @since 2016/10/17
 */
@Controller
@RequestMapping("/")
public class USTalkSsoController extends AbstractWebController {

    private final static String USTALK_SOURCE = "ustalk";
    private final static String VALIDATE_URL = "/homework/validate.vpage";

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private SsoConnectorFactory ssoConnectorFactory;

    @RequestMapping(value = "ssologin/ustalk.vpage", method = RequestMethod.GET)
    public String ssoLogin(Model model) {
        String data = getRequestString("data");

        Map<String, Object> dataMap = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(dataMap)) {
            return "redirect:/"; // no return url
        }

        Long userId = SafeConverter.toLong(dataMap.get("uid"));
        String ticket = SafeConverter.toString(dataMap.get("ticket"));
        String returnUrl = SafeConverter.toString(dataMap.get("target"));

        SsoConnections connectionInfo = SsoConnections.get(USTALK_SOURCE);
        AbstractSsoConnector connector = ssoConnectorFactory.getSsoConnector(connectionInfo);
        if (connector == null) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.getUSTalkUrl() + "/homework/error.vpage", MiscUtils.m(
                    "uid", userId,
                    "tag", "No sso connector found",
                    "data", data
            ));
            return "redirect:" + redirectUrl;
        }

        MapMessage result = connector.validateToken(connectionInfo, data);
        if (!result.isSuccess()) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.getUSTalkUrl() + "/homework/error.vpage", MiscUtils.m(
                    "uid", userId,
                    "tag", "ERROR_VALIDATE_TOKEN",
                    "data", data
            ));
            logger.error("ustalk validate token failed for data {} : {}", data, result.getInfo());
            return "redirect:" + redirectUrl;
        }


        if (!validateTicket(userId, ticket)) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.getUSTalkUrl() + "/homework/error.vpage", MiscUtils.m(
                    "uid", userId,
                    "tag", "ERROR_VALIDATE_TICKET",
                    "data", data
            ));
            return "redirect:" + redirectUrl;
        }

        User user = raikouSystem.loadUser(SafeConverter.toLong(userId));
        if (user == null) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.getUSTalkUrl() + "/homework/error.vpage", MiscUtils.m(
                    "uid", userId,
                    "tag", "ERROR_LOAD_USER",
                    "data", data
            ));
            return "redirect:" + redirectUrl;
        }

        RoleType roleType = RoleType.of(user.getUserType());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);
        if (StringUtils.isNotEmpty(returnUrl)) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/"; // no return url
    }

    private boolean validateTicket(Long userId, String ticket) {
        String validateUrl = ProductConfig.getUSTalkUrl() + VALIDATE_URL;

        String url = UrlUtils.buildUrlQuery(validateUrl, MiscUtils.m("uid", userId, "ticket", ticket));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();

        if (response == null) {
            return false;
        }

        String result = response.getResponseString();
        Map<String, Object> resultMap = JsonUtils.fromJsonToMap(result, String.class, Object.class);
        if (resultMap == null) {
            return false;
        }

        return SafeConverter.toBoolean(resultMap.get("success"));
    }

}
