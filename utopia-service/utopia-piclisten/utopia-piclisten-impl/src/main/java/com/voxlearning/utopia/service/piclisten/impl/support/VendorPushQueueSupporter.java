package com.voxlearning.utopia.service.piclisten.impl.support;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2018-08-23 下午1:19
 **/
@Named
public class VendorPushQueueSupporter {

    private static final String TYPE = "type";
    private static final String TEXT = "text";
    private static final String SOURCE = "source";
    private static final String USER_IDS = "userIds";
    private static final String EXT = "ext";
    private static final String SEND_TIME = "sendTime";



    @Getter
    @AlpsQueueProducer(queue = "utopia.push.event.queue")
    private MessageProducer producer;


    public void sendAppJpushMessageByIds(String content, String source, List<Long> userIds, Map<String, Object> extInfo){
        Map<String, Object> map = new HashMap<>();
        map.put(TYPE, "COMMON_SEND_PUSH_UIDS");
        map.put(SOURCE, source);
        map.put(USER_IDS, userIds);
        map.put(EXT, extInfo);
        map.put(TEXT, content);
        producer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
    }
}
