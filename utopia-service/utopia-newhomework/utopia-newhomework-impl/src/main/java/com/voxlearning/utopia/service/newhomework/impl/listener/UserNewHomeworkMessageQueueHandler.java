package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.impl.support.processor.UserNewHomeworkMessageProcessorManager;
import com.voxlearning.utopia.service.user.api.constants.UserNewHomeworkMessageType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2016-10-13
 */
@Named
public class UserNewHomeworkMessageQueueHandler extends SpringContainerSupport {

    @Inject
    private UserNewHomeworkMessageProcessorManager userNewHomeworkMessageProcessorManager;

    public void handleMessage(String messageText) {
        if (StringUtils.isBlank(messageText)) {
            return;
        }
        Map<String, Object> map = JsonUtils.fromJson(messageText);
        doProcess(map);

    }

    private void doProcess(Map<String, Object> map) {
        UserNewHomeworkMessageType messageType = UserNewHomeworkMessageType.ofWithUnKnow(SafeConverter.toInt(map.get("messageType")));
        if (messageType == UserNewHomeworkMessageType.UNKNOWN) {
            return;
        }
        userNewHomeworkMessageProcessorManager.get(messageType).process(map);
    }
}
