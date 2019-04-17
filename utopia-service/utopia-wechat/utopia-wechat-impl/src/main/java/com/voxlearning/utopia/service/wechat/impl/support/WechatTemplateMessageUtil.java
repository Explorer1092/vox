package com.voxlearning.utopia.service.wechat.impl.support;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;

import java.util.Collections;
import java.util.Map;

public class WechatTemplateMessageUtil {
    private static String URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    public static Map<String, Object> send(String token, Map<String, Object> paramsMap) {
        try {
            String response = HttpRequestExecutor.defaultInstance()
                    .post(URL + token)
                    .json(paramsMap)
                    .execute().getResponseString();
            return JsonUtils.fromJson(response);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
