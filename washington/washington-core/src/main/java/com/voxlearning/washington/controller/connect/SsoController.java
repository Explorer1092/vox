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

package com.voxlearning.washington.controller.connect;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 外部系统对接用Controller Class
 * Created by Alex on 14-10-15.
 */
@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
@Deprecated
public class SsoController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private SsoConnectorFactory ssoConnectorFactory;

    @RequestMapping(value = "ssologin/{source}.vpage", method = RequestMethod.GET)
    public String ssoLogin(@PathVariable("source") String source, Model model) {
        String token = getRequestString("token");
        String returnUrl = getRequestString("returnUrl");
        SsoConnections connectionInfo = SsoConnections.get(source);
        AbstractSsoConnector connector = ssoConnectorFactory.getSsoConnector(connectionInfo);
        if (connector == null) {
            return "redirect:/";
        }

        // 验证链接是否伪造
        MapMessage validateResult = connector.validateToken(connectionInfo, token);
        if (!validateResult.isSuccess()) {
            return "redirect:/";
        }

        // 检查用户是否存在
        String sourceName = connectionInfo.getSource();
        String sourceUid = String.valueOf(validateResult.get("userId"));
        LandingSource landingSource = thirdPartyLoaderClient.loadLandingSource(sourceName, sourceUid);

        // ================================================================================
        // 本处允许各个第三方的实现类做自己的特殊处理，如果走共用逻辑，那么直接返回NULL既可
        String userBindingResult = connector.processUserBinding(landingSource, sourceName, validateResult, getCookieManager());
        if (StringUtils.isNotBlank(userBindingResult)) {
            return userBindingResult;
        }
        // ================================================================================

        if (landingSource != null) {
            // 用户存在，以一起作业网用户身份登录
            Long userId = landingSource.getUserId();
            User user = raikouSystem.loadUser(userId);
            RoleType roleType = RoleType.of(user.getUserType());
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);
            if (StringUtils.isNotBlank(returnUrl)) {
                return "redirect:" + returnUrl;
            } else {
                return "redirect:/";
            }
        }

        // TODO 用户不存在进入用户绑定功能
        //source：第三方网站或系统的识别名
        //sourceUid：第三方网站或系统的用户ID
        //userType:用户身份，1：老师，3:学生
        //userName:用户的真实姓名 （可以作为初始值预置到页面上）
        //tel：手机号码 （可以作为初始值预置到页面上）
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("source", source);
        dataMap.put("sourceUid", String.valueOf(validateResult.get("userId")));
        dataMap.put("userType", String.valueOf(validateResult.get("userCode")));
        if (validateResult.get("userName") != null) {
            dataMap.put("userName", String.valueOf(validateResult.get("userName")));
        } else {
            dataMap.put("userName", "");
        }
        if (validateResult.get("userMobile") != null) {
            dataMap.put("userMobile", String.valueOf(validateResult.get("userMobile")));
        } else {
            dataMap.put("userMobile", "");
        }
        dataMap.put("sourceLogo", connectionInfo.getLogoUrl());
        dataMap.put("sourceName", connectionInfo.getName());

        String mckey = "sso" + RandomUtils.randomString(24);
        washingtonCacheSystem.CBS.unflushable.set(mckey, 1800, dataMap);

        model.addAttribute("dataKey", mckey);
        model.addAttribute("sourceLogo", connectionInfo.getLogoUrl());
        model.addAttribute("sourceName", connectionInfo.getName());

        return "/open/accountbind";
    }

    @RequestMapping(value = "/ssologinerror.vpage", method = RequestMethod.GET)
    public String ssoLoginError(Model model) {
        String dataKey = getRequestString("dataKey");
        model.addAttribute("dataKey", dataKey);
        model.addAttribute("error", true);
        // 莫名其妙有调用 datakey=xxxx的日志，估计是用户手工操作的，暂时就在这里加上容错处理吧
        if (StringUtils.isNotBlank(dataKey)) {
            CacheObject<Map> cacheObject = washingtonCacheSystem.CBS.unflushable.get(dataKey);
            if (cacheObject != null) {
                Map dataMap = cacheObject.getValue();
                if (dataMap != null) {
                    model.addAttribute("sourceLogo", dataMap.get("sourceLogo"));
                    model.addAttribute("sourceName", dataMap.get("sourceName"));
                }
            }
        }
        return "/open/accountbind";
    }

    @RequestMapping(value = "/ssologinbind.vpage", method = RequestMethod.GET)
    public String ssoLoginBind(Model model) {
        String dataKey = getRequestString("dataKey");
        model.addAttribute("dataKey", dataKey);
        if (StringUtils.isNotBlank(dataKey)) {
            CacheObject<Map> cacheObject = washingtonCacheSystem.CBS.unflushable.get(dataKey);
            if (cacheObject != null) {
                Map dataMap = cacheObject.getValue();
                if (dataMap != null) {
                    model.addAttribute("sourceLogo", dataMap.get("sourceLogo"));
                    model.addAttribute("sourceName", dataMap.get("sourceName"));
                }
            }
        }
        return "/open/accountbind";
    }
}
