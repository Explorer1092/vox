package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.impl.support.WechatConfig;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2018/8/4
 */
@Named
public class ChipsDailyShareRecordRemindProcessor extends WechatTemplateMessageProcessor {
    private final static String TEST_TEMPLATE_ID = "BYDvWySTfCRG8mh2RNAHp28qSLxKDgaG_HOkki5Y5QI";

    private final static String ONLINE_TEMPLATE_ID = "F7_40aXUnXrWFrIUxzM8HunHJEvFNBxEpKqQrujzZ1Y";

    @Override
    protected WechatTemplateMessageType type() {
        return WechatTemplateMessageType.CHIPS_DAILY_SHARE_RECORD_REMIND;
    }

    @Override
    protected Map<String, Object> params(Map<String, Object> extMap) {
        String bookId = "";
        if (MapUtils.isNotEmpty(extMap)) {
            bookId = SafeConverter.toString(extMap.get("bookId"));
        }
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("template_id", RuntimeMode.lt(Mode.STAGING) ? TEST_TEMPLATE_ID : ONLINE_TEMPLATE_ID);
        paramsMap.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/mysharerecord.vpage?bookId=" + bookId);
        return paramsMap;
    }
}
