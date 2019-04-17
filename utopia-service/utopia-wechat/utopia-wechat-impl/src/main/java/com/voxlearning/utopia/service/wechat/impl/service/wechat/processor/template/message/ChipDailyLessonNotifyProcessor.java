package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.impl.support.WechatConfig;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/3
 * 今日学习内容微信公众号通知推送
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=39718159
 */
@Named
public class ChipDailyLessonNotifyProcessor extends WechatTemplateMessageProcessor {

    private final static String TEST_TEMPLATE_ID = "oB3FOh7MGL95GaVSupfw5EaA4wXu-IeskMTIl_qwt0o";

    private final static String ONLINE_TEMPLATE_ID = "dfJUJnAGHDXlbBLzrtJTptQlzLI3OGaTNrG37PoXO3s";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_COURSE_DAILY_LESSON;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/getcertificate.vpage");
        return paramsMap;

    }
}
