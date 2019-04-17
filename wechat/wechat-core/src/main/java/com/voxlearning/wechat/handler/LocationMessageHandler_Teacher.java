package com.voxlearning.wechat.handler;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * @author Xin Xin
 * @since 10/21/15
 */
public class LocationMessageHandler_Teacher extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_LOCATION_X + ":" + MessageFields.FIELD_LOCATION_Y + ":" + MessageFields.FIELD_SCALE + ":" + MessageFields.FIELD_LABEL;
    }

    @Override
    public String handle(MessageContext context) {
        return "success";
    }
}
