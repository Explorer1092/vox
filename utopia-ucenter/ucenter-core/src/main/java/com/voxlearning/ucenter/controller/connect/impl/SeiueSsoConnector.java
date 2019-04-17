package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.api.constant.seiue.SeiueApi;
import com.voxlearning.utopia.api.constant.seiue.SeiueConfig;
import com.voxlearning.utopia.api.constant.seiue.SeiueSupport;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;

import javax.inject.Named;
import java.util.Map;

/**
 * 希悦平台用的
 *
 * @author xuesong.zhang
 * @since 2016/10/11
 */
@Named
public class SeiueSsoConnector extends AbstractSsoConnector {

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        if (StringUtils.isBlank(token)) {
            logger.error("Invalid Seiue System token");
            return MapMessage.errorMessage("Blank token");
        }
        SeiueConfig config = config();
        SeiueApi login = SeiueSupport.loginApi;
        String url = config.getApiUrl() + login.getApiUrl();
        AlpsHttpResponse res = HttpRequestExecutor
                .instance(HttpClientType.POOLING)
                .get(url)
                .headers(config.requestHeaders())
                .authorization("Bearer " + token)
                .execute();
        if (res == null || res.hasHttpClientException()) {
            logger.error("Failed get user info, url = {}", url);
            return MapMessage.errorMessage("获取用户信息失败");
        }
        Map<String, Object> userInfo = JsonUtils.fromJson(res.getResponseString());
        if (userInfo == null || userInfo.isEmpty()) {
            logger.error("Illegal user info resp = {}", res.getResponseString());
            return MapMessage.errorMessage("Illegal user info response");
        }
        return MapMessage.successMessage()
                .add("userId", userInfo.get("id"))
                .add("userName", userInfo.get("name"))
                .add("userCode", UserType.TEACHER.getType())
                .add("loginName", userInfo.get("usin"));
    }

    public SeiueConfig config() {
        if (RuntimeMode.isUsingProductionData()) {
            return SeiueSupport.SEIUE_CONFIG_PROD;
        }
//        if (RuntimeMode.isDevelopment()) {
//            return SeiueSupport.SEIUE_CONFIG_DEV;
//        }
        return SeiueSupport.SEIUE_CONFIG_DEV;
    }
}
