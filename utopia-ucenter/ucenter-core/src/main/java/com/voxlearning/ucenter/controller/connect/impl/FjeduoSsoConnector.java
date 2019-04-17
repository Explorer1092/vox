package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 15-3-10.
 */
@Named
public class FjeduoSsoConnector extends AbstractSsoConnector {
    private static final String MAC_NAME = "HmacSHA1";
    private static final String TICKET_VALIDATE_URL = "http://www.fjedu.cn:20014/aamif/ticketValidate?ticket=";
    private static final String ACCESS_TOKEN_GET_URL = "http://www.fjedu.cn:20001/apigateway/getaccesstoken";
    private static final String USER_INFO_GET_URL = "http://www.fjedu.cn:20001/aam/rest/user/getuserinfo/";

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
        String sendUrl = USER_INFO_GET_URL + session + "?token=" + accessToken;
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(sendUrl).execute();
        if (response != null && response.getStatusCode() == 200) {
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

    private String validateTicket(String ticket) {
        String httpUrl = TICKET_VALIDATE_URL + ticket;
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
            final String sendUrl = ACCESS_TOKEN_GET_URL;
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

//    public static void main(String[] args) {
//        try {
//            String sendUrl = TICKET_VALIDATE_URL + "b2IxODAwNjViZDg5NDRmZjliMGM0ODZjM2YzNDBlODU1MTQyNTk3NjA0MzA1MQ==";
//            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(sendUrl).execute();
//            String responseStr = response.getResponseString();
//            System.out.println(responseStr);
//            int startIndex = responseStr.indexOf("<cas:user>");
//            int endIndex = responseStr.indexOf("</cas:user>");
//            if (startIndex > 0 && endIndex > 0) {
//                System.out.println(responseStr.substring(startIndex + "<cas:user>".length(), endIndex));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}