package com.voxlearning.luffy.aggregate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.luffy.support.SessionHelper;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserService;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.ChipsMiniProgramConfig;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@Slf4j
public class ChipsWechatUserAggregate {


    /**
     * 微信api - 创建会话
     *
     * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/api/api-login.html#wxloginobject">创建会话</a>
     */
    private static String URL_CODE2SESSION = "https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code";


    @Inject
    private SessionHelper sessionHelper;

    @ImportService(interfaceClass = ChipsWechatUserService.class)
    private ChipsWechatUserService wechatUserService;

    public MapMessage loginAndRegister(String code) {
        String url = StringUtils.formatMessage(URL_CODE2SESSION, ChipsMiniProgramConfig.APP_ID, ChipsMiniProgramConfig.APP_SECRET, code);
        WechatJSCodeResult result = null;
        try {
            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(url).execute();
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", 0L,
                    "mod1", response.getStatusCode(),
                    "mod2", response.getResponseString(),
                    "mod3", url,
                    "op", "chipsWechatUserFetchOpenId"
            ));
            switch (response.getStatusCode()) {
                case 200: {
                    String responseText = response.getResponseString();
                    result = JSON.parseObject(responseText, WechatJSCodeResult.class);
                    if (result == null) {
                        log.error("luffy chipsWechatUserFetchOpenId error, result {}", responseText);
                        return wechatServerError("");
                    }
                    if (StringUtils.isNotBlank(result.getErrCode()) || StringUtils.isBlank(result.getOpenId())) {
                        log.error("luffy chipsWechatUserFetchOpenId error, result {}", responseText);
                        return wechatServerError(result.getErrMsg());
                    }
                    break;
                }
                default: {
                    log.error("luffy chipsWechatUserFetchOpenId error, result {}", response.getResponseString());
                    return wechatServerError("");
                }
            }
        } catch (Exception e) {
            return wechatServerError("");
        }

        MapMessage mapMessage = wechatUserService.register(result.getOpenId(), WechatUserType.CHIPS_MINI_PROGRAM.name());
        if (mapMessage.isSuccess()) {
            sessionHelper.cacheToken(result.getOpenId(), result.getSessionKey());
            mapMessage.set("token", result.getOpenId());
        }

        return mapMessage;
    }

    private MapMessage wechatServerError(String info) {
        ChipsErrorType errorType = ChipsErrorType.WECHAT_SERVER_ERROR;
        return MapMessage.errorMessage(StringUtils.isNotBlank(info) ? info : errorType.getInfo()).setErrorCode(errorType.getCode());
    }


    /**
     * 微信侧创建会话响应
     */
    @Data
    private static class WechatJSCodeResult implements Serializable {

        private static final long serialVersionUID = -1l;

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
