package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.builder.ServiceMessageBuilder;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.utils.MessageFields;

import java.util.Objects;

/**
 * @author Xin Xin
 * @since 10/21/15
 */
public class SubscribeEventHandler_Teacher extends AbstractHandler {

    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_EVENT;

    }

    @Override
    public String handle(MessageContext context) {
        Objects.requireNonNull(context, "context must not be null.");

        EventType eventType = EventType.of(context.getEvent());
        switch (eventType) {
            case SUBSCRIBE:
                return handleOnSubscribe(context);
            case UNSUBSCRIBE:
                return handleOnUnsubscribe(context);
        }
        return "success";
    }

    private String handleOnUnsubscribe(MessageContext context) {
        String siteUrl = WechatConfig.getBaseSiteUrl();

        //发一条客服消息、一条被动回复文本消息
        ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
        sb.buildArticleMsg("欢迎加入一起作业，点此注册", "", siteUrl + "/teacher/static/images/campaign/welcome.jpg", siteUrl + "/teacher/login.vpage?_from=subtui&woid=" + context.getFromUserName());
        messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);

        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);
        rb.buildContent("老师您来啦~欢迎加入17作业大家庭\n\n    <a href=\"http://x.eqxiu.com/s/mxMrtVTc\">【我是老师】</a> 点击这里\n\n    <a href=\"http://mp.weixin.qq.com/s?__biz=MjM5NjE5OTc0MQ==&mid=548556088&idx=1&sn=333190188ea09907b6583fdc149d2a2e#rd\">【我是家长】</a> 专属福利\n\n如果遇到任何疑问，可以随时联系我哦~\n\u2199快点左下角小键盘\ue415");
        return rb.toString();
    }

    private String handleOnSubscribe(MessageContext context) {
        userService.unbindParent(context.getFromUserName());

        return "success";
    }
}
