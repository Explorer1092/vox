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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Map;

/**
 * 一起学17xue sso controller
 *
 * @author changyuan
 * @since 2017/6/28
 */
@Controller
@RequestMapping("/")
public class A17XueSsoController extends AbstractWebController {

    private final static String A17XUE_SOURCE = "17xue";
    private final static String VALIDATE_URL = "/homework/validate.vpage";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private SsoConnectorFactory ssoConnectorFactory;

    @RequestMapping(value = "ssologin/17xue.vpage", method = RequestMethod.GET)
    public String ssoLogin() {
        String data = getRequestString("data");

        Map<String, Object> dataMap = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(dataMap)) {
            return "redirect:/"; // no return url
        }

        Long userId = SafeConverter.toLong(dataMap.get("uid"));
        String ticket = SafeConverter.toString(dataMap.get("ticket"));
        String returnUrl = SafeConverter.toString(dataMap.get("target"));

        SsoConnections connectionInfo = SsoConnections.get(A17XUE_SOURCE);
        AbstractSsoConnector connector = ssoConnectorFactory.getSsoConnector(connectionInfo);
        if (connector == null) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.get17XueTeacherUrl() + "/homework/error.vpage", MiscUtils.m(
                    "uid", userId,
                    "tag", "No sso connector found",
                    "data", data
            ));
            return "redirect:" + redirectUrl;
        }

        MapMessage result = connector.validateToken(connectionInfo, data);
        if (!result.isSuccess()) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.get17XueTeacherUrl() + "/homework/error.vpage", MapUtils.m(
                    "uid", userId,
                    "tag", "ERROR_VALIDATE_TOKEN",
                    "data", data
            ));
            logger.error("17xue validate token failed for data {} : {}", data, result.getInfo());
            return "redirect:" + redirectUrl;
        }

        if (!validateTicket(userId, ticket)) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.get17XueTeacherUrl() + "/homework/error.vpage", MapUtils.m(
                    "uid", userId,
                    "tag", "ERROR_VALIDATE_TICKET",
                    "data", data
            ));
            return "redirect:" + redirectUrl;
        }

        User user = raikouSystem.loadUser(SafeConverter.toLong(userId));
        if (user == null) {
            String redirectUrl = UrlUtils.buildUrlQuery(ProductConfig.get17XueTeacherUrl() + "/homework/error.vpage", MapUtils.m(
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
        String validateUrl = ProductConfig.get17XueTeacherUrl() + VALIDATE_URL;

        String url = UrlUtils.buildUrlQuery(validateUrl, MapUtils.m("uid", userId, "ticket", ticket));
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
