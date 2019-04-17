package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;


@Named
public class ChipsADVisitNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "9H4q1itjxwrVO9UWMQTEP8duSmrD_JUh5P6iF9Ns2VU";

    private final static String ONLINE_TEMPLATE_ID = "aDTyNLOwlbcYm3eqQO-FYwkHDC3XBnW5sNt__Fu2Ymc";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_AD_VISIT_NOTIFY;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id",  RuntimeMode.le(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        return paramsMap;
    }
}
