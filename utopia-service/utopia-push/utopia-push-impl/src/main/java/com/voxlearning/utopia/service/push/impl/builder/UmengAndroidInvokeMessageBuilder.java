package com.voxlearning.utopia.service.push.impl.builder;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
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
public class UmengAndroidInvokeMessageBuilder extends AbstractUmengInvokerMessageBuilder {

    private PushContext context;

    public static InvokeMessageBuilder instance(PushContext context) {
        UmengAndroidInvokeMessageBuilder builder = new UmengAndroidInvokeMessageBuilder();
        builder.context = context;
        return builder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMessage() {
        Map<String, Object> message = new HashMap<>();

        //基本信息
        message.put(PushConstants.PUSH_FIELD_UMENG_APPKEY, VendorPushConfiguration.getUmengAndriodAppKey(context.getSource().appKey));
        message.put(PushConstants.PUSH_FIELD_UMENG_TIMESTAMP, Instant.now().toEpochMilli());
        message.put(PushConstants.PUSH_FIELD_UMENG_MIPUSH, true);
        message.put(PushConstants.PUSH_FIELD_UMENG_MIPUSH_ACTIVITY, PushConstants.PUSH_VALUE_UMENG_MIPUSH_ACTIVITY);

        //消息发送类型
        fillTargetInfo(message, context);

        //消息体
        Map<String, Object> payload = new HashMap<>();
        message.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD, payload);

        payload.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_DISPLAY_TYPE, PushConstants.PUSH_VALUE_UMENG_PAYLOAD_DISPLAY_TYPE);
        if (MapUtils.isNotEmpty(context.getClientInfo())) {
            payload.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_EXTRA, context.getClientInfo());
        }

        Map<String, Object> body = new HashMap<>();
        payload.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY, body);

        body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_TICKER, context.getTicker());
        body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_TITLE, context.getTitle());
        body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_TEXT, context.getContent());
        if (StringUtils.isBlank(context.getTitle())) {
            body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_TITLE, getTitle(context));
        }
        if (StringUtils.isBlank(context.getTicker())) {
            body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_TICKER, getTitle(context));
        }

        //消息体：如果没有跳转URL，则默认是打开APP
        if (context.getExtInfo().containsKey(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_URL)) {
            body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_AFTER_OPEN, PushConstants.PUSH_VALUE_UMENG_PAYLOAD_BODY_AFTER_OPEN_GO_URL);
            body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_URL, context.getExtInfo().get(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_URL));
        } else {
            body.put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_AFTER_OPEN, PushConstants.PUSH_VALUE_UMENG_PAYLOAD_BODY_AFTER_OPEN_GO_APP);
        }

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

    private String getTitle(PushContext context) {
        if (!StringUtils.isBlank(context.getTitle())) {
            return context.getTitle();
        }

        if ("17Student".equals(context.getSource().appKey)) {
            return "一起作业学生";
        }
        if ("17Parent".equals(context.getSource().appKey)) {
            return "家长通";
        }

        return context.getContent();
    }
}
