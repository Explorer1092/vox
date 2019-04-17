package com.voxlearning.wechat.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.WechatRegisterEventType;
import org.slf4j.Logger;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/6/13
 */
@Named
public class WechatMessageHelper {
    private static final Logger logger = LoggerFactory.getLogger(WechatMessageHelper.class);

    @AlpsQueueProducer(queue = "utopia.wechat.template.message.queue")
    private MessageProducer wechatTempleteMessageQueueProducer;

    @AlpsQueueProducer(queue = "utopia.wechat.user.register.message.queue")
    private MessageProducer wechatRegisterMessageQueueProducer;

    public void sendUpdateTemplateMessageState(String openId, WechatType wechatType, String msgId, WechatNoticeState state, String error) {
        Map<String, Object> message = new HashMap<>();
        message.put("ID", openId);
        message.put("MID", msgId);
        message.put("T", wechatType.getType());
        message.put("S", state.getType());
        message.put("E", error);
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("utopia.wechat.template.message.queue：{}", message);
        }
        wechatTempleteMessageQueueProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
    }

    public void sendRegister(String openId, WechatType wechatType, WechatRegisterEventType eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("ID", openId);
        message.put("T", wechatType.getType());
        message.put("R", eventType.name());
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("utopia.wechat.template.message.queue：{}", message);
        }
        wechatRegisterMessageQueueProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
    }

}
