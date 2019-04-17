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

package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.utopia.service.user.api.constants.SsoConnections.QQ;

/**
 * @author Rui.Bao
 * @since 2014-10-22 10:26
 */
@Controller
@RequestMapping("/qq")
@Slf4j
@NoArgsConstructor
public class QqController extends AbstractWebController {
    @Inject
    private SsoConnectorFactory ssoConnectorFactory;

    public static final String AUTHORIZE_URL = "https://graph.qq.com/oauth2.0/authorize";
    public static final String RESPONSE_TYPE = "code";
    public static final String REDIRECT_URI = "/qq/authorize.vpage";
    public static final String REDIRECT_URI_TEST = "qq.test.17zuoye.net/qq/authorize.vpage";
    public static final String STATE = "qq_connect_state";

    @RequestMapping(value = "authorizecode.vpage", method = RequestMethod.GET)
    public String getAuthorizeCode() {
        String state = RandomUtils.nextObjectId();
        getWebRequestContext().getCookieManager().setCookieForTopLevelDomain(STATE, DigestUtils.md5Hex(state), 120);
        Map<String, Object> params = MiscUtils.m("client_id", QQ.getClientId(), "response_type", RESPONSE_TYPE,
                "redirect_uri", ProductConfig.getUcenterUrl() + REDIRECT_URI, "state", state);
        if (RuntimeMode.lt(Mode.STAGING)) {
            params.put("redirect_uri", REDIRECT_URI_TEST);
        }
        return "redirect:" + UrlUtils.buildUrlQuery(AUTHORIZE_URL, params);
    }

    @RequestMapping(value = "authorize.vpage", method = RequestMethod.GET)
    public String authorize(Model model) {
        String authorizationCode = getRequestString("code");
        String state = getRequestString("state");
        AbstractSsoConnector connector = ssoConnectorFactory.getSsoConnector(QQ);
        if (connector == null || StringUtils.isBlank(authorizationCode) || StringUtils.isBlank(state)) {
            logger.error("MISS PARAMS, CODE IS [{}], STATE IS [{}]", authorizationCode, state);
            return "redirect:/";
        }
        if (!StringUtils.equals(DigestUtils.md5Hex(state), getWebRequestContext().getCookieManager().getCookie(STATE, null))) {
            logger.error("CSRF CHECK FAILED, STATE RETURNED AND MD5 IS [{}], STATE FROM COOKIE IS [{}]",
                    state, getWebRequestContext().getCookieManager().getCookie(STATE, null));
            return "redirect:/";
        }
        MapMessage validateResult = connector.validateToken(QQ, authorizationCode);
        if (!validateResult.isSuccess()) {
            return "redirect:/";
        }

        String openId = ConversionUtils.toString(validateResult.get("openId"));
        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(QQ.getSource(), openId);
        if (landingSource != null) {
            // 用户存在，以一起作业网用户身份登录
            Long userId = landingSource.getUserId();
            UserAuthentication user = userLoaderClient.loadUserAuthentication(userId);
            RoleType roleType = RoleType.of(user.getUserType().getType());
            getWebRequestContext().saveAuthenticationStates(-1, user.getId(), user.getPassword(), roleType);
            return "redirect:/";
        } else {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("source", QQ.getSource());
            dataMap.put("sourceUid", ConversionUtils.toString(validateResult.get("openId")));
            dataMap.put("userName", ConversionUtils.toString(validateResult.get("nickname")));
            dataMap.put("sourceLogo", ConversionUtils.toString(validateResult.get("img")));
            dataMap.put("sourceName", ConversionUtils.toString(validateResult.get("nickname")));

            String mckey = "sso" + RandomUtils.randomString(24);
            ucenterWebCacheSystem.CBS.unflushable.set(mckey, 1800, dataMap);

            model.addAttribute("dataKey", mckey);
            model.addAttribute("sourceLogo", validateResult.get("img"));
            model.addAttribute("sourceName", validateResult.get("nickname"));

            return "/open/accountbind";
        }
    }
}
