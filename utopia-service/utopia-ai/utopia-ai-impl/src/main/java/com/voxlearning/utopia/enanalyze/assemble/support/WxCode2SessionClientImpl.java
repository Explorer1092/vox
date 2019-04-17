package com.voxlearning.utopia.enanalyze.assemble.support;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.enanalyze.assemble.WxCode2SessionClient;
import com.voxlearning.utopia.enanalyze.exception.support.ThirdPartyServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Service
public class WxCode2SessionClientImpl implements WxCode2SessionClient, InitializingBean {

    HttpRequestExecutor executor;

    @Override
    public Result getSession(String appId, String appSecret, String code) {
        String url = StringUtils.formatMessage(URL_CODE2SESSION, appId, appSecret, code);
        Result response;
        AlpsHttpResponse _response = executor.get(url).execute();
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", 0L,
                "mod1", _response.getStatusCode(),
                "mod2", _response.getResponseString(),
                "mod3", url,
                "op", "aiOCRNLPUserLogin"
        ));
        switch (_response.getStatusCode()) {
            case 200: {
                String responseText = _response.getResponseString();
                response = JSON.parseObject(responseText, Result.class);
                if (StringUtils.isNotBlank(response.getErrCode())) {
                    log.error("luffy code2session error, result {}", responseText);
                    throw new ThirdPartyServiceException("微信code2session服务异常");
                }
                break;
            }
            default: {
                log.error("luffy code2session error, result {}", _response.getResponseString());
                throw new ThirdPartyServiceException("微信code2session服务异常");
            }
        }
        return response;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executor = HttpRequestExecutor.instance(HttpClientType.POOLING);
    }
}
