package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.constant.AiUserVideoLevel;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActiveServiceRecordDao;
import com.voxlearning.utopia.service.ai.impl.service.ChipsUserVideoServiceImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 视频合成
 */

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.ai.user.audioImage2video.success.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.ai.user.audioImage2video.success.queue")
        },
        maxPermits = 64
)
public class AIUserAudioImageHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsActiveServiceRecordDao activeServiceRecordDao;
    @Inject
    private ChipsUserVideoServiceImpl chipsUserVideoService;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("audio image handle queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("AIUserAudioImageHandleQueueListener error. message:{}", body);
                return;
            }
            String video = SafeConverter.toString(param.get("video"));
            String id = SafeConverter.toString(param.get("ID"), "");
            if (StringUtils.isBlank(id)) {
                logger.warn("id is illegal.id:{}", id);
                return;
            }
            MapMessage result = MapMessage.successMessage();
            Long userId = 0L;
            if (id.contains(".")) {
                String[] splitIds = id.split("\\.");//String id = userId + "." + unitId + "." + level.name() + "." + qrcId + "." + lscore ;
                if (splitIds.length < 5) {
                    logger.error("AIUserAudioImageHandleQueueListener error. message:{}, video:{}, id:{}", body, video, id);
                    return;
                }
                try {
                    userId = Long.parseLong(splitIds[0]);
                    chipsUserVideoService.examineAudio(userId, splitIds[1], AiUserVideoLevel.valueOf(splitIds[2]), splitIds[3], Integer.parseInt(splitIds[4]), video);
                } catch (Exception e) {
                    result = MapMessage.errorMessage("处理失败");
                }
            }

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod2", video,
                    "mod3", body,
                    "mod5", JsonUtils.toJson(result),
                    "op", "ai user audioImage2video"
            ));

            if (RuntimeMode.lt(Mode.STAGING)) {
                logger.info("AIUserAudioImageHandleQueueListener, usertoken:{}, video:{}, message:{}, result:{}", userId, video, body, result);
            }
        }
    }
}
