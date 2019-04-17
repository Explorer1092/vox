package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaqCatalog;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.utils.MessageFields;

import java.util.List;
import java.util.Objects;

/**
 * @author Xin Xin
 * @since 10/21/15
 */
public class ClickEventHandler_Teacher extends AbstractHandler {
    private final String EVENT_KEY_CATALOG = "clk_faqcatalog";
    private final String EVENT_KEY_ONLINE = "clk_faqonline";

    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_EVENT_KEY;
    }

    @Override
    public String handle(MessageContext context) {
        Objects.requireNonNull(context, "context must not be null.");

        EventType type = EventType.of(context.getEvent());
        if (null == type) {
            return "success";
        }

        switch (type) {
            case CLICK:
                return handleOnClick(context);
            case VIEW:
                return handleOnView(context);
        }
        return "success";
    }

    private String handleOnClick(MessageContext context) {
        ReplyMessageBuilder rmb = new ReplyMessageBuilder(context);

        switch (context.getEventKey()) {
            case EVENT_KEY_CATALOG:
                List<WechatFaqCatalog> catalogs = wechatLoaderClient.loadWechatFaqCatalogs(WechatType.TEACHER);
                if (catalogs.size() > 0) {
                    rmb.buildMsgType(MessageType.NEWS);

                    for (int i = 0; i < catalogs.size(); i++) {
                        WechatFaqCatalog catalog = catalogs.get(i);
                        List<String> picUrls = JsonUtils.fromJsonToList(catalog.getPicUrl(), String.class);
                        String picUrl;
                        if (i == 0) {
                            picUrl = picUrls.get(0);
                            if (picUrl.length() == 0) {
                                picUrl = "wechat-teacher-faq-bigfirst.png";
                            }
                        } else {
                            picUrl = picUrls.get(1);
                        }

                        String siteUrl = WechatConfig.getBaseSiteUrl();

                        rmb.buildArticle(catalog.getName(), (null == catalog.getDescription() ? "" : catalog.getDescription()), (picUrl.length() > 0 ? siteUrl + "/teacher/test/wechatfaqimg/" + picUrl : ""), siteUrl + "/faq/catalog.vpage?id=" + catalog.getId() + "&t=" + WechatType.TEACHER.getType());
                    }
                } else {
                    rmb.buildMsgType(MessageType.TEXT);
                    rmb.buildContent("非常抱歉，解答专区还没有内容");
                }
            case EVENT_KEY_ONLINE:
                rmb.buildMsgType(MessageType.TEXT);
                rmb.buildContent("您好，很高兴为您服务，请在下方输入您的问题");
            default:
                rmb.buildMsgType(MessageType.TEXT);
                rmb.buildContent("您好，很高兴为您服务，请在下方输入您的问题");
        }
        return rmb.toString();
    }

    private String handleOnView(MessageContext context) {
        return "success";
    }
}
