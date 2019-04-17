package com.voxlearning.utopia.service.push.impl.invoker;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.service.push.impl.support.VendorPushConfiguration;
import com.voxlearning.utopia.service.push.api.constant.PushMeta;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 4/26/17.
 */
@Slf4j
public class MipushInvoker {

    public static AlpsHttpResponse invoke(PushTargetType targetType, Map<String, Object> params, String appKey) {

        try {
            Map<Object, Object> map = new HashMap<>();
            map.putAll(params);
            String miAuth = VendorPushConfiguration.getMiPushAuthentication(appKey);
            if (StringUtils.isBlank(miAuth)) return null;

            String url = targetType == PushTargetType.ALIAS ? PushMeta.MI.getBatchSendUrl() : PushMeta.MI.getTopicUrl();

            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).post(url)
                    .headers(new BasicHeader("Authorization", miAuth))
                    .socketTimeout(30000)
                    .addParameter(map)
                    .turnOffLogException()
                    .execute();

            return response;
        } catch (Exception e) {
            log.error("send mipush failed. target:{}, param:{}, appkey:{}", targetType, JsonUtils.toJson(params), appKey, e);
            return null;
        }
    }
}
