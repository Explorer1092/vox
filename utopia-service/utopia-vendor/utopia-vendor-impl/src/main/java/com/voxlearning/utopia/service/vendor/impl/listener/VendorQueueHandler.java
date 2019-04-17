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

package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorNotifyPersistence;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vendor queue incoming message handler implementation.
 *
 * @author Zhilong Hu
 * @author Xiaohai Zhang
 * @since Nov 7, 2014
 */
@Named
public class VendorQueueHandler extends SpringContainerSupport {

    @Inject
    private VendorNotifyPersistence vendorNotifyPersistence;

    public void handleMessage(String messageText) {
        Map<String, Object> messageMap = JsonUtils.fromJson(messageText);
        if (messageMap == null) {
            logger.warn("Ignore unrecognized notify message: {}", messageText);
            return;
        }
        sendHttpNotify(messageMap);
    }

    private void sendHttpNotify(Map<String, Object> messageMap) {
        if (RuntimeMode.current() == Mode.UNIT_TEST) {
            return;
        }
        AlpsHttpResponse response = null;
        Long notifyId = null;
        try {
            String targetUrl = (String) messageMap.get("targetUrl");
            Map<String, Object> params = (Map<String, Object>) messageMap.get("params");

            notifyId = ConversionUtils.toLong(messageMap.get("notifyId"));

            if (notifyId == 0L) {
                String appKey = SafeConverter.toString(messageMap.get("appKey"));
                VendorNotify notify = new VendorNotify();
                notify.setAppKey(appKey);
                notify.setTargetUrl(targetUrl);
                notify.setNotify(JsonUtils.toJson(params));
                vendorNotifyPersistence.insert(notify);
                notifyId = notify.getId();
            }

            List<NameValuePair> httpParams = new ArrayList<>();
            for (String paramKey : params.keySet()) {
                NameValuePair item = new BasicNameValuePair(paramKey, SafeConverter.toString(params.get(paramKey)));
                httpParams.add(item);
            }

            // FIXME: jpush使用长连接方式
            HttpRequestExecutor executor;
            if (StringUtils.containsIgnoreCase(targetUrl, "jpush")) {
                executor = HttpRequestExecutor.instance(HttpClientType.POOLING);
            } else {
                executor = HttpRequestExecutor.defaultInstance();
            }

            if (isPiclistenBookOrder(messageMap, params) && params.containsKey("platform_key")) {
                //人教订单
                response = executor.post(targetUrl).json(JsonUtils.toJson(params)).execute();
            } else {
                response = executor.post(targetUrl)
                        .entity(new UrlEncodedFormEntity(httpParams, ICharset.defaultCharset()))
                        .execute();
            }

            if (targetUrl.contains("platform_change_order")) {
                //如果是人教换购，打印出response方便排查问题
                logger.info("pep change book response:{}", JsonUtils.toJson(response.getResponseString()));
            }

            if (isSuccess(response, messageMap, params)) {
                vendorNotifyPersistence.updateNotifyDeliveried(notifyId);
            } else {
                vendorNotifyPersistence.updateNotifyDeliveryFailed(notifyId);
            }
        } catch (Exception e) {
            // 打印出返回值
            String returnStr = "";
            if (response != null) {
                returnStr = response.getResponseString();
            }
            logger.error("send vendor http notify failed with param: {}, response {}", JsonUtils.toJson(messageMap), returnStr, e);
            // 设置为失败  重试任务可继续重试
            if (notifyId != null && notifyId != 0L) {
                vendorNotifyPersistence.updateNotifyDeliveryFailed(notifyId);
            }
        }
    }

    private boolean isSuccess(AlpsHttpResponse response, Map<String, Object> messageMap, Map<String, Object> params) {
        // 点读机订单通过返回的json判断是否成功
        if (isPiclistenBookOrder(messageMap, params)) {
            String responseStr = response.getResponseString();
            if (StringUtils.isBlank(responseStr)) {
                return false;
            }

            Map<String, Object> result = JsonUtils.fromJson(responseStr);
            Boolean isSuccess = result != null && (
                    (result.containsKey("success") && result.get("success").toString().equals("true"))  //外研社订单
                            || (result.containsKey("errcode") && result.get("errcode").toString().equals("110"))//人教订单
                            || (result.containsKey("Success") && result.get("Success").toString().equals("true"))); //沪教订单
            if (!isSuccess) {
                logger.error("piclistenbook order sync fail, response:{}, param:{}", responseStr, JsonUtils.toJson(messageMap));
            }
            return isSuccess;
        }

        //非点读机订单的通过code判断
        return response.getStatusCode() == 200;
    }

    private boolean isPiclistenBookOrder(Map<String, Object> messageMap, Map<String, Object> params) {
        return (messageMap.containsKey("appKey")
                && messageMap.get("appKey").toString().equals(OrderProductServiceType.PicListenBook.name())) || SafeConverter.toBoolean(params.get("is_piclisten"));
    }

    public static void main(String[] args) {
        final String appKey = "c6433753f02c45bff84e7796";
        final String appSecret = "eae146e3460074f4e944975c";
        final String sendUrl = "https://api.jpush.cn/v3/push";

        String authorization = appKey + ":" + appSecret;
        authorization = org.apache.commons.codec.binary.Base64.encodeBase64String(authorization.getBytes());
        authorization = "Basic " + authorization;
        System.out.println(authorization);

//        Map<String, Object> params = new HashMap<>();
//        params.put("platform", "android");
//
//        Map<String, Object> audienceMap = new HashMap<>();
//        List<String> tagList = new ArrayList<>();
//        tagList.add("clazz_37221");
//        audienceMap.put("tag", tagList);
//        params.put("audience", audienceMap);
//
//        Map<String, Object> notificationMap = new HashMap<>();
//        notificationMap.put("alert", "您的老师布置作业啦! 快去看看吧!");
//        params.put("notification", notificationMap);
//
//        Map<String, Object> optionMap = new HashMap<>();
//        optionMap.put("time_to_live", 604800);
//        params.put("options", optionMap);
//
//        System.out.println(JsonUtils.toJson(params));
//
//        // FIXME: jpush使用长连接方式
//        String r = HttpRequestExecutor.instance(HttpClientType.POOLING)
//                .post(sendUrl)
//                .headers(new BasicHeader("Content-Type", "application/json"))
//                .headers(new BasicHeader("Authorization", authorization))
//                .json(params)
//                .execute()
//                .getResponseString();
//        System.out.println(r);
    }


}
