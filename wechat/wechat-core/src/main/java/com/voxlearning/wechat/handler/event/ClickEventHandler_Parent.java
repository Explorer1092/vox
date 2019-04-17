package com.voxlearning.wechat.handler.event;

import com.voxlearning.utopia.core.cdn.url2.CdnRuleType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaqCatalog;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.utils.MessageFields;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
public class ClickEventHandler_Parent extends AbstractHandler {
    private final String EVENT_KEY_CATALOG = "clk_faqcatalog";
    private final String EVENT_KEY_ONLINE = "clk_faqonline";

    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_EVENT_KEY;
    }

    @Override
    public String handle(MessageContext context) {
        Objects.requireNonNull(context, "context must not be null.");

        EventType eventType = EventType.of(context.getEvent());
        if (null == eventType) return "success";

        switch (eventType) {
            case CLICK:
                return handleOnClick(context);
            case VIEW:
                return handleOnView(context);
        }
        return "success";
    }

    private String handleOnClick(MessageContext context) {
        ReplyMessageBuilder rb = new ReplyMessageBuilder();
        rb.buildToUserName(context.getFromUserName());
        rb.buildFromUserName(context.getToUserName());
        rb.buildCreateTime((long) Instant.now().getNano());

        switch (context.getEventKey()) {
            case EVENT_KEY_CATALOG:
                List<WechatFaqCatalog> catalogs = wechatLoaderClient.loadWechatFaqCatalogs(WechatType.PARENT);
                if (catalogs.size() > 0) {
                    rb.buildMsgType(MessageType.NEWS);
                    String siteUrl = WechatConfig.getBaseSiteUrl();
                    for (int i = 0; i < catalogs.size(); i++) {
                        WechatFaqCatalog catalog = catalogs.get(i);
                        rb.buildArticle(catalog.getName(), (null == catalog.getDescription() ? "" : catalog.getDescription()), cdnResourceUrlGenerator.combineCdnUrl(getRequestContext().getRequest(), CdnRuleType.AUTO.typeName(), "/wechat/public/images/help/category/" + (i == 0 ? "big-" : "small-") + catalog.getPicUrl()), siteUrl + "/faq/catalog.vpage?id=" + catalog.getId() + "&t=" + WechatType.PARENT.getType());
                    }
                } else {
                    rb.buildMsgType(MessageType.TEXT);
                    rb.buildContent("非常抱歉，解答专区还没有内容");
                }
                break;
            case EVENT_KEY_ONLINE:
                rb.buildMsgType(MessageType.TEXT);
                rb.buildContent("遇到问题了？输入问题即可");
                break;
        }
        return rb.toString();
    }

    private String handleOnView(MessageContext context) {
        return "success";
    }
}
