package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;

/**
 * 上报地下位置事件处理
 *
 * @author Xin Xin
 * @since 10/19/15
 */
public class LocationEventHandler_Parent extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_LATITUDE + ":" + MessageFields.FIELD_LONGITUDE + ":" + MessageFields.FIELD_PRECISION;
    }

    @Override
    public String handle(MessageContext context) {
        return "success";
    }
}
