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

import java.util.Optional;

/**
 * Created by Summer on 2018/4/26
 */
@Slf4j
public class SubScribeEventHandler_Chips extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.CHIPS.name() + ":" + MessageFields.FIELD_EVENT;
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
                return handleOnSubscribe(context);
            case UNSUBSCRIBE:
                userService.unbindParent(context.getFromUserName());
                return "success";
            default:
                return "success";
        }
    }


    private String handleOnSubscribe(MessageContext context) {
        wechatMessageHelper.sendRegister(context.getFromUserName(), WechatType.CHIPS, WechatRegisterEventType.SUBSCRIBE);

        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        String url = Optional.ofNullable(WechatConfig.getBaseSiteUrl())
                .map(ur -> ur + "/chips/be/short/choice.vpage?refer=330357&channel=wechat")
                .map(ur -> ShortUrlGenerator.getShortUrlSiteUrl() + "/" + ShortUrlGenerator.generateShortUrl(ur, true).orElse(""))
                .orElse("");
        rb.buildMsgType(MessageType.TEXT);
        rb.buildContent("Hi，欢迎来到一起教育科技旗下英语口语学习产品-【薯条英语】！\n" +
                "没有开口自信？渴望与老外畅聊？\n" +
                "这可能是你见过最酷的口语学习方式！\n" +
                "吃一包薯条的时间，给你一口流利英文！\n" +
                "戳我购买：" + url);
        return rb.toString();

//        String siteUrl = WechatConfig.getBaseSiteUrl();
//
//        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
//        rb.buildMsgType(MessageType.TEXT);
//        rb.buildContent("Hi 你好哇，欢迎来到薯条英语O(∩_∩)O\n\n聊老外，学口语\nAI老师带你家宝贝情景对话\n" +
//                "第7期10天集训营，限额500人\n点这儿报名<a href=\"" + OAuthUrlGenerator.generatorForChips("chips_center") + "\">【购买链接】</a>\n" +
//                "——\n已报名的同学，记得在右下角个人中心，添加你的专属学习老师↘");
//        return rb.toString();
    }
}