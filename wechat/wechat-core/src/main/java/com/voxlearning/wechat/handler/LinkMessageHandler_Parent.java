package com.voxlearning.wechat.handler;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
public class LinkMessageHandler_Parent extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_TITLE + ":" + MessageFields.FIELD_DESCRIPTION + ":" + MessageFields.FIELD_URL;
    }

    @Override
    public String handle(MessageContext context) {
        return "success";
    }
}
