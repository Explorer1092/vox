package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewhomeworkVendorQueueProducer;
import com.voxlearning.utopia.service.vendor.api.constant.HomeworkVendorMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
@Named
public class NewHomeworkVendorMessageSender extends SpringContainerSupport {

    @Inject private NewhomeworkVendorQueueProducer newhomeworkVendorQueueProducer;

    public void sendMessageToVendor(HomeworkVendorMessageType messageType, Map<String, Object> extInfo) {
        Map<String, Object> info = new HashMap<>();
        info.put("messageType", messageType.getType());
        info.put("extInfo", extInfo);
        Message message = Message.newMessage().withStringBody(JsonUtils.toJson(info));
        newhomeworkVendorQueueProducer.getProducer().produce(message);
    }

}
