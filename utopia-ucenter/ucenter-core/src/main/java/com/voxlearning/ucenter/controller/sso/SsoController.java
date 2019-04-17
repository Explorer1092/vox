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

package com.voxlearning.ucenter.controller.sso;

import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.ucenter.service.SsoServiceImpl;
import com.voxlearning.ucenter.support.constants.SsoConstants;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 23/12/2015.
 */
@Controller
@RequestMapping(value = "/sso")
@Slf4j
public class SsoController extends AbstractSsoController {

    @Inject private SsoServiceImpl ssoService;

    /**
     * SSO入口,如果请求带ucenter的登录cookie,则生成ticket并跳转返回第三方提供的target
     * 如果请求不带ucenter登录cookie,则请求被拦截到登录页,登录成功后跳到这里继续
     */
    @RequestMapping(value = "ticket.vpage", method = RequestMethod.GET)
    public String sign() {
        String dataJson = getRequestParameter(SsoConstants.DATA, null);
        Map<String, Object> data = JsonUtils.fromJson(dataJson);
        String target = SafeConverter.toString(data.get(SsoConstants.TARGET));
        String appKey = SafeConverter.toString(data.get(SsoConstants.APP_KEY));

        try {
            Long userId = getWebRequestContext().getUserId();
            if (null == userId) {
                //未登录,跳去登录
                String returnUrl = "/sso/ticket.vpage?" + getRequest().getQueryString();
                if (returnUrl.endsWith("&ref=login")) {// 特殊处理首页登录时开发环境问题
                    returnUrl = returnUrl.substring(0, returnUrl.length() - 10);
                    return "redirect:/login.vpage?returnURL=" + URLEncoder.encode(returnUrl, "UTF-8") + "&ref=login";
                }
                return "redirect:/login.vpage?returnURL=" + URLEncoder.encode(returnUrl, "UTF-8");
            }

            String ticket = generateTicket(appKey);
            if (StringUtils.isBlank(ticket)) {
                return "redirect:/";
            }

            return "redirect:" + buildSsoUrl(target, appKey, ticket);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "redirect:/";
        }
    }

    /**
     * 验证ticket,验证通过以后,返回uid给第三方
     */
    @RequestMapping(value = "validate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateTicket() {
        String dataJson = getRequestParameter(SsoConstants.DATA, null);
        return ssoService.validateTicket(dataJson);
    }

    private String generateTicket(String appKey) {
        Long userId = getWebRequestContext().getUserId();
        if (null == userId || StringUtils.isBlank(appKey)) {
            return null;
        }

        //User user = userLoaderClient.loadUser(userId);
        UserAuthentication user = userLoaderClient.loadUserAuthentication(userId);

        if (null == user) {
            return null;
        }

        String sso_ticket_secret_key = ConfigManager.instance().getCommonConfig().getConfigs().get("sso_ticket_secret_key");
        if (sso_ticket_secret_key == null) {
            throw new ConfigurationException("No 'sso_ticket_secret_key' configured");
        }

        String ticket = appKey + ":" + userId + ":" + user.getPassword() + ":" + Instant.now().toEpochMilli();
        String encryptedTicket = AesUtils.encryptHexString(sso_ticket_secret_key, ticket);
        ucenterWebCacheSystem.CBS.flushable.add(SsoServiceImpl.generateTicketCacheKey(encryptedTicket), SsoConstants.TICKET_EXPIRE_TIMESTAMP / 1000, ticket);

        return encryptedTicket;
    }

    private String buildSsoUrl(String target, String appKey, String ticket) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
//        target = URLDecoder.decode(target, "UTF-8");

        // TODO 这块可能会出现参数覆盖的问题，需要商量下是否应该更改
        //解析出target中的QueryString
        if (target.lastIndexOf("?") > 0) {
            String queryStr = target.substring(target.lastIndexOf("?") + 1);
            String[] group = queryStr.split("&");
            for (String g : group) {
                String[] kv = g.split("=");
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
        }

        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        params.put(SsoConstants.APP_KEY, appKey);
        params.put(SsoConstants.TICKET, ticket);
        params.put(SsoConstants.TIMESTAMP, timestamp);

        VendorApps app = vendorLoaderClient.loadVendorAppsIncludeDisabled().values()
                .stream()
                .filter(t -> t.isVisible(RuntimeMode.current().getLevel()) && t.getAppKey().equals(appKey))
                .findFirst()
                .orElse(null);
        if (app == null) {
            return null;
        }

        String sign = DigestSignUtils.signMd5(params, app.getSecretKey());
        params.put(SsoConstants.SIGNATURE, sign);

        return UrlUtils.buildUrlQuery(target, MiscUtils.map("data", JsonUtils.toJson(params)));
    }

}
