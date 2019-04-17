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
public class ChipGraduationCertificateNotifyProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "NGdzUpa6FBG0dG6Us-ldbnbPwOclXtFQwuif-WmFgrE";

    private final static String ONLINE_TEMPLATE_ID = "vb-_RL03GjgibXg4N2LQbZcjzVHh2SnMa9kNqfkdnpU";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_COURSE_GRADUATION_CERTIFICATE;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/getcertificate.vpage");
        return paramsMap;
    }
}
