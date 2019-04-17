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

package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 中央电教馆的接入处理类,对方要求不得出现账号注册和绑定流程,市场期望进入供应商列表,权衡之下直接按照学生处理,
 * 新建账号后进入自学页面,反正对他们过来的用户量不做任何期望
 * <p>
 * Created by Jane on 18-6-15.
 */
@Named
public class JmyeduSsoConnector extends AbstractSsoConnector {
    private static final String MAC_NAME = "HmacSHA1";

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String ticket) {

        // Step1 获取网关令牌AccessToken
        String accessToken = getAccessToken(connectionInfo);
        if (StringUtils.isBlank(accessToken)) {
            return MapMessage.errorMessage("get access token  failed:");
        }

        // Step2 验证TICKET获取登录用户信息
        String httpUrl = getTicketValidateUrl() + accessToken;
        return validateTicket(httpUrl, ticket);

    }

    // 给中央电教馆做得特殊处理，如果用户存在的话直接去首页
    // 如果用户不存在，那么自动生成用户绑定后再去首页
    @Override
    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validationResult, CookieManager cookieManager) {
//        User ourUser = null;
//        if (landingSource == null) {
//            // 自动生成用户
//            String userId = String.valueOf(validationResult.get("userId"));
//            String userName = StringUtils.filterEmojiForMysql(String.valueOf(validationResult.get("name")));
//            if (userName != null && userName.length() > 16) {
//                userName = userName.substring(0, 16);
//            }
//
//            NeonatalUser neonatalUser = new NeonatalUser();
//            neonatalUser.setRoleType(getRoleType(String.valueOf(validationResult.get("userCode"))));
//            neonatalUser.setUserType((UserType) validationResult.get("userCode"));
//            neonatalUser.setRealname(userName);
//            neonatalUser.setPassword(RandomGenerator.generatePlainPassword()); // 随机密码
//            neonatalUser.setWebSource(sourceName);
//
//            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
//            if (message.isSuccess()) {
//                // 绑定用户
//                ourUser = (User) message.get("user");
//                try {
//                    thirdPartyService.persistLandingSource(sourceName, userId, userName, ourUser.getId());
//                } catch (Exception e) {
//                    logger.error("Binding gwchina user error, {},{},{}, {}", sourceName, userId, userName, ourUser.getId(), e);
//                }
//            }
//        } else {
//            ourUser = userLoaderClient.loadUser(landingSource.getUserId());
//        }
//
//        if (ourUser != null) {
//            RoleType roleType = RoleType.of(ourUser.getUserType());
//            UserAuthentication ua = userLoaderClient.loadUserAuthentication(ourUser.getId());
//            UtopiaHttpRequestContext context = HttpRequestContextUtils.currentRequestContext();
//            context.saveAuthenticationStates(-1, ourUser.getId(), ua.getPassword(), roleType);
//        }
//
//        return "redirect:/";
        return null;
    }

    private MapMessage validateTicket(String httpUrl, String ticket) {
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("ticket",ticket);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(httpUrl).json(httpParams).execute();

        if (response.hasHttpClientException()) {
            logger.error("Failed to validate ticket:" + ticket + "@cnedu", response.getHttpClientException());
            return MapMessage.errorMessage("Failed to validate ticket");
        }

        if (response.getStatusCode() == 200) {
            Map<String, Object> apiResult = JsonUtils.fromJson(response.getResponseString());
            if (apiResult == null || !apiResult.containsKey("retCode") || !apiResult.get("retCode").equals("000000") || !apiResult.containsKey("userInfo")) {
                return MapMessage.errorMessage("Failed to validate ticket");
            }

            Map<String, Object> userInfo = (Map) apiResult.get("userInfo");

            String userId = SafeConverter.toString(userInfo.get("personId"));
            String name = SafeConverter.toString(userInfo.get("name"));
            String userType = SafeConverter.toString(userInfo.get("userType"));
            // 慧教云 0：学生 -> 3, 1:老师 -> 1, 2:家长 -> 2, 3:机构,4:学校，5：学校工作人员,6:机构工作人员->老师
            if ("0".equals(userType)) {
                userType = "3";
            } else if (!"2".equals(userType)) {
                userType = "1";
            }
            //String dafaultIdentity = String.valueOf(userInfo.get("dafaultIdentity"));
            MapMessage result = MapMessage.successMessage();
            result.add("userId", userId);
            result.add("userName", name);
            result.add("userCode", userType);

            return result;
        }

        return MapMessage.errorMessage("Failed to validate ticket");
    }

    private static UserType getUserCode(String code) {
        switch (code) {
            case "1":
                return UserType.TEACHER;
            case "2":
                return UserType.PARENT;
            default:
                return UserType.STUDENT;
        }
    }

    private static RoleType getRoleType(String code) {
        switch (code) {
            case "1":
                return RoleType.ROLE_TEACHER;
            case "2":
                return RoleType.ROLE_PARENT;
            default:
                return RoleType.ROLE_STUDENT;
        }
    }

    private String getAccessToken(SsoConnections connections) {
        try {
            final String sendUrl = getAccessTokenGetUrl();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("appId", connections.getClientId());
            String curTime = String.valueOf(new Date().getTime());
            httpParams.put("timeStamp", curTime);
            String encryptText = connections.getClientId() + connections.getSecretId() + curTime;
            String keyinfo = HmacSHA1Encrypt(encryptText, connections.getSecretId());
            httpParams.put("keyInfo", keyinfo);
            //httpParams.put("sysCode", connections.getSource());
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(sendUrl).json(httpParams).execute();

            if (response.getStatusCode() == 200) {
                String validateResponse = response.getResponseString();
                Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
                if (apiResult == null || !apiResult.containsKey("retCode") || !apiResult.get("retCode").equals("000000")) {
                    return null;
                } else {
                    Map map = (Map) apiResult.get("tokenInfo");
                    return String.valueOf(map.get("accessToken"));
                }
            }
        } catch (Exception e) {
            logger.error("get access token failed.", e);
        }
        return null;
    }

    public static String HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes("UTF-8");
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes("UTF-8");
        //完成 Mac 操作
        byte[] resultBytes = mac.doFinal(text);
        return bytesToHexString(resultBytes);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int value = src[i];
            int v1 = value / 16;
            int v2 = value % 16;
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static String getTicketValidateUrl() {
        return "http://opengate.huijiaoyun.com/userSession/validaTicket?accessToken=";
    }

    private static String getAccessTokenGetUrl() {
        return "http://opengate.huijiaoyun.com/apigateway/getAccessToken";
    }

    public static void main(String[] args) {
        JmyeduSsoConnector c = new JmyeduSsoConnector();
        String accessToken = c.getAccessToken(SsoConnections.Jmyedu);
        System.out.println(accessToken);
        String httpUrl = getTicketValidateUrl() + accessToken;
        c.validateTicket(httpUrl,"cTRiODgxZGZmZjZjYTRhNWJiZmQ2NzU0ZTRkZDVlYzQzMTU0NDU5ODYzOTIwMQ==");
    }

}