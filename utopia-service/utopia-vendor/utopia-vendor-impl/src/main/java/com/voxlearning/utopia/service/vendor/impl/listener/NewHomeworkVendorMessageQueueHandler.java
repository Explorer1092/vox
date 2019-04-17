package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.vendor.api.constant.HomeworkVendorMessageType;
import com.voxlearning.utopia.service.vendor.impl.push.NewHomeworkVendorMessageProcessorManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016-9-12
 */
@Named
public class NewHomeworkVendorMessageQueueHandler extends SpringContainerSupport {

    @Inject
    private NewHomeworkVendorMessageProcessorManager newHomeworkVendorMessageProcessorManager;

    public void handleMessage(String messageText) {
        if (StringUtils.isBlank(messageText)) {
            return;
        }
        Map<String, Object> messageMap = JsonUtils.fromJson(messageText);
        process(messageMap);
    }

    private void process(Map<String, Object> messageMap) {
        Integer type = SafeConverter.toInt(messageMap.get("messageType")); //messageType值是约定的,要改两边都改
        HomeworkVendorMessageType messageType = HomeworkVendorMessageType.ofWithUnKnow(type);
        if (messageType == HomeworkVendorMessageType.UNKNOWN) {
            return;
        }
        newHomeworkVendorMessageProcessorManager.get(messageType).process(messageMap);
    }
}
