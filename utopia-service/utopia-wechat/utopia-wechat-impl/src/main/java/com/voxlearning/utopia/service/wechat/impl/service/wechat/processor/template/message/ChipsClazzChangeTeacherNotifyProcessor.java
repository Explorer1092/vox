package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;


@Named
public class ChipsClazzChangeTeacherNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "oySgVvnX80VwcIvfNdgTcJaogaGuAr2UxaN2iHLX2dk";

    private final static String ONLINE_TEMPLATE_ID = "xxc5WWbhZ1_P0J3oQ1uBd4bQZjr6qvlnIhYZ665t2EM";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_CLAZZ_CHANG_TEACHER_NOTIFY;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        return paramsMap;
    }
}
