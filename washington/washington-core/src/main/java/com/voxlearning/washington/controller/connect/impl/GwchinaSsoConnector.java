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

package com.voxlearning.washington.controller.connect.impl;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.washington.controller.connect.AbstractSsoConnector;
import org.apache.http.message.BasicHeader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 14-11-21.
 */
@Named
@Deprecated
public class GwchinaSsoConnector extends AbstractSsoConnector {

    private static final String USER_GET_URL_TEST = "http://testapi.gwchina.cn:10080/yqzyw/getuserinfo";
    private static final String USER_GET_URL_PROD = "http://api.gwchina.cn/yqzyw/getuserinfo";

    @Inject private RaikouSystem raikouSystem;

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // Step1 生成验证Token用的SIG
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("token", token);
        sigParams.put("app_key", connectionInfo.getClientId());
        String sig = DigestSignUtils.signMd5(sigParams, SsoConnections.GWCHINA.getSecretId());

        // Step2 调用绿网API获取用户信息
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("token", token);
        httpParams.put("sig", sig);

        String sendUrl = USER_GET_URL_TEST;
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            sendUrl = USER_GET_URL_PROD;
        }
        // 这块有个坑
        // 当只指定Content-Type: application/json是能正常返回json的
        // 但是一旦Content-Type加上其他的约定，比如Content-Type: application/json; charset=UTF-8，就会返回xml了
        // 明显是第三方处理2了
        String validateResponse = HttpRequestExecutor.defaultInstance().post(sendUrl)
                .headers(new BasicHeader("Content-Type", "application/json"))
                .json(httpParams)
                .execute()
                .getResponseString();
        Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
        if (apiResult == null || !"0".equals(String.valueOf(apiResult.get("ret"))) || apiResult.get("id") == null) {
            logger.warn("token validate api response:{}", validateResponse);
            return MapMessage.errorMessage("token validate failed!");
        }

        String userId = String.valueOf(apiResult.get("id"));
        String userName = String.valueOf(apiResult.get("user_name"));

        MapMessage result = MapMessage.successMessage();
        result.add("userId", userId);
        result.add("userName", userName);
        return result;
    }

    // 给绿网做得特殊处理，如果用户存在的话直接去自学页面
    // 如果用户不存在，那么自动生成学生用户绑定后再去自学页面
    @Override
    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validationResult, CookieManager cookieManager) {
        User ourUser = null;
        if (landingSource == null) {
            // 自动生成学生用户
            String userId = String.valueOf(validationResult.get("userId"));
            String userName = StringHelper.filterEmojiForMysql(String.valueOf(validationResult.get("userName")));
            if (userName.length() > 16) {
                userName = userName.substring(0, 16);
            }

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_STUDENT);
            neonatalUser.setUserType(UserType.STUDENT);
            neonatalUser.setRealname(userName);
            neonatalUser.setPassword("123456"); // 固定密码
            neonatalUser.setWebSource(sourceName);

            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                return null;
            }

            // 绑定用户
            ourUser = (User) message.get("user");
            try {
                thirdPartyService.persistLandingSource(sourceName, userId, userName, ourUser.getId());
            } catch (Exception e) {
                logger.error("Binding gwchina user error, {},{},{}, {}", sourceName, userId, userName, ourUser.getId(), e);
                return null;
            }
        } else {
            ourUser = raikouSystem.loadUser(landingSource.getUserId());
        }

        if (ourUser == null) {
            return null;
        }

        RoleType roleType = RoleType.of(ourUser.getUserType());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(ourUser.getId());
        HttpRequestContextUtils.currentRequestContext()
                .saveAuthenticationStates(-1, ourUser.getId(), ua.getPassword(), roleType);
        return "redirect:/student/learning/gwchina.vpage";
    }

    public static void main(String[] args) {
        try {
            final String sendUrl = "http://testapi.gwchina.cn:10080/yqzyw/gettoken";
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("user_name", "cslssw01");
            String r = HttpRequestExecutor.defaultInstance().post(sendUrl).json(httpParams).execute().getResponseString();
            System.out.println(r);

//            final String sendUrl = "http://testapi.gwchina.cn:10080/yqzyw/getuserinfo";
//            String token = "134C11AF-8C01-E563-5E61-0892521E083F";
//            Map<String, String> sigParam = new HashMap<>();
//            sigParam.put("token", token);
//            sigParam.put("app_key", SsoConnections.GWCHINA.getClientId());
//            String sig = DigestSignUtils.signMd5(sigParam, SsoConnections.GWCHINA.getSecretId());
//
//            Map<String, String> httpParam = new HashMap<>();
//            httpParam.put("token", token);
//            httpParam.put("sig", sig);
//
//            HttpUtils.HttpResponse response1 = HttpUtils.httpPostJson(sendUrl, JsonUtils.toJson(httpParam), "application/json");
//            System.out.println(response1.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
