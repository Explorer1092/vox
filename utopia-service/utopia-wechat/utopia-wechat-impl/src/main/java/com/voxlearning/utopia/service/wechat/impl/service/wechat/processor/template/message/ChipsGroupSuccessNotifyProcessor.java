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
public class ChipsGroupSuccessNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "MTjN4cYK-dKwvGXlUak2SCn7dJytqc8sm7tHSgx6LRI";

    private final static String ONLINE_TEMPLATE_ID = "ueqb1IlxYA8S78DxR5wkX8lWjQ_BgeYQNRpP7KeFGf4";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_GROUP_SHOPPING_SUCCESS;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        return paramsMap;
    }
}
