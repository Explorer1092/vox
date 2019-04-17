package com.voxlearning.utopia.service.vendor.impl.push.processor;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushConstants;
import com.voxlearning.utopia.service.push.api.constant.PushTarget;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import com.voxlearning.utopia.service.vendor.impl.push.AppPushChannelManager;
import com.voxlearning.utopia.service.vendor.impl.push.PushProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.push.api.constant.PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_CA;

/**
 * Created by wangshichao on 16/8/25.
 */

@Named
public class JgChannelPushProcess extends SpringContainerSupport implements ChannelPushProcess {
    @Inject
    private PushProducer pushProducer;
    @Inject
    AppPushChannelManager appPushChannelManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        appPushChannelManager.register(this);
    }

    @Override
    public PushType getPushType() {
        return PushType.JG;
    }

    @Override
    public void product(Map<String, Object> paramMap) {
        String messageText = JsonUtils.toJson(paramMap);
        Object sourceObject = paramMap.get("source");
        AppMessageSource appMessageSource = AppMessageSource.of(SafeConverter.toString(sourceObject));
        if (appMessageSource == AppMessageSource.UNKNOWN) {
            return;
        }
        if (appMessageSource == AppMessageSource.YIQIXUETEAHCER || appMessageSource == AppMessageSource.YIQIXUEPARENT) {
            pushProducer.getYiQiXueProducer().produce(Message.newMessage().withStringBody(messageText));
        } else {
            pushProducer.getJpushProducer().produce(Message.newMessage().withStringBody(messageText));
        }
    }

    @Override
    public Set<Map<String, Object>> buildSendParams(Map<String, Object> map, PushTarget pushTarget) {
        String content = (String) map.get("content");
        Integer durationTime = SafeConverter.toInt(map.get("durationTime"));
        Map<String, Object> extras = (Map<String, Object>) map.get("extInfo");
        Map<String, Object> audienceMap = getAudience(map, pushTarget);

        Map<String, Object> params = new HashMap<>();
        params.put(PushConstants.PUSH_FIELD_JPUSH_PLATFORM, Arrays.asList(PushConstants.PUSH_VALUE_JPUSH_PLATFORM_ANDROID, PushConstants.PUSH_VALUE_JPUSH_PLATFORM_IOS));
        params.put(PushConstants.PUSH_FIELD_JPUSH_AUDIENCE, audienceMap);

        Map<String, Object> iosNotification = new HashMap<>();
        Map<String, Object> androidNotification = new HashMap<>();
        iosNotification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_ALERT, content);
        androidNotification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_ALERT, content);
        androidNotification.put("uri_activity", "com.yiqizuoye.library.jpush.JpushClickActivity");

        //11:00pm-7:30am静音
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 22 && hour > 8) {
            iosNotification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_SOUND, PushConstants.PUSH_VALUE_JPUSH_NOTIFICATION_IOS_SOUND);
            extras.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_EXTRAS_SOUND, PushConstants.PUSH_VALUE_JPUSH_NOTIFICATION_IOS_EXTRAS_SOUND_DEFAULT);
        } else {
            extras.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_EXTRAS_SOUND, PushConstants.PUSH_VALUE_JPUSH_NOTIFICATION_IOS_EXTRAS_SOUND_SILENT);
        }

        // 外部可以传入指定的时间戳
        if (!extras.containsKey(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_EXTRAS_TIMESTAMP)) {
            extras.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_EXTRAS_TIMESTAMP, System.currentTimeMillis());
        }
        //这里一起学老师端要实现App图标上的数字变化。实在没有其他地方能放这个逻辑了
        Object source = map.get("source");
        AppMessageSource messageSource = (AppMessageSource) source;
        iosNotification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_BADGE, 1);
        iosNotification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_EXTRAS, extras);
        androidNotification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS_EXTRAS, extras);

        if (extras.containsKey(PUSH_FIELD_JPUSH_NOTIFICATION_IOS_CA) && SafeConverter.toBoolean(extras.get(PUSH_FIELD_JPUSH_NOTIFICATION_IOS_CA))) {
            iosNotification.put(PUSH_FIELD_JPUSH_NOTIFICATION_IOS_CA, true);
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_IOS, iosNotification);
        // FIXME 只有蜂巢才加Android Notify
        if (messageSource == AppMessageSource.HONEYCOMB || messageSource == AppMessageSource.HONEYCOMB_EV) {
            notification.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION_ANDROID, androidNotification);
        }
        params.put(PushConstants.PUSH_FIELD_JPUSH_NOTIFICATION, notification);

        Map<String, Object> message = new HashMap<>();
        message.put(PushConstants.PUSH_FIELD_JPUSH_MESSAGE_TITLE, PushConstants.PUSH_VALUE_JPUSH_MESSAGE_TITLE);
        message.put(PushConstants.PUSH_FIELD_JPUSH_MESSAGE_CONTENT, content);
        message.put(PushConstants.PUSH_FIELD_JPUSH_MESSAGE_CONTENT_TYPE, PushConstants.PUSH_VALUE_JPUSH_MESSAGE_CONTENT_TYPE);
        message.put(PushConstants.PUSH_FIELD_JPUSH_MESSAGE_EXTRAS, extras);

        // FIXME  只有蜂巢才去掉Message
        if (messageSource != AppMessageSource.HONEYCOMB && messageSource != AppMessageSource.HONEYCOMB_EV) {
            params.put(PushConstants.PUSH_FIELD_JPUSH_MESSAGE, message);
        }

        Map<String, Object> options = new HashMap<>();
        options.put(PushConstants.PUSH_FIELD_JPUSH_OPTIONS_TTL, 86400 * 2);
        options.put(PushConstants.PUSH_FIELD_JPUSH_OPTIONS_APNS_PRODUCTION, true);
        //按tag推送要判断是否需要定速推送
        if (durationTime > 0) {
            options.put(PushConstants.PUSH_FIELD_JPUSH_OPTIONS_DURATION, durationTime);
        }
        params.put(PushConstants.PUSH_FIELD_JPUSH_OPTIONS, options);
        return Collections.singleton(params);
    }

    private Map<String, Object> getAudience(Map<String, Object> map, PushTarget pushTarget) {
        Map<String, Object> audienceMap = new HashMap<>();
        if (pushTarget == PushTarget.TAG) {
            List<String> tags = (List<String>) map.get("tags");
            List<String> tagsAnd = (List<String>) map.get("tagsAnd");

            //如果只有一个tag。被放到tagsAnd里了。需要在这里处理一下。用tag
            if (CollectionUtils.isEmpty(tags) && CollectionUtils.isNotEmpty(tagsAnd) && tagsAnd.size() == 1) {
                tags = new ArrayList<>();
                tags.addAll(tagsAnd);
                audienceMap.put(PushConstants.PUSH_FIELD_JPUSH_AUDIENCE_TAG, tags);
            } else if (CollectionUtils.isNotEmpty(tags)) {
                audienceMap.put(PushConstants.PUSH_FIELD_JPUSH_AUDIENCE_TAG, tags);
            } else if (CollectionUtils.isNotEmpty(tagsAnd)) {
                audienceMap.put(PushConstants.PUSH_FIELD_JPUSH_AUDIENCE_TAG_AND, tagsAnd);
            }
        } else {
            List<Long> userIds = (List<Long>) map.get("userIdList");
            // 一次推送最多1000个
            if (userIds.size() > 1000) {
                userIds = new ArrayList<>(userIds.subList(0, 1000));
            }
            List<String> alias = new ArrayList<>();
            for (Object userId : userIds) {
                alias.add(SafeConverter.toString(userId));
            }
            audienceMap.put(PushConstants.PUSH_FIELD_JPUSH_AUDIENCE_ALIAS, alias);
        }
        return audienceMap;
    }
}
