package com.voxlearning.washington.controller.open.v1.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2016/9/19
 */
public class TestUserApiController {
    public static void main(String[] args) {
        System.out.println("Test Start...");
        final String secretKey = "qaP4ElgkY8ss";

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("app_key", "Shensz");
        paramsMap.put("user_auth_infos", JsonUtils.toJson(Arrays.asList(MapUtils.m("user_id", 12979800, "authenticationState", "SUCCESS"))));
        String sig = DigestSignUtils.signMd5(paramsMap, secretKey);
        paramsMap.put("sig", sig);
        String apiUrl = "http://www.test.17zuoye.net/v1/user/updateUserAuthenticationState.vpage";
        String URL = UrlUtils.buildUrlQuery(apiUrl, paramsMap);

        System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());
        System.out.println("Test End...");
    }
}
