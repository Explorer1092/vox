package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by xinxin on 16/11/15.
 */
@Slf4j
public class KfAccountEventHandler_Parent extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_KF_ACCOUNT;
    }

    @Override
    public String handle(MessageContext context) {
        EventType eventType = EventType.of(context.getEvent());
        if (null == eventType) {
            log.warn("Invalid message type:{}", JsonUtils.toJson(context));
            return "success";
        }

        switch (eventType) {
            case KF_CREATE_SESSION:
                break;
            case KF_SWITCH_SESSION:
                break;
            case KF_CLOSE_SESSION:
                break;
            default:
                break;
        }
        return "success";
    }
}
