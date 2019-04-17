package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.impl.support.WechatConfig;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;


@Named
public class ChipsInvitationSuccessProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "Hp202c8Nf1nf65u3W8Vyfb3f1tlslj8iQwXL01zpbLQ";

    private final static String ONLINE_TEMPLATE_ID = "Wiy4pxlsR0Jgmyy0JoDdrjD4XGBxIVwsAihJgEclEUo";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_INVITATION_SUCCESS;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id",  RuntimeMode.le(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/invite_award_activity.vpage");
        return paramsMap;
    }
}
