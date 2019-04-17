package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.vendor.impl.push.PushProducer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * internal jpushRetry queue sender
 * Created by malong on 2016/4/25.
 */
@Named
public class InternalPushRetryQueueSender extends SpringContainerSupport {

    @Inject
    private PushProducer pushProducer;

    public void sendJpushNotify(Map<String, Object> sendMessage) {
        String messageText = JsonUtils.toJson(sendMessage);
        Object sourceObject = sendMessage.get("source");
        AppMessageSource appMessageSource = AppMessageSource.of(SafeConverter.toString(sourceObject));
        if (appMessageSource == AppMessageSource.UNKNOWN) {
            return;
        }
        if (appMessageSource == AppMessageSource.YIQIXUETEAHCER || appMessageSource == AppMessageSource.YIQIXUEPARENT) {
            pushProducer.getYiQiXueRetryProducer().produce(Message.newMessage().withStringBody(messageText));
        } else {
            pushProducer.getJpushRetryProducer().produce(Message.newMessage().withStringBody(messageText));
        }
    }

    public void sendUmengNotify(PushContext context) {
        pushProducer.getUmengRetryProducer().produce(Message.newMessage().writeObject(context));
    }
}
