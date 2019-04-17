package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.impl.support.WechatConfig;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/6/27
 */
@Named
public class ChipsInvitationTeamCombineNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "_xWwMDvcVJWh-A2S_F_TLfgdOF2L4L_jfWgmMcGFs20";

    private final static String ONLINE_TEMPLATE_ID = "OB2qS0mJWWHZ8s1t9E5hVQS1I0baKahTORwrZHyGxN0";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_INVITATION_TEAM_COM_NOTIFY;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        return paramsMap;
    }
}
