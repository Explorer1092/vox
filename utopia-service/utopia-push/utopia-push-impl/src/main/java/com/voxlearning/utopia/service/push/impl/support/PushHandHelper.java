package com.voxlearning.utopia.service.push.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushStatus;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.entity.AppJpushMessageRetry;
import com.voxlearning.utopia.service.push.impl.persistence.AppJpushMessageRetryPersistence;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * Created by wangshichao on 16/8/23.
 */
@Named
public class PushHandHelper extends SpringContainerSupport {


    @Inject
    private AppJpushMessageRetryPersistence appJpushMessageRetryPersistence;

    public void sendPushNotify(Map<String, Object> messageMap) {

        PushTarget pushTarget = PushTarget.valueOf(messageMap.get("target").toString());
        Map<String, Object> params = (Map<String, Object>) messageMap.get("params");
        String notifyId = ConversionUtils.toString(messageMap.get("notifyId"));
        String sourceName = SafeConverter.toString(messageMap.get("source"));
        AppMessageSource source = AppMessageSource.of(sourceName);
        if (source == AppMessageSource.UNKNOWN) {
            return;
        }
        PushType pushType = PushType.valueOf(messageMap.get("pushType").toString());

        try {
            AlpsHttpResponse response = ThirdInvokeHelper.invokeThird(pushType, params, pushTarget, source.appKey);
            if (null == response) return;
            int statusCode = response.getStatusCode();
            PushStatus pushStatus = getSendStatus(pushType, response);
            if (pushStatus != PushStatus.OK) {

                LoggerUtils.info("push.MIPush.error", notifyId, statusCode, pushType, source.appKey, response.getResponseString());

                AppJpushMessageRetry retry = new AppJpushMessageRetry();
                retry.setMessageSource(source.name());
                retry.setNotify(JsonUtils.toJson(messageMap));
                retry.setRetryCount(0);
                retry.setStatus(0);
                retry.setHttpStatusCode(statusCode);
                retry.setPushType(pushType);
                retry.setNotifyId(notifyId);
                String cause = response.getHttpClientExceptionMessage();
                retry.setCause(cause);
                appJpushMessageRetryPersistence.insert(retry);
            }
        } catch (Exception e) {
            logger.error("Send Jpush message failed with exception! error:", e);
            logger.error("Send Jpush message failed with exception! id:", notifyId);
        }
    }

    public void sendRetryPushNotify(Map<String, Object> messageMap) {

        PushType pushType = PushType.valueOf(SafeConverter.toString(messageMap.get("pushType")));
        Integer retryCount = ConversionUtils.toInt(messageMap.get("retryCount"));
        Map<String, Object> params = (Map<String, Object>) messageMap.get("params");
        String notifyId = ConversionUtils.toString(messageMap.get("notifyId"));
        String source = SafeConverter.toString(messageMap.get("source"));
        AppMessageSource appSource = AppMessageSource.of(source);
        if (appSource == AppMessageSource.UNKNOWN) {
            return;
        }
        PushTarget pushTarget = PushTarget.of(SafeConverter.toString(messageMap.get("target")));
        String retryId = SafeConverter.toString(messageMap.get("_id"));


        if (RuntimeMode.isUnitTest()) {
            // FIXME: 单元测试下，默认认为发送成功了。
            return;
        }

        try {
            AlpsHttpResponse response = ThirdInvokeHelper.invokeThird(pushType, params, pushTarget, appSource.appKey);

            if (null == response) return;
            int statusCode = response.getStatusCode();
            PushStatus pushStatus = getSendStatus(pushType, response);
            if (pushStatus == PushStatus.OK) {
                appJpushMessageRetryPersistence.updateRetrySuccess(new ObjectId(retryId));
            } else {
                appJpushMessageRetryPersistence.updateRetryFailed(new ObjectId(retryId), retryCount + 1);
            }
        } catch (Exception e) {
            logger.error("Send Jpush message failed with exception! error:", e);
            logger.error("Send Jpush message failed with exception! id:", notifyId);
        }
    }


    private PushStatus getSendStatus(PushType pushType, AlpsHttpResponse response) {

        int statusCode = response.getStatusCode();
        String responseString = response.getResponseString();
        switch (pushType) {
            case JG:
                if (statusCode == 200 || statusCode == 400) {
                    return PushStatus.OK;
                } else {
                    if (StringUtils.isBlank(responseString) && !response.hasHttpClientException()) {
                        return PushStatus.OK;
                    }
                }
                if (statusCode == 429) {
                    return PushStatus.OVERFREQUENTLY;
                }
                return PushStatus.FAIL;
            case MI:
                if (statusCode == 200) {
                    Map<String, Object> map = JsonUtils.fromJson(responseString);
                    if (map.get("result").equals("ok")) {
                        return PushStatus.OK;
                    }
                }
                return PushStatus.FAIL;
            case HW:
                if (statusCode == 200 || statusCode == 400) {
                    return PushStatus.OK;
                } else {
                    if (StringUtils.isBlank(responseString) && !response.hasHttpClientException()) {
                        return PushStatus.OK;
                    }
                }
                return PushStatus.FAIL;
            default:
                return PushStatus.FAIL;
        }
    }

}
