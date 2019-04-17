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

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.washington.controller.connect.AbstractSsoConnector;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 牡丹江教育云接入用验证Class
 * <p>
 * Created by Alex on 14-10-15.
 */
@Named
@Deprecated
public class MdjeduSsoConnector extends AbstractSsoConnector {

    private static final String TOKEN_VALIDATE_URL = "http://yun.mdjedu.net/sunrise-core-web/api/auth/validate";
    private static final String USER_GET_URL = "http://yun.mdjedu.net/sunrise-core-web/api/user/get";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // TODO 现在的模式有DNS劫持的风险，对方表示要用HTTPS，等待修正结果

        // Step1 验证Token是否有效并获取用户ID
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("token", token);
        httpParams.put("client_id", connectionInfo.getClientId());
        //httpParams.put("client_secret", connectionInfo.getSecretId());
        //httpParams.put("service", ""); // 似乎可以不要

        String URL = UrlUtils.buildUrlQuery(TOKEN_VALIDATE_URL, httpParams);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        String validateResponse = response.getResponseString();
        Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
        if (apiResult == null || !"0".equals(String.valueOf(apiResult.get("ret")))) {
            // logger.warn("token validate api response:{}", validateResponse);
            return MapMessage.errorMessage("token validate failed!");
        }

        String userId = String.valueOf(apiResult.get("userId"));

        // Step2 使用UserId获取用户信息
        httpParams.clear();
        httpParams.put("client_id", connectionInfo.getClientId());
        httpParams.put("client_secret", connectionInfo.getSecretId());
        httpParams.put("userId", userId);
        URL = UrlUtils.buildUrlQuery(USER_GET_URL, httpParams);
        response = HttpRequestExecutor.defaultInstance().get(URL).execute();

        validateResponse = response.getResponseString();
        logger.debug("user get api response:{}", validateResponse);

        apiResult = JsonUtils.fromJson(validateResponse);
        if (apiResult == null || !"0".equals(String.valueOf(apiResult.get("ret")))) {
            return MapMessage.errorMessage("user get failed!");
        }

        MapMessage result = MapMessage.successMessage();
        result.add("userId", userId);

        List itemList = (List) apiResult.get("items");
        if (itemList != null && itemList.size() > 0) {
            Map itemData = (Map) itemList.get(0);
            result.add("userName", itemData.get("userName"));
            result.add("userCode", itemData.get("userCode"));
            result.add("userMobile", itemData.get("tel"));
        }

        return result;
    }
}
