package com.voxlearning.ucenter.controller.connect.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.ucenter.controller.connect.SsoConnectorFactory;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
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
 * 网校 云课堂app sso接口
 * Created by Summer on 2019/3/11
 */
@Controller
@RequestMapping("/")
public class A17XueYktSsoController extends AbstractWebController {

    private final static String SOURCE = "17Yunketang";
    private final static String VALIDATE_URL = "/app/sso/ticket.vpage";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private SsoConnectorFactory ssoConnectorFactory;

    @RequestMapping(value = "ssologin/17ykt.vpage", method = RequestMethod.GET)
    public String ssoLogin(Model model) {
        String data = getRequestString("data");

        Map<String, Object> dataMap = JsonUtils.fromJson(data);
        if (MapUtils.isEmpty(dataMap)) {
            return "redirect:/"; // no return url
        }

        Long userId = SafeConverter.toLong(dataMap.get("uid"));
        String ticket = SafeConverter.toString(dataMap.get("ticket"));
        String returnUrl = SafeConverter.toString(dataMap.get("target"));

        SsoConnections connectionInfo = SsoConnections.get(SOURCE);
        AbstractSsoConnector connector = ssoConnectorFactory.getSsoConnector(connectionInfo);
        if (connector == null) {
            logger.error("17yunketang sso connector not fund, uid {}, data {}", userId, JsonUtils.toJson(data));
            return "redirect:/"; // no return url
        }

        MapMessage result = connector.validateToken(connectionInfo, data);
        if (!result.isSuccess()) {
            logger.error("17yunketang sso validate token error, uid {}, error {}, data {}", userId, result.getInfo(), JsonUtils.toJson(data));
            return "redirect:/"; // no return url
        }


        if (!validateTicket(userId, ticket)) {
            logger.error("17yunketang sso validate ticket error, uid {}, error {}, data {}", userId, result.getInfo(), JsonUtils.toJson(data));
            return "redirect:/";
        }

        User user = raikouSystem.loadUser(SafeConverter.toLong(userId));
        if (user == null) {
            logger.error("17yunketang sso load user null, uid {}, error {}, data {}", userId, result.getInfo(), JsonUtils.toJson(data));
            return "redirect:/";
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
        String validateUrl = getYktBaseSiteUrl() + VALIDATE_URL;

        String url = UrlUtils.buildUrlQuery(validateUrl, MiscUtils.m("uid", userId, "ticket", ticket));
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();

        if (response == null) {
            return false;
        }

        String result = response.getResponseString();
        Map<String, Object> resultMap = JsonUtils.fromJson(result);
        if (resultMap == null) {
            return false;
        }

        return SafeConverter.toBoolean(resultMap.get("success"));
    }

    private String getYktBaseSiteUrl() {
        String url = "";
        if(RuntimeMode.current().le(Mode.TEST)){
            url = "https://17xue-appserver.test.17zuoye.net";
        }else if(RuntimeMode.current() == Mode.STAGING){
            url = "https://17xue-appserver.staging.17zuoye.net";
        }else if(RuntimeMode.current().ge(Mode.PRODUCTION)){
            url = "https://17xue-appserver.17xueba.com";
        }
        return url;
    }
}
