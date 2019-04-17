package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;

import java.util.Objects;

/**
 * 占坑
 *
 */
public class ClickEventHandler_Chips extends AbstractHandler {


    @Override
    public String getFingerprint() {
        return WechatType.CHIPS.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_EVENT_KEY;
    }

    @Override
    public String handle(MessageContext context) {
        Objects.requireNonNull(context, "context must not be null.");

        return "success";

    }
}
