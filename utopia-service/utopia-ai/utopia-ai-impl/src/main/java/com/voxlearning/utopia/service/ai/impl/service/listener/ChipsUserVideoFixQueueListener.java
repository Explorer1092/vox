package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserVideoDao;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 修复用户视频数据修复
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.user.video.data.fix.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.user.video.data.fix.queue")
        },
        maxPermits = 4
)
public class ChipsUserVideoFixQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private AIUserVideoDao aiUserVideoDao;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("video handle pic queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsUserVideoFixQueueListener error. message:{}", body);
                return;
            }

            String bookId = SafeConverter.toString(param.get("B"));
            String unitId = SafeConverter.toString(param.get("U"));
            String lessonId = SafeConverter.toString(param.get("L"));
            if (StringUtils.isNoneBlank(bookId, unitId, lessonId)) {
                aiUserVideoDao.loadByUnitId(lessonId, AIUserVideo.ExamineStatus.Waiting).forEach(e -> {
                    e.setUnitId(unitId);
                    e.setBookId(bookId);
                    aiUserVideoDao.upsert(e);
                });
            }
        }

    }
}
