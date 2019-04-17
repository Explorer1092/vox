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
public class ChipsTeamCombinaFailNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "oySgVvnX80VwcIvfNdgTcJaogaGuAr2UxaN2iHLX2dk";

    private final static String ONLINE_TEMPLATE_ID = "Ei_qNR8BAnEplYAenmuO3yUmNfgtstiDst_0bsaEJrw";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_INVITATION_COM_FAIL_NOTIFY;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/invite_personal_center.vpage");
        return paramsMap;
    }
}
