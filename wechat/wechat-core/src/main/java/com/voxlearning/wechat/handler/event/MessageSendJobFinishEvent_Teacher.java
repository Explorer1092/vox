package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * @author xinxin
 * @since 4/13/17.
 */
public class MessageSendJobFinishEvent_Teacher extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_STATUS + ":" + MessageFields.FILED_TOTAL_COUNT
                + ":" + MessageFields.FILED_FILTER_COUNT + ":" + MessageFields.FILED_SENT_COUNT
                + ":" + MessageFields.FILED_ERROR_COUNT;
    }

    @Override
    public String handle(MessageContext context) {
        return "success";
    }
}
