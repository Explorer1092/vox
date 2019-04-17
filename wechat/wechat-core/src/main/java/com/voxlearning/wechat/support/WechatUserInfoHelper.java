package com.voxlearning.wechat.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;

public class WechatUserInfoHelper {
    public static WechatUserInfo getInfo(String accessToken, String openId) {
        if (StringUtils.isAnyBlank(accessToken, openId)) {
            return null;
        }
        try {
            String response = HttpRequestExecutor.defaultInstance()
                    .get("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken +"&openid="+ openId +"&lang=zh_CN")
                    .execute().getResponseString();
            return JsonUtils.fromJson(response, WechatUserInfo.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static WechatUserInfo getSNSUserInfo(String accessToken, String openId) {
        if (StringUtils.isAnyBlank(accessToken, openId)) {
            return null;
        }
        try {
            String response = HttpRequestExecutor.defaultInstance()
                    .get("https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken +"&openid="+ openId +"&lang=zh_CN")
                    .execute().getResponseString();
            return JsonUtils.fromJson(response, WechatUserInfo.class);
        } catch (Exception e) {
            return null;
        }
    }
}
