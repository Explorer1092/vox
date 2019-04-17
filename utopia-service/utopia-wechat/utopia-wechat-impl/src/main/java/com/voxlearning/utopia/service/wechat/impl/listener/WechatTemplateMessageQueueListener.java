
package com.voxlearning.utopia.service.wechat.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatTemplateMessageRecordPersistence;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.wechat.template.message.queue"),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.wechat.template.message.queue")
        },
        maxPermits = 64
)
public class WechatTemplateMessageQueueListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatTemplateMessageRecordPersistence wechatTemplateMessageRecordPersistence;

    @Override
    public void onMessage(Message message) {
        if (RuntimeMode.lt(Mode.STAGING)) {
            logger.info("wechat template message queue:" + message);
        }

        if (message == null) {
            logger.error("wechat template message queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (MapUtils.isEmpty(param)) {
                logger.error("WechatTemplateMessageQueueListener message empty. message:{}", body);
                return;
            }

            String openId = SafeConverter.toString(param.get("ID"));
            String msgId = SafeConverter.toString(param.get("MID"));
            WechatType wechatType = WechatType.of(SafeConverter.toInt(param.get("T")));
            WechatNoticeState state = WechatNoticeState.of(SafeConverter.toInt(param.get("S")));
            String error = SafeConverter.toString(param.get("E"), "");

            if (StringUtils.isAnyBlank(openId, msgId) || wechatType == null || state == null) {
                logger.error("WechatTemplateMessageQueueListener message error. message:{}", body);
                return;
            }

            wechatTemplateMessageRecordPersistence.updateState(openId, wechatType, msgId, state, error);
        }
    }
}