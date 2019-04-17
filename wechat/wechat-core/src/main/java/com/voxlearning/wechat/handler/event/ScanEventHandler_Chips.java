

package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.constants.WechatRegisterEventType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.utils.MessageFields;
import lombok.extern.slf4j.Slf4j;


/**
 * 先占坑，防止报错
 */
@Slf4j
public class ScanEventHandler_Chips extends AbstractHandler {

    @Override
    public String getFingerprint() {
        return WechatType.CHIPS.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_EVENT_KEY + ":" + MessageFields.FIELD_TICKET;
    }

    @Override
    public String handle(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TEXT);
        EventType eventType = EventType.of(context.getEvent());
        if (null == eventType) {
            log.warn("Invalid message type,msg:{}", JsonUtils.toJson(context));
            return "success";
        }
        String eventKey;
        switch (eventType) {
            case SUBSCRIBE:
                eventKey = context.getEventKey().substring(8);//qrscene_ 前缀
                break;
            case SCAN:
                eventKey = context.getEventKey();
                break;
            default:
                eventKey = "temp";
        }
        String url = WechatConfig.getBaseSiteUrl() + "/chips/center/" + eventKey + "/learning_duration.vpage";
        String shortUrl = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + ShortUrlGenerator.generateShortUrl(url, true).orElse("");
        rb.buildContent("Hi，欢迎来到一起教育科技旗下英语口语学习产品-【薯条英语】！\n" +
                "没有开口自信？渴望与老外畅聊？\n" +
                "\n" +
                "这可能是你见过最酷的口语学习方式！\n" +
                "\n" +
                "吃一包薯条的时间，给你一口流利英文！\n" +
                "\n" +
                "戳我购买：" +  shortUrl);
        wechatMessageHelper.sendRegister(context.getFromUserName(), WechatType.CHIPS, WechatRegisterEventType.SUBSCRIBE);
        return rb.toString();
    }


}
