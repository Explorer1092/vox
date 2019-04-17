package com.voxlearning.utopia.service.push.impl.builder;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.push.impl.support.VendorPushConfiguration;
import com.voxlearning.utopia.service.push.api.constant.PushConstants;
import com.voxlearning.utopia.service.push.api.support.PushContext;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 10/11/2016
 */
public class UmengIOSInvokeMessageBuilder extends AbstractUmengInvokerMessageBuilder {
    private PushContext context;

    public static InvokeMessageBuilder instance(PushContext context) {
        UmengIOSInvokeMessageBuilder builder = new UmengIOSInvokeMessageBuilder();
        builder.context = context;
        return builder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMessage() {
        Map<String, Object> message = new HashMap<>();

        //基本信息
        message.put(PushConstants.PUSH_FIELD_UMENG_APPKEY, VendorPushConfiguration.getUmengIOSAppKey(context.getSource().appKey));
        message.put(PushConstants.PUSH_FIELD_UMENG_TIMESTAMP, Instant.now().toEpochMilli());

        //消息发送类型
        fillTargetInfo(message, context);

        //消息体
        Map<String, Object> payload = new HashMap<>();
        message.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD, payload);

        if (MapUtils.isNotEmpty(context.getClientInfo())) {
            payload.putAll(context.getClientInfo());
        }

        Map<String, Object> aps = new HashMap<>();
        payload.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_APS, aps);

        aps.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_APS_ALERT, context.getContent());

        //发送策略
        Map<String, Object> policy = new HashMap<>();
        if (context.getExtInfo().containsKey(PushConstants.PUSH_FIELD_UMENG_POLICY_START_TIME)) {
            policy.put(PushConstants.PUSH_FIELD_UMENG_POLICY_START_TIME, context.getExtInfo().get(PushConstants.PUSH_FIELD_UMENG_POLICY_START_TIME));
        }
        if (null != context.getDuration() && context.getDuration() > 0) {
            policy.put(PushConstants.PUSH_FIELD_UMENG_POLICY_MAX_SEND_NUM, 500);
        }
        if (MapUtils.isNotEmpty(policy)) {
            message.put(PushConstants.PUSH_FIELD_UMENG_POLICY, policy);
        }

        //消息描述
        if (context.getExtInfo().containsKey(PushConstants.PUSH_FIELD_UMENG_DESC)) {
            message.put(PushConstants.PUSH_FIELD_UMENG_DESC, context.getExtInfo().get(PushConstants.PUSH_FIELD_UMENG_DESC));
        }
        //自定义ID
        if (context.getExtInfo().containsKey(PushConstants.PUSH_FIELD_UMENG_THIRDPARTY_ID)) {
            message.put(PushConstants.PUSH_FIELD_UMENG_THIRDPARTY_ID, context.getExtInfo().get(PushConstants.PUSH_FIELD_UMENG_THIRDPARTY_ID));
        }
        return message;
    }
}
