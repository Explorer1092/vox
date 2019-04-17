package com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi.daite;

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.vendor.impl.service.thirdpartapi.ThirdPart;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhouwei on 2018/8/2
 **/
@Named
@Slf4j
public class DaiteLoginApi extends DaiteApi {

    /**
     * 校验用户是否在DT方已经登录
     *
     * @param token
     * @return
     * @author zhouwei
     */
    public MapMessage checkLogin(String token) {
        Map<String, Object> httpParams = new HashMap<>();
        httpParams.put("timestamp", new Date().getTime());
        httpParams.put("token", token);
        httpParams.put("app_key", ThirdPart.DAITE.getAppKey());
        String sig = this.generateRequestSig(ThirdPart.DAITE.getAppKey(), httpParams);
        httpParams.put("sig", sig);
        String url = this.getDomain() + DaiteApi.checkLoginUrl;

        String apiURL = UrlUtils.buildUrlQuery(url, httpParams);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(apiURL).execute();
        if (response.getStatusCode() == 200) {
            String validateResponse = response.getResponseString();
            Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
            if (apiResult == null || !apiResult.containsKey("code") || !Objects.equals(String.valueOf(apiResult.get("code")),"200") || SafeConverter.toLong(apiResult.get("data")) == 0) {
                log.error("request's result error. url: {}, param: {}, response: [head {}], [response {}]", url, httpParams, response.getHeaders(), response.getResponseString());
                return MapMessage.errorMessage("API结果返回错误错误, result：" + apiResult);
            } else {
                MapMessage message = MapMessage.successMessage();
                message.put("userId", SafeConverter.toLong(apiResult.get("data")));
                return message;
            }
        } else {
            log.error("request api result. url: {}, param: {}, response: [head {}], [response {}]", url, httpParams, response.getHeaders(), response.getResponseString());
            return MapMessage.errorMessage("API调用错误");
        }
    }

}
