package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.utils.MessageFields;
import org.slf4j.Logger;

/**
 *先占坑，防止报错
 */
public class TemplateMsgHandler_Chips extends AbstractHandler {
    private final static Logger logger = LoggerFactory.getLogger(TemplateMsgHandler_Chips.class);

    @Override
    public String getFingerprint() {
        return WechatType.CHIPS.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_STATUS;
    }

    @Override
    public String handle(MessageContext context) {
        WechatNoticeState state ;
        String error;
        switch(context.getStatus()) {
            case "success" :
                state = WechatNoticeState.SUCCESS;
                error = "0";
                break;
            case "failed:user block":
                state = WechatNoticeState.FAILED;
                error = "usrblk";
                break;
            case "failed: system failed":
                state = WechatNoticeState.FAILED;
                error = "syserr";
                break;
            default:
                state = WechatNoticeState.FAILED;
                error = "other";
                break;
        }
        try {
            wechatMessageHelper.sendUpdateTemplateMessageState(context.getFromUserName(), WechatType.CHIPS, context.getMsgID(), state, error);
        } catch (Exception e) {
            logger.error("sendUpdateStateMessage error. openId:{}, msgId:{}, state:{}, error:{}", context.getFromUserName(), context.getMsgID(), state, error, e);
        }

        return "success";
    }
}
