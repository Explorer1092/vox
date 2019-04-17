
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
import com.voxlearning.utopia.service.wechat.api.entities.WechatTemplateMessageRecord;
import com.voxlearning.utopia.service.wechat.consumer.helpers.WechatCodeManager;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatTemplateMessageRecordPersistence;
import com.voxlearning.utopia.service.wechat.impl.support.WechatTemplateMessageUtil;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.wechat.template.message.fix.send.queue"),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.wechat.template.message.fix.send.queue")
        },
        maxPermits = 64
)
public class WechatTemplateMessageFixSendQueueListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static List<String> ERROR_CODES = Arrays.asList("40001");
    @Inject
    private WechatTemplateMessageRecordPersistence wechatTemplateMessageRecordPersistence;
    @Inject private WechatCodeManager wechatCodeManager;

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
                logger.error("WechatTemplateMessageFixSendQueueListener message empty. message:{}", body);
                return;
            }

            Long id = SafeConverter.toLong(param.get("ID"));
            WechatTemplateMessageRecord document = wechatTemplateMessageRecordPersistence.$load(id);
            if (document != null && Boolean.FALSE.equals(document.getDisabled()) &&
                    document.getState() != WechatNoticeState.SENDED && ERROR_CODES.contains(document.getErrorCode())) {
                String token = wechatCodeManager.generateAccessToken(WechatType.of(document.getWechatType()));
                Map<String,Object> map = JsonUtils.fromJson(document.getMessage());
                if (StringUtils.isBlank(token) || MapUtils.isEmpty(map)) {
                    return;
                }
                Map<String, Object> resMap = WechatTemplateMessageUtil.send(token, map);
                String msgId = Optional.of(resMap)
                        .filter(e -> MapUtils.isNotEmpty(e))
                        .map(e -> SafeConverter.toString(e.get("msgid"),""))
                        .orElse(null);
                WechatNoticeState state;
                String errorCode;
                if (MapUtils.isNotEmpty(resMap) && "0".equals(SafeConverter.toString(resMap.get("errcode"), "")) ) {
                    state = WechatNoticeState.SENDED;
                    errorCode = "";
                } else {
                    state = WechatNoticeState.FAILED;
                    errorCode = MapUtils.isNotEmpty(resMap) ? SafeConverter.toString(resMap.get("errcode"), "") : "500";
                }
                wechatTemplateMessageRecordPersistence.updateState(id, state, errorCode, msgId);
            }

        }
    }
}