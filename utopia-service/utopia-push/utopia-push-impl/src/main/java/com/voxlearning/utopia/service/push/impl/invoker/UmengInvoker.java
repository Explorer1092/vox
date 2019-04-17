package com.voxlearning.utopia.service.push.impl.invoker;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.util.DigestUtils;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.entity.AppJpushMessageRetry;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.push.api.support.PushRetryContext;
import com.voxlearning.utopia.service.push.impl.builder.InvokeMessageBuilder;
import com.voxlearning.utopia.service.push.impl.builder.UmengAndroidInvokeMessageBuilder;
import com.voxlearning.utopia.service.push.impl.builder.UmengIOSInvokeMessageBuilder;
import com.voxlearning.utopia.service.push.impl.persistence.AppJpushMessageRetryPersistence;
import com.voxlearning.utopia.service.push.impl.support.VendorPushConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * @author xinxin
 * @since 11/11/2016
 */
@Named
@Slf4j
public class UmengInvoker {
    @Inject
    private AppJpushMessageRetryPersistence appJpushMessageRetryPersistence;

    private static final String MSG_SEND_POST_URL = "http://umeng.17zyw.cn/api/send";  // 反向代理,原域名 msg.umeng.com
    private static final String MSG_SEND_POST_UR_REAL = "http://msg.umeng.com/api/send";  // ONLY FOR SIGN

    public void invoke(PushContext context, InvokeMessageBuilder messageBuilder) {
        Map<String, Object> message = messageBuilder.toMessage();
        PushType pushType = getPushType(messageBuilder);
        String secret = getSecret(pushType, context.getSource().appKey);
        if (StringUtils.isBlank(secret)) {
            return;
        }

        String sign = md5Sign("POST", MSG_SEND_POST_UR_REAL, JsonUtils.toJson(message), secret);

        try {
            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                    .post(MSG_SEND_POST_URL + "?sign=" + sign)
                    .json(message)
                    .turnOffLogException()
                    .execute();

            if (RuntimeMode.current().le(Mode.STAGING)) { // only output log in staging
                log.warn("umeng post url " + MSG_SEND_POST_URL + "?sign=" + sign);
                log.warn("umeng post json " + JsonUtils.toJson(message));
                log.warn("umeng post response " + response.getResponseString());
            }

            if (null != response && !response.hasHttpClientException()
                    && (response.getStatusCode() == 200 || response.getStatusCode() == 400)) {
                invokeSuccess(context, message, response, pushType);
            } else {
                invokeFail(context, message, response, pushType);
            }
        } catch (Exception ex) {
            invokeFail(context, message, null, pushType);
        }
    }

    private PushType getPushType(InvokeMessageBuilder messageBuilder) {
        if (messageBuilder instanceof UmengAndroidInvokeMessageBuilder) {
            return PushType.UMENG_ANDRIOD;
        } else if (messageBuilder instanceof UmengIOSInvokeMessageBuilder) {
            return PushType.UMENG_IOS;
        }
        return null;
    }

    private String getSecret(PushType pushType, String app) {
        if (pushType == PushType.UMENG_ANDRIOD) {
            return VendorPushConfiguration.getUmengAndriodSecret(app);
        } else if (pushType == PushType.UMENG_IOS) {
            return VendorPushConfiguration.getUmengIOSSecret(app);
        }
        return null;
    }

    private String md5Sign(String method, String url, String body, String secret) {
        String str = method + url + body + secret;
        return DigestUtils.md5Hex(str).toLowerCase();
    }

    private void invokeSuccess(PushContext context, Map<String, Object> message, AlpsHttpResponse response, PushType pushType) {
        if (context instanceof PushRetryContext) {
            appJpushMessageRetryPersistence.updateRetrySuccess(new ObjectId(((PushRetryContext) context).getId()));
        }
    }

    private void invokeFail(PushContext context, Map<String, Object> message, AlpsHttpResponse response, PushType pushType) {
        //retry
        if (null != response) {
            if (context instanceof PushRetryContext) {
                appJpushMessageRetryPersistence.updateRetryFailed(new ObjectId(((PushRetryContext) context).getId()));
            } else {
                AppJpushMessageRetry retry = new AppJpushMessageRetry();
                retry.setMessageSource(context.getSource().name());
                retry.setNotify(JsonUtils.toJson(context));
                retry.setRetryCount(0);
                retry.setStatus(0);
                retry.setHttpStatusCode(response.getStatusCode());
                retry.setPushType(pushType);
                retry.setNotifyId(context.getExtInfo().get("customId").toString());
                retry.setCause(response.getHttpClientExceptionMessage());
                appJpushMessageRetryPersistence.insert(retry);
            }
        }

        //log collect
        if (null == response || response.hasHttpClientException()) {
            LogCollector.info("push_slow_request_log", MiscUtils.map(
                    "pushType", pushType.name(),
                    "cause", null == response ? null : response.getHttpClientExceptionMessage(),
                    "evn", RuntimeMode.getCurrentStage(),
                    "time", DateUtils.dateToString(new Date()),
                    "context", JsonUtils.toJson(context)
            ));
        }
    }
}
