package com.voxlearning.utopia.service.push.impl.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.service.push.impl.invoker.JpushInvoker;
import com.voxlearning.utopia.service.push.impl.invoker.MipushInvoker;
import com.voxlearning.utopia.service.push.api.constant.PushMeta;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import org.apache.http.message.BasicHeader;

import java.util.Date;
import java.util.Map;

/**
 * Created by wangshichao on 16/8/23.
 */
public class ThirdInvokeHelper {
    private static int maxTimeOut;

    static {
        maxTimeOut = RuntimeMode.current() == Mode.PRODUCTION ? 3000 : 100;
    }

    public static AlpsHttpResponse invokeThird(PushType pushType, Map<String, Object> paraMap, PushTarget pushTarget, String app) {
        if (pushType != PushType.JG && pushType != PushType.MI) return null;

        PushTargetType targetType = PushTargetType.convert(pushTarget);
        if (null == targetType) return null;

        long t1 = System.currentTimeMillis();

        AlpsHttpResponse response;
        if (pushType == PushType.JG) {
            response = JpushInvoker.invoke(targetType, paraMap, app);
        } else {
            response = MipushInvoker.invoke(targetType, paraMap, app);
        }

        long cost = System.currentTimeMillis() - t1;
        if (cost > maxTimeOut) {
            recordSlowLog(pushType, response, pushTarget, app, cost);
        }
        return response;
    }

    private static void recordSlowLog(PushType pushType, AlpsHttpResponse response, PushTarget pushTarget, String app, Long cost) {
        try {
            String msgId = "";
            if (response != null) {
                Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(response.getResponseString());
                if (MapUtils.isNotEmpty(resMap)) {
                    switch (pushType) {
                        case JG:
                            msgId = SafeConverter.toString(resMap.get("msg_id"), "");
                            break;
                        case MI:
                            Object data = resMap.get("data");
                            if (data != null && data instanceof Map) {
                                Map<String, Object> dataMap = (Map<String, Object>) data;
                                msgId = SafeConverter.toString(dataMap.get("id"), "");
                                break;
                            }
                        default:
                    }
                }
            }
            LogCollector.info("push_slow_request_log",
                    MiscUtils.map(
                            "pushType", pushType == null ? "" : pushType.name(),
                            "msgId", msgId == null ? "" : msgId,
                            "app", app == null ? "" : app,
                            "cost", cost == null ? 0L : cost,
                            "pushTarget", pushTarget == null ? "" : pushTarget.name(),
                            "env", RuntimeMode.getCurrentStage(),
                            "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date())
                    ));
        } catch (Exception ex) {
            //ignore
        }
    }
}
