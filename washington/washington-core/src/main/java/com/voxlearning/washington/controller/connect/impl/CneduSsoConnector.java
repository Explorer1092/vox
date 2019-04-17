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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
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
 * Created by Alex on 15-3-10.
 */
@Named
@Deprecated
public class CneduSsoConnector extends AbstractSsoConnector {
    private static final String MAC_NAME = "HmacSHA1";

    @Inject private RaikouSystem raikouSystem;

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // Step1 验证TICKET是否有效
        String session = validateTicket(token);
        if (StringUtils.isBlank(session)) {
            return MapMessage.errorMessage("ticket validate failed:" + token);
        }

        // Step2 获取AccessToken
        String accessToken = getAccessToken(connectionInfo);
        if (StringUtils.isBlank(accessToken)) {
            return MapMessage.errorMessage("get access token  failed:");
        }

        // Step3 获取用户信息
        String sendUrl = getUserInfoGetUrl() + session + "?token=" + accessToken;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(sendUrl).execute();
        if (response.getStatusCode() == 200) {
            Map<String, Object> apiResult = JsonUtils.fromJson(response.getResponseString());
            if (apiResult == null || !apiResult.containsKey("result") || !apiResult.get("result").equals("000000") || !apiResult.containsKey("userinfo")) {
                return MapMessage.errorMessage("failed to get user info with result:" + response.getResponseString());
            }

            Map<String, Object> userInfo = (Map) apiResult.get("userinfo");

            String userId = String.valueOf(userInfo.get("personid"));
            String mobile = "";
            if (userInfo.containsKey("mobnum")) {
                mobile = String.valueOf(userInfo.get("mobnum"));
            }
            String name = String.valueOf(userInfo.get("name"));
            String userType = String.valueOf(userInfo.get("usertype"));
            if ("0".equals(userType)) {
                userType = "3";
            } else if ("2".equals(userType)) {
                userType = "2";
            } else {
                userType = "1";
            }

            MapMessage result = MapMessage.successMessage();
            result.add("userId", userId);
            result.add("userCode", userType);
            result.add("userName", name);
            result.add("userMobile", mobile);

            return result;
        }

        return MapMessage.errorMessage("failed to get user info with result:" + String.valueOf(response));
    }

    // 给中央电教馆做得特殊处理，如果用户存在的话直接去首页
    // 如果用户不存在，那么自动生成用户绑定后再去自学页面
    @Override
    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validationResult, CookieManager cookieManager) {
        User ourUser = null;
        if (landingSource == null) {
            // 自动生成学生用户
            String userId = String.valueOf(validationResult.get("userId"));
            String userName = StringUtils.filterEmojiForMysql(String.valueOf(validationResult.get("userName")));
            if (userName != null && userName.length() > 16) {
                userName = userName.substring(0, 16);
            }

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_STUDENT);
            neonatalUser.setUserType(UserType.STUDENT);
            neonatalUser.setRealname(userName);
            neonatalUser.setPassword("123456"); // 固定密码
            neonatalUser.setWebSource(sourceName);

            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (message.isSuccess()) {
                // 绑定用户
                ourUser = (User) message.get("user");
                try {
                    thirdPartyService.persistLandingSource(sourceName, userId, userName, ourUser.getId());
                } catch (Exception e) {
                    logger.error("Binding gwchina user error, {},{},{}, {}", sourceName, userId, userName, ourUser.getId(), e);
                }
            }
        } else {
            ourUser = raikouSystem.loadUser(landingSource.getUserId());
        }

        if (ourUser != null) {
            RoleType roleType = RoleType.of(ourUser.getUserType());
            UtopiaHttpRequestContext context = HttpRequestContextUtils.currentRequestContext();
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(ourUser.getId());
            context.saveAuthenticationStates(-1, ourUser.getId(), ua.getPassword(), roleType);
        }

        return "redirect:/guest/learning/index.vpage";
    }

    private String validateTicket(String ticket) {
        String httpUrl = getTicketValidateUrl() + ticket;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(httpUrl).execute();
        if (response.hasHttpClientException()) {
            logger.error("Failed to validate ticket:" + ticket + "@fjedu", response.getHttpClientException());
            return null;
        }
        if (response.getStatusCode() == 200) {
            String responseStr = response.getResponseString();
            int startIndex = responseStr.indexOf("<cas:user>");
            int endIndex = responseStr.indexOf("</cas:user>");
            if (startIndex > 0 && endIndex > 0) {
                return responseStr.substring(startIndex + "<cas:user>".length(), endIndex);
            }
        }
        return null;
    }

    private String getAccessToken(SsoConnections connections) {
        try {
            final String sendUrl = getAccessTokenGetUrl();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("appid", connections.getClientId());
            String curTime = String.valueOf(new Date().getTime());
            httpParams.put("timestamp", curTime);
            String encryptText = connections.getClientId() + connections.getSecretId() + curTime;
            String keyinfo = HmacSHA1Encrypt(encryptText, connections.getSecretId());
            httpParams.put("keyinfo", keyinfo);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(sendUrl).json(httpParams).execute();

            if (response.getStatusCode() == 200) {
                String validateResponse = response.getResponseString();
                Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
                if (apiResult == null || !apiResult.containsKey("result") || !apiResult.get("result").equals("000000")) {
                    return null;
                } else {
                    Map map = (Map) apiResult.get("tokenInfo");
                    return String.valueOf(map.get("token"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    static String HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
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

    static String bytesToHexString(byte[] src) {
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
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            return "http://ticket.eduyun.cn:10000/aamif/ticketValidate?ticket=";
        } else {
            return "http://111.4.118.51:12000/aamif/ticketValidate?ticket=";
        }
    }

    private static String getAccessTokenGetUrl() {
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            return "http://api.eduyun.cn/apigateway/getaccesstoken";
        } else {
            return "http://111.4.118.51:16001/apigateway/getaccesstoken";
        }
    }

    private static String getUserInfoGetUrl() {
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            return "http://api.eduyun.cn/aam/rest/user/getuserinfo/";
        } else {
            return "http://111.4.118.51:16001/aam/rest/user/getuserinfo/";
        }
    }

    public static void main(String[] args) {
        try {
            String sendUrl = getTicketValidateUrl() + "NmIzNWVmY2FhNzgyYzQ3YTNhZGY3ZTlmODJjMWNkYzhlMTQzMzIzNzM0MjkwOA==";
            System.out.println(sendUrl);
            String responseStr = HttpRequestExecutor.defaultInstance().get(sendUrl)
                    .execute().getResponseString();
            System.out.println(responseStr);
            int startIndex = responseStr.indexOf("<cas:user>");
            int endIndex = responseStr.indexOf("</cas:user>");
            if (startIndex > 0 && endIndex > 0) {
                System.out.println(responseStr.substring(startIndex + "<cas:user>".length(), endIndex));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
