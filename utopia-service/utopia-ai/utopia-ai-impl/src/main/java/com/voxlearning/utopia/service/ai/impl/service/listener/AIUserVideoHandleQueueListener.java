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
import com.voxlearning.utopia.service.ai.entity.ChipsActiveServiceRecord;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActiveServiceRecordDao;
import com.voxlearning.utopia.service.ai.internal.ChipsVideoService;
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
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.ai.user.video.handle.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.ai.user.video.handle.queue")
        },
        maxPermits = 64
)
public class AIUserVideoHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsVideoService chipsUserVideoService;
    @Inject
    private ChipsActiveServiceRecordDao activeServiceRecordDao;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("video handle queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("AIUserVideoHandleQueueListener error. message:{}", body);
                return;
            }
            String video = SafeConverter.toString(param.get("videoUrl"));
            String id = SafeConverter.toString(param.get("id"), "");
            if (StringUtils.isBlank(id) || id.length() < 5) {
                logger.warn("id is illegal.id:{}", id);
                return;
            }
            String sessionId = "";
            long userId = 0;
            String lessonId = "";
            MapMessage result = MapMessage.successMessage();
            if (id.contains(".")) {
                String[] splitIds = id.split("\\.");
                if (splitIds.length < 1) {
                    logger.error("AIUserVideoHandleQueueListener error. message:{}, video:{}, id:{}",body, video, id);
                    return;
                }
                try {
                    switch (splitIds[0]) {
                        case "1" :
                            lessonId = SafeConverter.toString(splitIds[2]);
                            userId = SafeConverter.toLong(splitIds[1]);
                            sessionId = SafeConverter.toString(splitIds[3]);
                            result = chipsUserVideoService.saveUserVideoV2(userId, lessonId, sessionId, video);
                            break;
                        case "2" :
                            userId = SafeConverter.toLong(splitIds[1]);
                            lessonId = SafeConverter.toString(splitIds[2]);
                            sessionId = SafeConverter.toString(splitIds[3]);
                            result = chipsUserVideoService.saveWechatUserVideo(userId, lessonId, sessionId, video);
                            break;
                        case "3" :
                            String userVideoId = splitIds[1];
                            chipsUserVideoService.updateFinishVideoStatus(userVideoId, video);
                            break;
                        case "4" :
                            String userIdStr = splitIds[1];
                            String unitId = splitIds[2];
                            activeServiceRecordDao.updateExamineStatusAndVideoUrl(Long.valueOf(userIdStr), unitId, video);
                            activeServiceRecordDao.updateRemarkStatus(Long.valueOf(userIdStr), unitId, ChipsActiveServiceRecord.RemarkStatus.Five);
                            break;
                        default:
                            if (splitIds.length > 3) {
                                result = splitIds[3].equals("1") ? chipsUserVideoService.saveUserVideoV2(userId, lessonId, sessionId, video) : chipsUserVideoService.saveWechatUserVideo(userId, lessonId, sessionId, video);
                            } else {
                                result = chipsUserVideoService.saveAIVideoResultV1(userId, lessonId, sessionId, video);
                            }
                            break;
                    }
                } catch (Exception e) {
                    result = MapMessage.errorMessage("处理失败");
                }
            }

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", lessonId,
                    "mod2", video,
                    "mod3", body,
                    "mod4", sessionId,
                    "mod5", JsonUtils.toJson(result),
                    "op", "ai user video to handle"
            ));

            if (RuntimeMode.lt(Mode.STAGING)) {
                logger.info("AIUserVideoHandleQueueListener, usertoken:{}, qid: {}, video:{}, sessionId:{}, message:{}, result:{}", userId, lessonId, video, sessionId, body, result);
            }
        }
    }
}
