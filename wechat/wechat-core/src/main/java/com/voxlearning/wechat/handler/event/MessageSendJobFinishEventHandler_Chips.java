package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * 占坑
 *
 * 高级群发消息结果推送事件处理
 */
public class MessageSendJobFinishEventHandler_Chips extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.CHIPS.name() + ":" + MessageFields.FIELD_STATUS + ":" + MessageFields.FILED_TOTAL_COUNT
                + ":" + MessageFields.FILED_FILTER_COUNT + ":" + MessageFields.FILED_SENT_COUNT
                + ":" + MessageFields.FILED_ERROR_COUNT;
    }

    @Override
    public String handle(MessageContext context) {
        return "success";
    }
}
