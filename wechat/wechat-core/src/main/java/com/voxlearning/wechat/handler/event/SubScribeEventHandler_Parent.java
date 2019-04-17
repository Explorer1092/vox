package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
@Slf4j
public class SubScribeEventHandler_Parent extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_EVENT;
    }

    @Override
    public String handle(MessageContext context) {
        EventType eventType = EventType.of(context.getEvent());
        if (null == eventType) {
            log.warn("Invalid message type,msg:{}", JsonUtils.toJson(context));
            return "success";
        }

        switch (eventType) {
            case SUBSCRIBE:
                return sendMsgForSubscribe_Parent(context);
            case UNSUBSCRIBE:
                userService.unbindParent(context.getFromUserName());
                return "success";
            default:
                return "success";
        }
    }
}
