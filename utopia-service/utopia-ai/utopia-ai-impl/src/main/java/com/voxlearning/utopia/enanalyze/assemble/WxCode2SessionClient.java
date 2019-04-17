package com.voxlearning.utopia.enanalyze.assemble;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信的登录接口
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@FunctionalInterface
public interface WxCode2SessionClient {


    /**
     * 微信api - 创建会话
     *
     * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api/api-login.html#wxloginobject">创建会话</a>
     */
    String URL_CODE2SESSION = "https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code";

    /**
     * 发送https请求，获取微信侧的会话
     *
     * @param appId
     * @param appSecret
     * @param code
     * @return
     */
    Result getSession(String appId, String appSecret, String code);

    /**
     * 微信侧创建会话响应
     */
    @Data
    class Result implements Serializable {

        @JSONField(name = "errcode")
        private String errCode;

        @JSONField(name = "errmsg")
        private String errMsg;

        @JSONField(name = "openid")
        private String openId;

        @JSONField(name = "session_key")
        private String sessionKey;

        @JSONField(name = "unionid")
        private String unionId;
    }
}
