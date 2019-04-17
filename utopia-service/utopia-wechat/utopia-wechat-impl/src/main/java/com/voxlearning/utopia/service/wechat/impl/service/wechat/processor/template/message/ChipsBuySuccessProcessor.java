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
 * @since 2018/6/13
 */
@Named
public class ChipsBuySuccessProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "j16Jei2LrLNYbyMusT32AegF5QvaI02Dy0-HOuFmAcw";

    private final static String ONLINE_TEMPLATE_ID = "_kaDG0APTDCtcuo1gpGiRAllwW2DFNHeHghdF-7459A";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_BUY_SUCCESS;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/myteacher.vpage");
        return paramsMap;
    }
}
