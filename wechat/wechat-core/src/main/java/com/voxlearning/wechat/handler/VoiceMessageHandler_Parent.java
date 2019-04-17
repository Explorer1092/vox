package com.voxlearning.wechat.handler;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * 处理语音消息
 * 如果开启了语音识别，则增加一个Recongnition字段
 *
 * @author Xin Xin
 * @since 10/19/15
 */
public class VoiceMessageHandler_Parent extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_MEDIA_ID + ":" + MessageFields.FIELD_FORMAT;
    }

    @Override
    public String handle(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TRANSFER_CUSTOMER_SERVICE);
        return rb.toString();
    }
}
