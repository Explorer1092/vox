package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 扫描二维码事件
 * 分两种：未关注时扫描、已关注时扫描
 *
 * @author Xin Xin
 * @since 10/19/15
 */
@Slf4j
public class ScanEventHandler_Parent extends AbstractHandler {
    private final List<String> SUBSCRIBE_CODES = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");

    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_EVENT_KEY + ":" + MessageFields.FIELD_TICKET;
    }

    @Override
    public String handle(MessageContext context) {
        EventType eventType = EventType.of(context.getEvent());
        if (null == eventType) {
            log.warn("Invalid message type:{}", JsonUtils.toJson(context));
            return "success";
        }

        return handleOnSubscribe(context);
    }

    private String handleOnSubscribe(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);

        String eventKey = context.getEventKey().replace("qrscene_", "");
        if (SUBSCRIBE_CODES.contains(eventKey)) {//扫了一个关注二维码
            return sendMsgForSubscribe_Parent(context);
        }

        rb.buildContent("无效的二维码");
        return rb.toString();
    }
}
