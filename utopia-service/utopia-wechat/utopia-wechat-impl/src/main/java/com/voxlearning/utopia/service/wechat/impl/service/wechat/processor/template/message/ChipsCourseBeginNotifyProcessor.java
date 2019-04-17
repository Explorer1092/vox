package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/6/27
 */
@Named
public class ChipsCourseBeginNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "XaU0yozYJX-v5l38RCLPMf2_tY8SzNDmG4yYs1rJDCU";

    private final static String ONLINE_TEMPLATE_ID = "8BvZA-3QXPKBTC69X31jQsVU8uTgGEnyXFt_1yapC5Y";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_COURSE_BEGIN;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        return paramsMap;
    }
}
