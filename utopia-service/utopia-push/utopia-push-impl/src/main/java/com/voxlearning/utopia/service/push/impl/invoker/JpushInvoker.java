package com.voxlearning.utopia.service.push.impl.invoker;

import com.voxlearning.alps.api.monitor.PublishMonitorGenericInvocationEvent;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.push.api.constant.PushMeta;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import com.voxlearning.utopia.service.push.impl.support.VendorPushConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.message.BasicHeader;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * @author xinxin
 * @since 4/26/17.
 */
@Slf4j
public class JpushInvoker {

    public static AlpsHttpResponse invoke(PushTargetType targetType, Map<String, Object> params, String appKey) {
        String jgAuth = VendorPushConfiguration.getJPushAuthentication(appKey);
        if (StringUtils.isBlank(jgAuth)) return null;

        String url = targetType == PushTargetType.ALIAS ? PushMeta.JG.getBatchSendUrl() : PushMeta.JG.getTopicUrl();

        HttpPoolingRequestExecutor.InternalHttpRequestExecutor executor = HttpPoolingRequestExecutor.get();

        Instant start = Instant.now();
        AlpsHttpResponse response;
        try {
            response = executor.post(url)
                    .headers(new BasicHeader("Authorization", jgAuth))
                    .headers(new BasicHeader("Content-Type", "application/json"))
                    .connectionTimeout(5000)
                    .socketTimeout(15000)
                    .json(JsonUtils.toJson(params))
                    .execute();
            LoggerUtils.info(url, appKey, params, response.toString());
        } finally {
            Instant stop = Instant.now();
            long duration = stop.toEpochMilli() - start.toEpochMilli();
            PublishMonitorGenericInvocationEvent.publish("JPSInvocation", stop.getEpochSecond(), duration);
        }

        return response;
    }

    public static void main(String[] args) {
        String appKey = "ab9209e2f2e8e51b6d187257";
        String secretKey = "fbf0d1f5e70a3933c393fb43";
        System.out.println(Base64.getEncoder().encodeToString((appKey + ":" + secretKey).getBytes()));
        appKey = "b6b9bc86bc50fba440ec3504";
        secretKey = "346982b0319533ad2af24757";
        System.out.println(Base64.getEncoder().encodeToString((appKey + ":" + secretKey).getBytes()));

//        String jgAuth = "Basic YjUwMGNiOGQ3YjcwOTg0ZWNhOWU2MzRiOmY5NjQ3MGU2NWIyMzFiMTRjYmNiMDA3NA==";
//        String requestUrl = "https://device.jpush.cn/v3/aliases/220063947";
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(requestUrl)
//                .headers(new BasicHeader("Authorization", jgAuth))
//                .headers(new BasicHeader("Content-Type", "application/json"))
//                .connectionTimeout(3000)
//                .socketTimeout(10000)
//                .execute();
//
//        System.out.println(response.getResponseString());
//
//        requestUrl = "https://device.jpush.cn/v3/devices/1104a89792acab106b2";
//        response = HttpRequestExecutor.defaultInstance().get(requestUrl)
//                .headers(new BasicHeader("Authorization", jgAuth))
//                .headers(new BasicHeader("Content-Type", "application/json"))
//                .connectionTimeout(3000)
//                .socketTimeout(10000)
//                .execute();
//        System.out.println(response.getResponseString());
//
//        requestUrl = "https://device.jpush.cn/v3/devices/191e35f7e040b297cf6";
//        response = HttpRequestExecutor.defaultInstance().get(requestUrl)
//                .headers(new BasicHeader("Authorization", jgAuth))
//                .headers(new BasicHeader("Content-Type", "application/json"))
//                .connectionTimeout(3000)
//                .socketTimeout(10000)
//                .execute();
//        System.out.println(response.getResponseString());
//
//        requestUrl = "https://device.jpush.cn/v3/devices/161a3797c80e3decb40";
//        response = HttpRequestExecutor.defaultInstance().get(requestUrl)
//                .headers(new BasicHeader("Authorization", jgAuth))
//                .headers(new BasicHeader("Content-Type", "application/json"))
//                .connectionTimeout(3000)
//                .socketTimeout(10000)
//                .execute();
//        System.out.println(response.getResponseString());

    }

}
