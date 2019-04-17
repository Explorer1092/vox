package com.voxlearning.wechat.handler;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * 教师端图片消息处理
 * @author Xin Xin
 * @since 10/21/15
 */
public class ImageMessageHandler_Teacher extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_PIC_URL + ":" + MessageFields.FIELD_MEDIA_ID;

    }

    @Override
    public String handle(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TRANSFER_CUSTOMER_SERVICE);
        return rb.toString();
    }
}
