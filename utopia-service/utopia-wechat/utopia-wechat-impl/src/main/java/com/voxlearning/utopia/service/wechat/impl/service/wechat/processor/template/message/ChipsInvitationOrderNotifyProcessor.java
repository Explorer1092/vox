package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.impl.support.WechatConfig;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;


@Named
public class ChipsInvitationOrderNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "lCqIC4IZylCkpcefc-_4j0-XPOocpL8Yd5S9O4AgbJY";

    private final static String ONLINE_TEMPLATE_ID = "_CizBcnh9CTUnGZFj4-_ZpifOagXmP820yppiXH6YJY";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_INVITATION_ORDER_NOTIFY;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/invite_award_activity.vpage");
        return paramsMap;
    }
}
