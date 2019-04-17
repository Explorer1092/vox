package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author songtao
 * @since 2018/6/27
 */
@Named
public class ChipsCourseFeedbackNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "_TqHzI9s6gC8O8a6dPksAERkNKKftrRmeYrpSWHYjGA";

    private final static String ONLINE_TEMPLATE_ID = "td6ifMKVzObL4pYsjLhDG5w5XhQBmh8aHTDVj3fgEDY";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_COURSE_FEEDBACK;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", "https://www.wjx.top/jq/30789363.aspx");
        return paramsMap;
    }
}
